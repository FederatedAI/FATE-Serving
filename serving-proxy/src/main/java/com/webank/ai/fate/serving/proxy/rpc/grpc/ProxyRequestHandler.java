

package com.webank.ai.fate.serving.proxy.rpc.grpc;

import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.core.bean.BaseContext;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.ServiceAdaptor;
import com.webank.ai.fate.serving.metrics.api.IMetricFactory;
import com.webank.ai.fate.serving.proxy.rpc.core.ProxyServiceRegister;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class ProxyRequestHandler extends DataTransferServiceGrpc.DataTransferServiceImplBase {

    @Autowired
    IMetricFactory metricFactory;

    private static final Logger logger = LoggerFactory.getLogger(ProxyRequestHandler.class);

    public abstract ProxyServiceRegister getProxyServiceRegister();

    public abstract void setExtraInfo(Context context, InboundPackage<Proxy.Packet> inboundPackage, Proxy.Packet req);

    @RegisterService(serviceName = "unaryCall")
    @Override
    public void unaryCall(Proxy.Packet req, StreamObserver<Proxy.Packet> responseObserver)  {

        metricFactory.counter("grpc.unaryCall.request", "grpc unaryCall request",
                "src", req.getHeader().getSrc().getPartyId(), "dst", req.getHeader().getDst().getPartyId()).increment();

        if (logger.isDebugEnabled()) {
            logger.debug("unaryCall req {}", req);
        }
        ServiceAdaptor unaryCallService = getProxyServiceRegister().getServiceAdaptor("unaryCall");
        Context context  =  new BaseContext();
        InboundPackage<Proxy.Packet> inboundPackage = buildInboundPackage(context, req);
        setExtraInfo(context, inboundPackage, req);

        metricFactory.counter("grpc.unaryCall", "grpc unaryCall","direction", "request", "grpc.type", context.getGrpcType().toString()).increment();

        OutboundPackage<Proxy.Packet> outboundPackage = null;
        try {
            outboundPackage = unaryCallService.service(context,inboundPackage);
        } catch (Exception e) {
            e.printStackTrace();

        }
        Proxy.Packet   result = (Proxy.Packet)outboundPackage.getData();
        responseObserver.onNext(result);
        responseObserver.onCompleted();

        metricFactory.counter("grpc.unaryCall.response", "grpc unaryCall response",
                "src", req.getHeader().getSrc().getPartyId(), "dst", req.getHeader().getDst().getPartyId()).increment();
        metricFactory.counter("grpc.unaryCall", "grpc unaryCall", "direction", "response", "grpc.type", context.getGrpcType().toString()).increment();
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