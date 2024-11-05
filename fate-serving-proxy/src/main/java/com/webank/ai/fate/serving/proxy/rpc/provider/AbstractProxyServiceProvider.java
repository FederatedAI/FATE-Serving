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

package com.webank.ai.fate.serving.proxy.rpc.provider;

import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.exceptions.UnSupportMethodException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public abstract class AbstractProxyServiceProvider<req, resp> extends AbstractServiceAdaptor<req, resp> {

    private static final Logger logger  = LoggerFactory.getLogger(AbstractProxyServiceProvider.class);

    @Override
    protected resp transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        return null;
    }

    @Override
    protected resp doService(Context context, InboundPackage<req> data, OutboundPackage<resp> outboundPackage) throws InvocationTargetException, IllegalAccessException {
        resp result;
        Map<String, Method> methodMap = this.getMethodMap();
        String actionType = context.getActionType();
        try {
            Method method = methodMap.get(actionType);
            if (method == null) {
                throw new UnSupportMethodException();
            }
            result = (resp) method.invoke(this, context, data);
        } catch (Throwable e) {
            logger.error("Error processing request for caseId: {}, actionType: {}, error: {}", context.getCaseId(), actionType, e.getMessage());
            throw e;
        }
        return result;
    }

    @Override
    protected void printFlowLog(Context context) {
        flowLogger.info("{}|{}|" +
                        "{}|{}|{}|{}|",
                context.getCaseId(), context.getReturnCode(), context.getCostTime(),
                context.getDownstreamCost(), serviceName, context.getRouterInfo() != null ? context.getRouterInfo() : "");
    }
}
