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

package com.webank.ai.fate.serving.admin.bean;

public enum  VerifyService {

//    PUBLISH_LOAD("publishLoad"),
//    PUBLISH_BIND("publishBind"),
//    PUBLISH_ONLINE("publishOnline"),
    INFERENCE("inference"),
    BATCH_INFERENCE("batchInference");

    private String callName;

    public String getCallName() {
        return callName;
    }

    public void setCallName(String callName) {
        this.callName = callName;
    }

    VerifyService(String callName) {
        this.callName = callName;
    }

    public static boolean contains(String callName) {
        VerifyService[] values = VerifyService.values();
        for (VerifyService value : values) {
            if (value.getCallName().equals(callName)) {
                return true;
            }
        }
        return false;
    }
}
