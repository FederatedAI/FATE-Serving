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

package com.webank.ai.fate.serving.monitor.bean;




public class ReturnResult<T> {

    private static final int SUCCESS_CODE = 0;
    private static final int FAILURE_CODE = -1;

    private int retcode;

    private String retmsg;

    private T data;

    public ReturnResult(int retcode, String retmsg) {
        this.retcode = retcode;
        this.retmsg = retmsg;
    }

    public ReturnResult(int retcode, String retmsg, T data) {
        this.retcode = retcode;
        this.retmsg = retmsg;
        this.data = data;
    }

    public static ReturnResult success() {
        return new ReturnResult(SUCCESS_CODE, Dict.SUCCESS);
    }

    public static <T> ReturnResult success(T data) {
        return new ReturnResult(SUCCESS_CODE, Dict.SUCCESS, data);
    }

    public static <T> ReturnResult success(int retcode, String retmsg, T data) {
        return new ReturnResult(retcode, retmsg, data);
    }

    public static <T> ReturnResult success(T data, String retmsg) {
        return new ReturnResult(SUCCESS_CODE, retmsg, data);
    }

    public static ReturnResult failure() {
        return new ReturnResult(FAILURE_CODE, Dict.FAILED);
    }

    public static ReturnResult failure(int retcode, String retmsg) {
        return new ReturnResult(retcode, retmsg);
    }
}
