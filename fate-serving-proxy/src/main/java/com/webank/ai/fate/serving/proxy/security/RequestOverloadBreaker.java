package com.webank.ai.fate.serving.proxy.security;

import com.webank.ai.fate.serving.common.flow.FlowCounterManager;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.exceptions.OverLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class RequestOverloadBreaker implements Interceptor {
    Logger logger = LoggerFactory.getLogger(RequestOverloadBreaker.class);

    @Autowired
    FlowCounterManager flowCounterManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        String resource = context.getResourceName();
        boolean pass = flowCounterManager.pass(resource, 1);
        if (!pass) {
            flowCounterManager.block(context.getServiceName(), 1);
            logger.warn("request was block by over load, service name: {}", context.getServiceName());
            throw new OverLoadException("request was block by over load, service name: " + context.getServiceName());
        }
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

    }
}
