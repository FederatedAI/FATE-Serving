package com.webank.ai.fate.serving.rpc;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.core.rpc.sink.Protocol;
import com.webank.ai.fate.serving.core.rpc.sink.Sender;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Future;


@Service
@Protocol(name = "grpc" )
public class GrpcSender implements Sender<Proxy.Packet  ,Proxy.Packet> {
    @Autowired
    RouterService  routerService;

    @Override
    public Future<Proxy.Packet> async(Context context, Proxy.Packet packet) {
    //    String address = this.route();
        RouterInfo routerInfo = context.getRouterInfo();
        GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
        Preconditions.checkArgument(StringUtils.isNotEmpty(routerInfo.toString()));
        ManagedChannel channel1 = grpcConnectionPool.getManagedChannel(routerInfo.toString());
        DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(channel1);
        context.setDownstreamBegin(System.currentTimeMillis());
        ListenableFuture<Proxy.Packet> future = stub1.unaryCall(packet);
        return  future;
    }

    private String route() {
        boolean routerByzk = MetaInfo.PROPERTY_USE_ZK_ROUTER;
        String address = null;
        if (!routerByzk) {
            address = MetaInfo.PROPERTY_PROXY_ADDRESS;
        } else {
            List<URL> urls = routerService.router(Dict.PROPERTY_PROXY_ADDRESS, Dict.ONLINE_ENVIRONMENT, Dict.UNARYCALL);
            if (urls != null && urls.size() > 0) {
                URL url = urls.get(0);
                String ip = url.getHost();
                int port = url.getPort();
                address = ip + ":" + port;
            }
        }
        return address;
    }
}
