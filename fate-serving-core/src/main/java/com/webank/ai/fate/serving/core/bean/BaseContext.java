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

import com.google.common.collect.Maps;
import com.webank.ai.fate.core.bean.ReturnResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.util.Map;


public class BaseContext<Req, Resp extends ReturnResult> implements Context<Req, Resp> {

    private static final Logger LOGGER = LogManager.getLogger(LOGGER_NAME);
    public static ApplicationContext applicationContext;
    long timestamp;
    LoggerPrinter loggerPrinter;
    String actionType;
    Map dataMap = Maps.newHashMap();

    public BaseContext(LoggerPrinter loggerPrinter) {
        this.loggerPrinter = loggerPrinter;
        timestamp = System.currentTimeMillis();
    }

    private BaseContext(LoggerPrinter loggerPrinter, long timestamp, Map dataMap) {
        this.timestamp = timestamp;
        this.dataMap = dataMap;
        this.loggerPrinter = loggerPrinter;
    }

    @Override
    public String getActionType() {
        return actionType;
    }

    @Override
    public void setActionType(String actionType) {
        this.actionType = actionType;

    }

    @Override
    public void preProcess() {

        //WatchDog.enter(this);

    }

    @Override
    public Object getData(Object key) {
        return null;
    }

    @Override
    public Object getDataOrDefault(Object key, Object defaultValue) {
        return dataMap.getOrDefault(key, defaultValue);
    }

    @Override
    public void putData(Object key, Object data) {

        dataMap.put(key, data);

    }

    @Override
    public String getCaseId() {
        if (dataMap.get(Dict.CASEID) != null) {
            return dataMap.get(Dict.CASEID).toString();
        } else {
            return null;
        }
    }

    @Override
    public void setCaseId(String caseId) {
        dataMap.put(Dict.CASEID, caseId);
    }

    @Override
    public long getTimeStamp() {
        return timestamp;
    }

    @Override
    public void postProcess(Req req, Resp resp) {


        try {

            //    WatchDog.quit(this);

            if (loggerPrinter != null) {
                loggerPrinter.printLog(this, req, resp);
            }

        } catch (Throwable e) {


        }
    }

    @Override
    public ReturnResult getFederatedResult() {
        return (ReturnResult) dataMap.get(Dict.FEDERATED_RESULT);
    }

    @Override
    public void setFederatedResult(ReturnResult returnResult) {
        dataMap.put(Dict.FEDERATED_RESULT, returnResult);
    }

    @Override
    public boolean isHitCache() {
        return (Boolean) dataMap.getOrDefault(Dict.HIT_CACHE, false);
    }

    @Override
    public void hitCache(boolean hitCache) {
        dataMap.put(Dict.HIT_CACHE, hitCache);
    }

    @Override
    public Context subContext() {

        Map newDataMap = Maps.newHashMap(dataMap);

        return new BaseContext(this.loggerPrinter, this.timestamp, dataMap);
    }

    @Override
    public String getSeqNo() {
        return (String) this.dataMap.getOrDefault(Dict.REQUEST_SEQNO, "");
    }

    @Override
    public long getCostTime() {
        return System.currentTimeMillis() - timestamp;
    }
}
