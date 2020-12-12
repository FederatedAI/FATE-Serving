package com.webank.ai.fate.serving.redirect;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.RequestRedirector;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.BaseException;
import com.webank.ai.fate.serving.core.exceptions.HostModelNullException;
import com.webank.ai.fate.serving.core.exceptions.ModelNullException;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HostRequestRedirector
        extends AbstractServingServiceProvider<Proxy.Packet, Proxy.Packet>  {

    @Autowired(required = false)
    RouterService routerService;

    private static final String MODEL_KEY_SEPARATOR = "&";

    static Logger logger = LoggerFactory.getLogger(HostRequestRedirector.class);

    public static String genModelKey(String tableName, String namespace) {
        return StringUtils.join(Arrays.asList(tableName, namespace), MODEL_KEY_SEPARATOR);
    }

    public static String getModelRouteKey(Context context, Proxy.Packet packet) {
        String namespace;
        String tableName;
        if (StringUtils.isBlank(context.getVersion()) || Double.parseDouble(context.getVersion()) < 200) {
            // version 1.x
            String data = packet.getBody().getValue().toStringUtf8();
            Map hostFederatedParams = JsonUtil.json2Object(data, Map.class);
            Map partnerModelInfo = (Map) hostFederatedParams.get("partnerModelInfo");
            namespace = partnerModelInfo.get("namespace").toString();
            tableName = partnerModelInfo.get("name").toString();
        } else {
            // version 2.0.0+
            Proxy.Model model = packet.getHeader().getTask().getModel();
            namespace = model.getNamespace();
            tableName = model.getTableName();
        }

        String key = genModelKey(tableName, namespace);
        logger.info("get model route key by version: {} namespace: {} tablename: {}, key: {}", context.getVersion(), namespace, tableName, key);

        return EncryptUtils.encrypt(key, EncryptMethod.MD5);
    }



    @Override
    public Proxy.Packet doService(Context context, InboundPackage<Proxy.Packet> inboundPackage, OutboundPackage<Proxy.Packet> outboundPackage) {
        Proxy.Packet  packet = inboundPackage.getBody();
        String  key = getModelRouteKey(context, packet);
        if(routerService==null) {
               throw new HostModelNullException("mode key  "+key+" is not exist in this cluster");
        }
        List<URL> urls = routerService.router(Dict.SERVICE_SERVING, key, serviceName);
        RouterInfo  routerInfo=null;
        if(CollectionUtils.isNotEmpty(urls)) {
              String ip = urls.get(0).getIp();
              int port = urls.get(0).getPort();
              routerInfo =  new RouterInfo();
          }else{
              throw new HostModelNullException("mode key  "+key+" is not exist in this cluster");
          }
        ManagedChannel channel = GrpcConnectionPool.getPool().getManagedChannel(routerInfo.getHost(),routerInfo.getPort());
        DataTransferServiceGrpc.DataTransferServiceFutureStub futureStub = DataTransferServiceGrpc.newFutureStub(channel);
        ListenableFuture< Proxy.Packet > resultFuture = futureStub.unaryCall(packet);
        String resultString = null;
        try {
            Proxy.Packet  result = resultFuture.get(MetaInfo.PROPERTY_GRPC_TIMEOUT, TimeUnit.MILLISECONDS);
            return result;
        } catch (Exception e) {
            throw new RemoteRpcException("remote rpc exception");
        }
    }

    @Override
    protected OutboundPackage<Proxy.Packet> serviceFailInner(Context context, InboundPackage<Proxy.Packet> data, Throwable e) {
        OutboundPackage<Proxy.Packet> outboundPackage = new OutboundPackage<Proxy.Packet>();
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
        Proxy.Packet.Builder  resultBuilder =       data.getBody().toBuilder();
        resultBuilder.setBody(Proxy.Data.newBuilder().setValue(ByteString.copyFrom(JsonUtil.object2Json(returnResult).getBytes())));
        outboundPackage.setData(resultBuilder.build());
        return outboundPackage;
    }
}
