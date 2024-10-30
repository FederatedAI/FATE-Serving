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

import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;

public interface Context<Req, Resp> {

    String LOGGER_NAME = "flow";

    void preProcess();

    Object getData(Object key);

    Object getDataOrDefault(Object key, Object defaultValue);

    void putData(Object key, Object data);

    String getCaseId();

    void setCaseId(String caseId);

    long getTimeStamp();

    default void postProcess(Req req, Resp resp) {

    }

    ReturnResult getFederatedResult();

    void setFederatedResult(ReturnResult returnResult);

    boolean isHitCache();

    void hitCache(boolean hitCache);

    Context subContext();

    String getInterfaceName();

    void setInterfaceName(String interfaceName);

    String getActionType();

    void setActionType(String actionType);

    String getSeqNo();

    long getCostTime();

    /**
     * proxy
     */
    GrpcType getGrpcType();

    void setGrpcType(GrpcType grpcType);

    String getVersion();

    void setVersion(String version);

    String getGuestAppId();

    void setGuestAppId(String guestAppId);

    String getHostAppid();

    void setHostAppid(String hostAppid);

    RouterInfo getRouterInfo();

    void setRouterInfo(RouterInfo routerInfo);

    Object getResultData();

    void setResultData(Object resultData);

    int getReturnCode();

    void setReturnCode(int returnCode);

    long getDownstreamCost();

    void setDownstreamCost(long downstreamCost);

    long getDownstreamBegin();

    void setDownstreamBegin(long downstreamBegin);

    long getRouteBasis();

    void setRouteBasis(long routeBasis);

    String getSourceIp();

    void setSourceIp(String sourceIp);

    String getServiceName();

    void setServiceName(String serviceName);

    String getCallName();

    void setCallName(String callName);

    String getServiceId();

    void setServiceId(String serviceId);

    String getApplyId();

    void setApplyId(String applyId);

    ListenableFuture getRemoteFuture();

    void setRemoteFuture(ListenableFuture future);

    String getResourceName();

}
