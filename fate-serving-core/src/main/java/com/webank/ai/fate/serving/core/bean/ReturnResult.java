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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ReturnResult {
    private static final Logger logger = LoggerFactory.getLogger(ReturnResult.class);
    private String retcode;
    private String retmsg = "";
    private String caseid = "";
    private Map<String, Object> data;
    private Map<String, Object> log;
    private Map<String, Object> warn;

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    private int flag;

    public ReturnResult(){
        this.data = new HashMap<>();
        this.log = new HashMap<>();
        this.warn = new HashMap<>();
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getRetcode() {
        return retcode;
    }

    public void setRetmsg(String retmsg) {
        this.retmsg = retmsg;
    }

    public String getRetmsg() {
        return retmsg;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setLog(Map<String, Object> log) {
        this.log = log;
    }

    public Map<String, Object> getLog() {
        return log;
    }

    public void setWarn(Map<String, Object> warn) {
        this.warn = warn;
    }

    public Map<String, Object> getWarn() {
        return warn;
    }

    public void setCaseid(String caseid) {
        this.caseid = caseid;
    }

    public String getCaseid() {
        return caseid;
    }

    @Override
    public String toString(){
        String  result= "";

        return  result;


    }
}
