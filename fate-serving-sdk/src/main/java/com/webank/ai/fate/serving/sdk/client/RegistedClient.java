package com.webank.ai.fate.serving.sdk.client;


import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class RegistedClient extends  SimpleClient{

    protected RouterService routerService;

    private  String  registerAddress;

    public RegistedClient(String address){
        Preconditions.checkArgument(StringUtils.isNotBlank(address), "register address is blank");
        this.registerAddress= address;
        ZookeeperRegistry zookeeperRegistry = ZookeeperRegistry.createRegistry(address, ClIENT_PROJECT, ENVIRONMENT, port);
        zookeeperRegistry.subProject(PROJECT);
        DefaultRouterService defaultRouterService = new DefaultRouterService();
        defaultRouterService.setRegistry(zookeeperRegistry);
        this.routerService = defaultRouterService;

    }



    public ReturnResult singleInference(InferenceRequest inferenceRequest, int timeout) throws Exception {
        Preconditions.checkArgument(inferenceRequest != null, "inferenceRequest is null");
        Preconditions.checkArgument(this.routerService != null, "router service is null, please call setRegistryAddress(String address) to config registry");
        Preconditions.checkArgument(StringUtils.isNotEmpty(inferenceRequest.getServiceId()));
        List<URL> urls = routerService.router(PROJECT, inferenceRequest.getServiceId(), SINGLE_INFERENCE);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(urls), "serviceId " + inferenceRequest.getServiceId() + " found no url in zk");
        URL url = urls.get(0);
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(inferenceRequest), "UTF-8"));
        InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = this.getInferenceServiceBlockingStub(url.getHost(), url.getPort(),timeout);
        InferenceServiceProto.InferenceMessage result = blockingStub.inference(inferenceMessage);
        Preconditions.checkArgument(result != null && result.getBody() != null);
        return JsonUtil.deserializeJsonBytes(result.getBody().toByteArray(), ReturnResult.class);
    }

    public BatchInferenceResult batchInference(BatchInferenceRequest batchInferenceRequest) throws Exception {
        return batchInference(batchInferenceRequest,timeout);
    }


    public BatchInferenceResult batchInference(BatchInferenceRequest batchInferenceRequest,int timeout) throws Exception {
        Preconditions.checkArgument(batchInferenceRequest != null, "batchInferenceRequest is null");
        Preconditions.checkArgument(this.routerService != null, "router service is null, please call setRegistryAddress(String address) to config registry");
        Preconditions.checkArgument(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId()));
        List<URL> urls = routerService.router(PROJECT, batchInferenceRequest.getServiceId(), BATCH_INFERENCE);
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(urls), "serviceId " + batchInferenceRequest.getServiceId() + " found no url in zk");
        URL url = urls.get(0);
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(batchInferenceRequest), "UTF-8"));
        InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = this.getInferenceServiceBlockingStub(url.getHost(), url.getPort(),timeout);
        InferenceServiceProto.InferenceMessage result = blockingStub.batchInference(inferenceMessageBuilder.build());
        Preconditions.checkArgument(result != null && result.getBody() != null);
        return JsonUtil.deserializeJsonBytes(result.getBody().toByteArray(), BatchInferenceResult.class);
    }

    public ReturnResult singleInference(InferenceRequest inferenceRequest) throws Exception {
        return  singleInference(inferenceRequest,timeout);
    }

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }
}
