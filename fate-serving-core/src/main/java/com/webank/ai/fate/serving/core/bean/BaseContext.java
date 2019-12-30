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

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Map;


public class BaseContext<Req, Resp extends ReturnResult> implements Context<Req, Resp> {
    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);
    public static ApplicationContext applicationContext;
    long timestamp;
    LoggerPrinter loggerPrinter;
    String actionType;
    Map dataMap = Maps.newHashMap();
    Timer.Context  timerContext;
    long  costTime;
    MetricRegistry  metricRegistry;
    public BaseContext(){

    }

    public BaseContext(LoggerPrinter loggerPrinter,String  actionType,MetricRegistry metricRegistry) {
        this.loggerPrinter = loggerPrinter;
        this.metricRegistry =  metricRegistry;
        timestamp = System.currentTimeMillis();
        this.actionType = actionType;
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
        try {
            Timer timer = metricRegistry.timer(actionType+"_timer");
            Counter counter = metricRegistry.counter(actionType+"_couter");
            counter.inc();
            timerContext = timer.time();
        }catch(Exception e){
            logger.error("preProcess error" ,e);

        }
    }

    @Override
    public Object getData(Object key) {
        return dataMap.get(key);
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
            if(timerContext!=null){
               costTime = timerContext.stop()/1000000;
            }else{
                costTime = System.currentTimeMillis() -  timestamp;
            }
            if (loggerPrinter != null) {
                loggerPrinter.printLog(this, req, resp);
            }

        } catch (Throwable e) {

            logger.error("postProcess error" ,e);
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
        return  costTime;
    }



}
