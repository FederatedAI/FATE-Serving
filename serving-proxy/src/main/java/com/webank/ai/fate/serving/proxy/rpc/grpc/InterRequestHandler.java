

package com.webank.ai.fate.serving.proxy.rpc.grpc;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.proxy.rpc.core.Context;
import com.webank.ai.fate.serving.proxy.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.proxy.rpc.core.ProxyServiceRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InterRequestHandler extends ProxyRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(InterRequestHandler.class);

    @Autowired
    ProxyServiceRegister proxyServiceRegister;

    public ProxyServiceRegister getProxyServiceRegister(){
        return proxyServiceRegister;
    }

    public void setExtraInfo(Context  context, InboundPackage<Proxy.Packet> inboundPackage, Proxy.Packet req){
        context.setGrpcType(GrpcType.INTER_GRPC);
    }


}