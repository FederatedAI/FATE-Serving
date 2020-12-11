package com.webank.ai.fate.serving.redirect;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.common.rpc.core.RequestRedirector;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.exceptions.ModelNullException;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class HostRequestRedirector implements RequestRedirector<Proxy.Packet, Proxy.Packet> {

    @Autowired(required = false)
    RouterService routerService;

    Logger logger = LoggerFactory.getLogger(HostRequestRedirector.class);

    @Override
    public Proxy.Packet redirect(Context context, Proxy.Packet packet, String serviceName) {
        if(routerService!=null) {
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

        }else{
            throw new ModelNullException("serviceId "+context.getServiceId()+" is not exist in this node");
        }
    }
}
