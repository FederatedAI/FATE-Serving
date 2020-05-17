package com.webank.ai.fate.serving.common.interceptors;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
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
    FlowCounterManager  flowCounterManager;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        
        flowCounterManager.pass(context.getServiceName());

    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

    }
}
