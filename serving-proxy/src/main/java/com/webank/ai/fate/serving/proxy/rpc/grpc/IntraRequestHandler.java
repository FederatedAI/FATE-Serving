

package com.webank.ai.fate.serving.proxy.rpc.grpc;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.proxy.rpc.core.Context;
import com.webank.ai.fate.serving.proxy.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.proxy.rpc.core.ProxyServiceRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IntraRequestHandler extends ProxyRequestHandler {
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    ProxyServiceRegister proxyServiceRegister;

    public ProxyServiceRegister getProxyServiceRegister(){
        return proxyServiceRegister;
    }

    public void setExtraInfo(Context  context, InboundPackage<Proxy.Packet> inboundPackage, Proxy.Packet req){
        context.setGrpcType(GrpcType.INTRA_GRPC);
    }


}