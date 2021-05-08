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

package com.webank.ai.fate.serving.admin.services;

import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.exceptions.UnSupportMethodException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public abstract class AbstractAdminServiceProvider<req, resp> extends AbstractServiceAdaptor<req, resp> {

    @Override
    protected resp doService(Context context, InboundPackage<req> data, OutboundPackage<resp> outboundPackage) {
        Map<String, Method> methodMap = this.getMethodMap();
        String actionType = context.getActionType();
        resp result;
        try {
            Method method = methodMap.get(actionType);
            if (method == null) {
                throw new UnSupportMethodException();
            }
            result = (resp) method.invoke(this, context, data);
        } catch (Throwable e) {
            e.printStackTrace();
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

    }
}
