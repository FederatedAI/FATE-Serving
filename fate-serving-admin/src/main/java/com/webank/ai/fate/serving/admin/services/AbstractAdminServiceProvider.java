package com.webank.ai.fate.serving.admin.services;

import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.exceptions.UnSupportMethodException;

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

    }
}
