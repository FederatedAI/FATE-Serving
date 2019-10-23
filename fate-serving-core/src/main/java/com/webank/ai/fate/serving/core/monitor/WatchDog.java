/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.core.monitor;


import com.webank.ai.fate.serving.core.bean.Context;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class WatchDog {

    private static final int MAX_PROCESS_COUNT = 100000;
    private static AtomicLong RPC_IN_PROCESS = new AtomicLong(0);
    private static AtomicLong TOTAL_RPC_PROCESS = new AtomicLong(0);
    private static AtomicLong COMPLATE_RPC_PROCESS = new AtomicLong(0);
    private static ConcurrentHashMap<String, AtomicLong> SERVICE_PROCESS_COUNT_MAP = new ConcurrentHashMap<String, AtomicLong>();

    public static void enter(Context context) {
        RPC_IN_PROCESS.addAndGet(1);
    }

    public static void quit(Context context) {
        RPC_IN_PROCESS.decrementAndGet();
    }

    public static long get() {
        return RPC_IN_PROCESS.get();
    }

    public static void enter(String serviceName) {
        if (StringUtils.isNotEmpty(serviceName)) {
            if (SERVICE_PROCESS_COUNT_MAP.get(serviceName) == null) {
                SERVICE_PROCESS_COUNT_MAP.putIfAbsent(serviceName, new AtomicLong(1));
            } else {
                SERVICE_PROCESS_COUNT_MAP.get(serviceName).incrementAndGet();
            }
            // update in process
            RPC_IN_PROCESS.incrementAndGet();
            // update total
            TOTAL_RPC_PROCESS.incrementAndGet();
        }
    }

    public static void quit(String serviceName) {
        if (StringUtils.isNotEmpty(serviceName)) {
            if (SERVICE_PROCESS_COUNT_MAP.containsKey(serviceName)) {
                SERVICE_PROCESS_COUNT_MAP.get(serviceName).decrementAndGet();
                // update in process
                RPC_IN_PROCESS.decrementAndGet();
            }
        }
    }

    public static void complete(String serviceName) {
        quit(serviceName);
        // update complate process
        COMPLATE_RPC_PROCESS.incrementAndGet();
    }

    public static void getCount() {
        System.out.println("rpc in process count: " + RPC_IN_PROCESS + ";\n\r" + "complate process count: "
                + COMPLATE_RPC_PROCESS + ";\n\r" + "total rpc process count: " + TOTAL_RPC_PROCESS + ";");
    }
}
