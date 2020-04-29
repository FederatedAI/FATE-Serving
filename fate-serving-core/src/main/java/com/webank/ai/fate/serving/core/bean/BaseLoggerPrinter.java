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

package com.webank.ai.fate.serving.core.bean;


import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BaseLoggerPrinter implements LoggerPrinter<Object, ReturnResult> {


    static final String LOGGER_NAME = "flow";

    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    @Override
    public void printLog(Context context, Object req, ReturnResult resp) {
//
//        logger.info("{}|{}|{}|{}|{}|{}|{}|{}", NetUtils.getLocalIp(), context.getSeqNo(), Dict.NONE, context.getActionType(), context.getCostTime(),
//                resp != null ? resp.getRetcode() : Dict.NONE, req, resp);


    }
}
