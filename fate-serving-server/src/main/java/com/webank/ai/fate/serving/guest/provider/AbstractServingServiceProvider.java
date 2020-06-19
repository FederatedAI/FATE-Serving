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

package com.webank.ai.fate.serving.guest.provider;

import com.google.common.collect.Lists;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public abstract class AbstractServingServiceProvider<req, resp> extends AbstractServiceAdaptor<req, resp> implements EnvironmentAware {

    Environment environment;

    @Override
    protected resp transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        return null;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    protected resp doService(Context context, InboundPackage<req> data, OutboundPackage<resp> outboundPackage) {
        Map<String, Method> methodMap = this.getMethodMap();
        String actionType = context.getActionType();
        Method method = methodMap.get(actionType);
        resp result = null;
        try {
            result = (resp) method.invoke(this, context, data);
        } catch (Throwable e) {
            if (e.getCause() != null && e.getCause() instanceof BaseException) {
                BaseException baseException = (BaseException) e.getCause();
                throw baseException;
            } else {
                throw new SysException(e.getMessage());
            }
        }
        return result;
    }

    @Override
    protected void printFlowLog(Context context) {

        flowLogger.info("{}|{}|" +
                        "{}|{}|{}|{}|" +
                        "{}|{}",
                context.getCaseId(), context.getReturnCode(), context.getCostTime(),
                context.getDownstreamCost(), serviceName, context.getRouterInfo() != null ? context.getRouterInfo() : "NO_ROUTER_INFO");
    }

    protected List<FederatedRpcInvoker.RpcDataWraper> buildRpcDataWraper(Context context, String methodName, Object data) {
        List<FederatedRpcInvoker.RpcDataWraper> result = Lists.newArrayList();
        Model model = ((ServingServerContext) context).getModel();
        Map<String, Model> hostModelMap = model.getFederationModelMap();
        hostModelMap.forEach((partId, hostModel) -> {
            FederatedRpcInvoker.RpcDataWraper rpcDataWraper = new FederatedRpcInvoker.RpcDataWraper();
            rpcDataWraper.setGuestModel(model);
            rpcDataWraper.setHostModel(hostModel);
            rpcDataWraper.setRemoteMethodName(methodName);
            rpcDataWraper.setData(data);
            result.add(rpcDataWraper);
        });

        return result;
    }

}
