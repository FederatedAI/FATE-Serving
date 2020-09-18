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
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.exceptions.UnSupportMethodException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public abstract class AbstractServingServiceProvider<req, resp> extends AbstractServiceAdaptor<req, resp> {

    final String baseLogString = "{}|{}|{}|{}|{}|{}|{}|{}";

    @Override
    protected resp transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        return null;
    }

    @Override
    protected resp doService(Context context, InboundPackage<req> data, OutboundPackage<resp> outboundPackage) {
        Map<String, Method> methodMap = this.getMethodMap();
        String actionType = context.getActionType();
        resp result = null;
        try {
            Method method = methodMap.get(actionType);
            if (method == null) {
                throw new UnSupportMethodException();
            }
            result = (resp) method.invoke(this, context, data);
        } catch (Throwable e) {
            if (e.getCause() != null && e.getCause() instanceof BaseException) {
                BaseException baseException = (BaseException) e.getCause();
                throw baseException;
            } else if (e instanceof InvocationTargetException) {
                InvocationTargetException ex = (InvocationTargetException) e;
                throw new SysException(ex.getTargetException().getMessage());
            } else {
                throw new SysException(e.getMessage());
            }
        }
        return result;
    }

    @Override
    protected void printFlowLog(Context context) {

        flowLogger.info(baseLogString,

                context.getCaseId(), context.getReturnCode(), context.getCostTime(),
                context.getDownstreamCost(), serviceName, context.getRouterInfo() != null ? context.getRouterInfo() : "NO_ROUTER_INFO",
                MetaInfo.PROPERTY_PRINT_INPUT_DATA ? context.getData(Dict.INPUT_DATA) : "",
                MetaInfo.PROPERTY_PRINT_OUTPUT_DATA ? context.getData(Dict.OUTPUT_DATA) : ""
        );
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

    protected void postProcess(Context context, ReturnResult returnResult) {
        Model model = ((ServingServerContext) context).getModel();
        context.setReturnCode(returnResult.getRetcode());
        if (model != null) {
            Map<String, Object> data = returnResult.getData();
            if (data == null) {
                data = Maps.newHashMap();
            }
            data.put(Dict.MODEL_ID, model.getNamespace());
            data.put(Dict.MODEL_VERSION, model.getTableName());
            data.put(Dict.TIMESTAMP, model.getTimestamp());
            returnResult.setData(data);
        }
    }

}
