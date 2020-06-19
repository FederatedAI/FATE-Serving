package com.webank.ai.fate.serving.sdk.client;


import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
//import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
//import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Client {
    RouterService routerService;
    GrpcConnectionPool  grpcConnectionPool =  GrpcConnectionPool.getPool();
    final static String PROJECT ="serving";
    final String SINGLE_INFERENCE = "inference";

    private  Client(RouterService  routerService){
        this.routerService = routerService;
    }

    public static synchronized Client getClient(String  zkAddress){
        if(clientMap.get(zkAddress)==null){
            ZookeeperRegistry  zookeeperRegistry = ZookeeperRegistry.getRegistry(zkAddress,"client","online",12202);
            zookeeperRegistry.subProject(PROJECT);
            DefaultRouterService  defaultRouterService = new DefaultRouterService();
            defaultRouterService.setRegistry(zookeeperRegistry);
            Client  client = new  Client(defaultRouterService);
            clientMap.put(zkAddress,client);
        }
       return clientMap.get(zkAddress);
    }

    public static  Client getClient(String  zkAddress,boolean useAcl,String aclUserName,String aclPassword){

        if(useAcl) {
            System.setProperty("acl.enable", "true");
        }
        else {
            System.setProperty("acl.enable", "false");
        }
        System.setProperty("acl.username", aclUserName!=null?aclUserName:"");
        System.setProperty("acl.password", aclPassword!=null?aclPassword:"");
        return getClient(zkAddress);
    }

    private  static Map<String,Client>  clientMap = new ConcurrentHashMap<>();

    public ReturnResult singleInference(InferenceRequest  inferenceRequest) throws Exception{
        Preconditions.checkArgument(inferenceRequest!=null);
        Preconditions.checkArgument(StringUtils.isNotEmpty(inferenceRequest.getServiceId()));
        List<URL> urls = routerService.router(PROJECT,inferenceRequest.getServiceId(),SINGLE_INFERENCE);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(urls));
        ManagedChannel managedChannel = null;
        URL url = urls.get(0);
        managedChannel = grpcConnectionPool.getManagedChannel(url.getIp(), url.getPort());
        inferenceRequest.getSendToRemoteFeatureData().putAll(inferenceRequest.getFeatureData());
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();
        String contentString = JsonUtil.object2Json(inferenceRequest);
        inferenceMessageBuilder.setBody(ByteString.copyFrom(contentString, "UTF-8"));
        InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        InferenceServiceProto.InferenceMessage result = blockingStub.inference(inferenceMessage);
        Preconditions.checkArgument(result!=null);
        Preconditions.checkArgument(result.getBody()!=null);
        ReturnResult  returnResult = JsonUtil.json2Object(result.getBody().toByteArray(),ReturnResult.class);
        return  returnResult;
    }





    public BatchInferenceResult batchInference(BatchInferenceRequest batchInferenceRequest) throws Exception{
        Preconditions.checkArgument(batchInferenceRequest!=null);
        Preconditions.checkArgument(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId()));
        List<URL> urls = routerService.router(PROJECT,batchInferenceRequest.getServiceId(),SINGLE_INFERENCE);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(urls));
        ManagedChannel managedChannel = null;
        URL url = urls.get(0);
        managedChannel = grpcConnectionPool.getManagedChannel(url.getIp(), url.getPort());
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();
        String contentString = JsonUtil.object2Json(batchInferenceRequest);
        inferenceMessageBuilder.setBody(ByteString.copyFrom(contentString, "UTF-8"));
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
        InferenceServiceProto.InferenceMessage result = blockingStub.batchInference(inferenceMessageBuilder.build());
        Preconditions.checkArgument(result!=null);
        Preconditions.checkArgument(result.getBody()!=null);
        BatchInferenceResult  returnResult = JsonUtil.json2Object(result.getBody().toByteArray(),BatchInferenceResult.class);
        return  returnResult;
    }

}
