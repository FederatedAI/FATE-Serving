package com.webank.ai.fate.serving.guest.provider;

import com.google.common.collect.Lists;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ServingServerContext;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public abstract class AbstractServingServiceProvider<req, resp> extends AbstractServiceAdaptor<req, resp> implements EnvironmentAware {

    Environment environment;

    @Override
    protected resp transformExceptionInfo(Context context, ExceptionInfo  exceptionInfo) {
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
        } catch (IllegalAccessException e) {
            throw new SysException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new SysException(e.getMessage());
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
