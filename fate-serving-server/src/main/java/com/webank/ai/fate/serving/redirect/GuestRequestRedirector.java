package com.webank.ai.fate.serving.redirect;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.common.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.RequestRedirector;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.ModelNullException;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.channels.Channel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GuestRequestRedirector extends AbstractServingServiceProvider<InferenceServiceProto.InferenceMessage,
        InferenceServiceProto.InferenceMessage> {
    @Autowired(required = false)
    RouterService routerService;

    Logger logger = LoggerFactory.getLogger(GuestRequestRedirector.class);

    public InferenceServiceProto.InferenceMessage doService(Context context, InboundPackage<InferenceServiceProto.InferenceMessage> inboundPackage, OutboundPackage<InferenceServiceProto.InferenceMessage> outboundPackage) {

       InferenceServiceProto.InferenceMessage inferenceMessage =  inboundPackage.getBody();
        if(routerService==null) {
            throw new ModelNullException("serviceId {} is not exist in this node");
        }
        List<URL> urls = routerService.router(Dict.SERVICE_SERVING, context.getServiceId(), serviceName);
        if(CollectionUtils.isNotEmpty(urls)) {
            String ip = urls.get(0).getIp();
            int port = urls.get(0).getPort();
            ManagedChannel channel = GrpcConnectionPool.getPool().getManagedChannel(ip,port);
            InferenceServiceGrpc.InferenceServiceFutureStub futureStub = InferenceServiceGrpc.newFutureStub(channel);
            Preconditions.checkArgument(context.getData(Dict.ORIGINAL_REQUEST_DATA) != null);
            ListenableFuture<InferenceServiceProto.InferenceMessage> resultFuture = futureStub.inference((InferenceServiceProto.InferenceMessage) context.getData(Dict.ORIGINAL_REQUEST_DATA));
            String resultString = null;
            try {
                InferenceServiceProto.InferenceMessage result = resultFuture.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
                return result;
            } catch (Exception e) {
                throw new RemoteRpcException("remote rpc exception");
            }
        }else{
            throw new ModelNullException("serviceId "+context.getServiceId()+" is not exist in this cluster");
        }
    }



    @Override
    protected OutboundPackage<InferenceServiceProto.InferenceMessage> serviceFailInner(Context context, InboundPackage<InferenceServiceProto.InferenceMessage> data, Throwable e) {
        OutboundPackage<InferenceServiceProto.InferenceMessage> outboundPackage = new OutboundPackage<InferenceServiceProto.InferenceMessage>();
        ReturnResult  returnResult = new ReturnResult();
        if (e instanceof BaseException) {
            BaseException baseException = (BaseException) e;
            returnResult.setRetcode(baseException.getRetcode());
            returnResult.setRetmsg(e.getMessage());
        } else {
            returnResult.setRetcode(StatusCode.SYSTEM_ERROR);
            returnResult.setRetmsg(e.getMessage());
        }
        context.setReturnCode(returnResult.getRetcode());
        InferenceServiceProto.InferenceMessage  resultInferenceMessage =InferenceServiceProto.InferenceMessage.newBuilder().setBody(ByteString.copyFrom(JsonUtil.object2Json(returnResult).getBytes())).build();
                outboundPackage.setData(resultInferenceMessage);
        return outboundPackage;
    }
}
