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
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;

import java.util.concurrent.Future;

public interface Context<Req, Resp> {

    static final String LOGGER_NAME = "flow";

    public void preProcess();

    public Object getData(Object key);

    public Object getDataOrDefault(Object key, Object defaultValue);

    public void putData(Object key, Object data);

    public String getCaseId();

    public void setCaseId(String caseId);

    public long getTimeStamp();

    public default void postProcess(Req req, Resp resp) {
    }

    public ReturnResult getFederatedResult();

    public void setFederatedResult(ReturnResult returnResult);

    public boolean isHitCache();

    public void hitCache(boolean hitCache);

    public Context subContext();

    public String getInterfaceName();

    public void setInterfaceName(String interfaceName);

    public String getActionType();

    public void setActionType(String actionType);

    public String getSeqNo();

    public long getCostTime();

    /**
     * proxy
     */
    public GrpcType getGrpcType();

    public void setGrpcType(GrpcType grpcType);

    public String getVersion();

    public void setVersion(String version);

    public String getGuestAppId();

    public void setGuestAppId(String guestAppId);

    public String getHostAppid();

    public void setHostAppid(String hostAppid);

    public RouterInfo getRouterInfo();

    public void setRouterInfo(RouterInfo routerInfo);

    public Object getResultData();

    public void setResultData(Object resultData);

    public String getReturnCode();

    public void setReturnCode(String returnCode);

    public long getDownstreamCost();

    public void setDownstreamCost(long downstreamCost);

    public long getDownstreamBegin();

    public void setDownstreamBegin(long downstreamBegin);

    public long getRouteBasis();

    public void setRouteBasis(long routeBasis);

    public String getSourceIp();

    public void setSourceIp(String sourceIp);

    public String getServiceName();

    public void setServiceName(String serviceName);

    public void setCallName(String callName);

    public String getCallName();

    public String getServiceId();

    public void setServiceId(String serviceId);

    public String getApplyId();

    public void setApplyId(String applyId);

    public ListenableFuture getRemoteFuture();

    public void  setRemoteFuture(ListenableFuture  future);

    public Model getModel();

    public void  setModel(Model model);

    

}
