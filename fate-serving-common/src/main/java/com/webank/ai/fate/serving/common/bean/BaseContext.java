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

package com.webank.ai.fate.serving.common.bean;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class BaseContext<Req, Resp> implements Context<Req, Resp> {
    private static final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);
    public static AtomicLong requestInProcess = new AtomicLong(0);
    protected long timestamp;
    protected String interfaceName;
    protected String actionType;
    protected Map dataMap = Maps.newHashMap();
    long costTime;
    String resourceName;

    @Override
    public boolean isNeedDispatch() {
        return needDispatch;
    }

    @Override
    public void setNeedDispatch(boolean needDispatch) {
        this.needDispatch = needDispatch;
    }

    @Override
    public String getOriginService() {
        return dataMap.get(Dict.ORIGIN_SERVICE).toString();
    }

    @Override
    public void setOriginService(String service) {
        dataMap.put(Dict.ORIGIN_SERVICE, service);
    }

    protected  boolean  needDispatch=false;

    public BaseContext() {
        timestamp = System.currentTimeMillis();
    }

    public BaseContext(String actionType) {

        timestamp = System.currentTimeMillis();
        this.actionType = actionType;
    }

    private BaseContext(long timestamp, Map dataMap) {
        this.timestamp = timestamp;
        this.dataMap = dataMap;
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
            requestInProcess.addAndGet(1);
        } catch (Exception e) {
            logger.error("preProcess error", e);
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
    public String getTraceId() {
        return (String) dataMap.get(Dict.TRACEID);
    }

    @Override
    public void setTraceId(String traceId) {
        dataMap.put(Dict.TRACEID, traceId);
    }

    @Override
    public long getTimeStamp() {
        return timestamp;
    }

    @Override
    public void postProcess(Req req, Resp resp) {
        try {
            requestInProcess.decrementAndGet();
            costTime = System.currentTimeMillis() - timestamp;
        } catch (Throwable e) {
            logger.error("postProcess error", e);
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
        return new BaseContext(this.timestamp, newDataMap);
    }

    @Override
    public String getInterfaceName() {
        return interfaceName;
    }

    @Override
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public String getSeqNo() {
        return (String) this.dataMap.getOrDefault(Dict.REQUEST_SEQNO, "");
    }

    @Override
    public long getCostTime() {
        return costTime;
    }

    @Override
    public GrpcType getGrpcType() {
        return (GrpcType) dataMap.get(Dict.GRPC_TYPE);
    }

    @Override
    public void setGrpcType(GrpcType grpcType) {
        dataMap.put(Dict.GRPC_TYPE, grpcType);
    }

    @Override
    public String getVersion() {
        return (String) dataMap.get(Dict.VERSION);
    }

    @Override
    public void setVersion(String version) {
        dataMap.put(Dict.VERSION, version);
    }

    @Override
    public String getGuestAppId() {
        return (String) dataMap.get(Dict.GUEST_APP_ID);
    }

    @Override
    public void setGuestAppId(String guestAppId) {
        dataMap.put(Dict.GUEST_APP_ID, guestAppId);
    }

    @Override
    public String getHostAppid() {
        return (String) dataMap.get(Dict.HOST_APP_ID);
    }

    @Override
    public void setHostAppid(String hostAppid) {
        dataMap.put(Dict.HOST_APP_ID, hostAppid);
    }

    @Override
    public RouterInfo getRouterInfo() {
        return (RouterInfo) dataMap.get(Dict.ROUTER_INFO);
    }

    @Override
    public void setRouterInfo(RouterInfo routerInfo) {
        dataMap.put(Dict.ROUTER_INFO, routerInfo);
    }

    @Override
    public Object getResultData() {
        return dataMap.get(Dict.RESULT_DATA);
    }

    @Override
    public void setResultData(Object resultData) {
        dataMap.put(Dict.RESULT_DATA, resultData);
    }

    @Override
    public int getReturnCode() {
        return (int) dataMap.get(Dict.RETURN_CODE);
    }

    @Override
    public void setReturnCode(int returnCode) {
        dataMap.put(Dict.RETURN_CODE, returnCode);
    }

    @Override
    public long getDownstreamCost() {
        if (dataMap.get(Dict.DOWN_STREAM_COST) != null) {

            return (long) dataMap.get(Dict.DOWN_STREAM_COST);
        }
        return 0;
    }

    @Override
    public void setDownstreamCost(long downstreamCost) {
        dataMap.put(Dict.DOWN_STREAM_COST, downstreamCost);
    }

    @Override
    public long getDownstreamBegin() {
        return  dataMap.get(Dict.DOWN_STREAM_BEGIN)!=null? (long) dataMap.get(Dict.DOWN_STREAM_BEGIN):0;
    }

    @Override
    public void setDownstreamBegin(long downstreamBegin) {
        dataMap.put(Dict.DOWN_STREAM_BEGIN, downstreamBegin);
    }

    @Override
    public long getRouteBasis() {
        return dataMap.get(Dict.ROUTE_BASIS)!=null?(long) dataMap.get(Dict.ROUTE_BASIS):0;
    }

    @Override
    public void setRouteBasis(long routeBasis) {
        dataMap.put(Dict.ROUTE_BASIS, routeBasis);
    }

    @Override
    public String getSourceIp() {
        return (String) dataMap.get(Dict.SOURCE_IP);
    }

    @Override
    public void setSourceIp(String sourceIp) {
        dataMap.put(Dict.SOURCE_IP, sourceIp);
    }

    @Override
    public String getServiceName() {
        return (String) dataMap.get(Dict.SERVICE_NAME);
    }

    @Override
    public void setServiceName(String serviceName) {
        dataMap.put(Dict.SERVICE_NAME, serviceName);
    }

    @Override
    public String getCallName() {
        return (String) dataMap.get(Dict.CALL_NAME);
    }

    @Override
    public void setCallName(String callName) {
        dataMap.put(Dict.CALL_NAME, callName);
    }

    @Override
    public String getServiceId() {
        return (String) this.dataMap.getOrDefault(Dict.SERVICE_ID, "");
    }

    @Override
    public void setServiceId(String serviceId) {
        dataMap.put(Dict.SERVICE_ID, serviceId);
    }

    @Override
    public String getApplyId() {
        return (String) this.dataMap.getOrDefault(Dict.APPLY_ID, "");
    }

    @Override
    public void setApplyId(String applyId) {
        dataMap.put(Dict.APPLY_ID, applyId);
    }

    @Override
    public ListenableFuture getRemoteFuture() {
        return (ListenableFuture) this.dataMap.getOrDefault(Dict.FUTURE, null);
    }

    @Override
    public void setRemoteFuture(ListenableFuture future) {
        this.dataMap.put(Dict.FUTURE, future);
    }

    @Override
    public String getResourceName() {
        if (StringUtils.isNotEmpty(resourceName)) {
            return resourceName;
        } else {
            resourceName = "I_" + (StringUtils.isNotEmpty(this.getActionType()) ? this.getActionType() : this.getServiceName());
        }
        return resourceName;
    }

}
