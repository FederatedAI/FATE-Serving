

package com.webank.ai.fate.serving.proxy.rpc.grpc;

import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.proxy.common.Dict;
import com.webank.ai.fate.serving.proxy.rpc.core.*;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProxyRequestHandler extends DataTransferServiceGrpc.DataTransferServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(ProxyRequestHandler.class);

    public abstract ProxyServiceRegister getProxyServiceRegister();

    public abstract void setExtraInfo(Context  context, InboundPackage<Proxy.Packet> inboundPackage, Proxy.Packet req);

    @RegisterService(serviceName = "unaryCall")
    @Override
    public void unaryCall(Proxy.Packet req, StreamObserver<Proxy.Packet> responseObserver)  {

        logger.info("unaryCall req {}",req);
        ServiceAdaptor unaryCallService = getProxyServiceRegister().getServiceAdaptor("unaryCall");
        Context context  =  new Context();
        InboundPackage<Proxy.Packet> inboundPackage = buildInboundPackage(context, req);
        setExtraInfo(context, inboundPackage, req);

        OutboundPackage<Proxy.Packet>   outboundPackage = null;
        try {
            outboundPackage = unaryCallService.service(context,inboundPackage);
        } catch (Exception e) {
            e.printStackTrace();

        }
        Proxy.Packet   result = (Proxy.Packet)outboundPackage.getData();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    public InboundPackage<Proxy.Packet> buildInboundPackage(Context  context, Proxy.Packet req){
        context.setCaseId(Long.toString(System.currentTimeMillis()));
        context.setVersion(req.getAuth().getVersion());
        if(StringUtils.isEmpty(context.getVersion())){
            context.setVersion(Dict.DEFAULT_VERSION);
        }
        context.setGuestAppId(req.getHeader().getSrc().getPartyId());
        context.setHostAppid(req.getHeader().getDst().getPartyId());

        InboundPackage<Proxy.Packet> inboundPackage = new InboundPackage<Proxy.Packet>();
        inboundPackage.setBody(req);

        return inboundPackage;
    }
}