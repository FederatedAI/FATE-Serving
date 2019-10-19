/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.federatedml.model;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.core.utils.Configuration;
import com.webank.ai.fate.core.utils.ObjectTransform;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.utils.ProtobufUtils;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class BaseModel implements Predictor<List<Map<String, Object>>, FederatedParams, Map<String, Object>> {

    private static final Logger LOGGER = LogManager.getLogger();
    public static RouterService routerService;
    protected String componentName;

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public abstract int initModel(byte[] protoMeta, byte[] protoParam);

    protected <T> T parseModel(com.google.protobuf.Parser<T> protoParser, byte[] protoString) throws com.google.protobuf.InvalidProtocolBufferException {
        return ProtobufUtils.parseProtoObject(protoParser, protoString);
    }

    @Override
    public Map<String, Object> predict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {

        long beginTime = System.currentTimeMillis();
        try {
            this.preprocess(context, inputData, predictParams);
            Map<String, Object> result = handlePredict(context, inputData, predictParams);
            result = this.postprocess(context, inputData, predictParams, result);
            return result;
        } finally {
            long endTime = System.currentTimeMillis();
            long cost = endTime - beginTime;
            String caseId = context.getCaseId();
            String className = this.getClass().getSimpleName();
            LOGGER.info("model {} caseid {} predict cost time {}", className, caseId, cost);
        }


    }

    ;

    @Override
    public void preprocess(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {

    }


    @Override
    public Map<String, Object> postprocess(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams, Map<String, Object> res) {

        return res;

    }

    public abstract Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams);


    protected ReturnResult getFederatedPredict(Context context, FederatedParams guestFederatedParams, String remoteMethodName, boolean useCache) {
        ReturnResult remoteResult = null;

        try {
            FederatedParty srcParty = guestFederatedParams.getLocal();
            FederatedRoles federatedRoles = guestFederatedParams.getRole();
            Map<String, Object> featureIds = (Map<String, Object>) guestFederatedParams.getFeatureIdMap();

            //TODO: foreach
            FederatedParty dstParty = new FederatedParty(Dict.HOST, federatedRoles.getRole(Dict.HOST).get(0));
            if (useCache) {
                ReturnResult remoteResultFromCache = CacheManager.getInstance().getRemoteModelInferenceResult(dstParty, federatedRoles, featureIds);
                if (remoteResultFromCache != null) {
                    LOGGER.info("caseid {} get remote party model inference result from cache.", context.getCaseId());
                    //federatedParams.put("getRemotePartyResult", false);
                    context.putData(Dict.GET_REMOTE_PARTY_RESULT, false);
                    context.hitCache(true);
                    remoteResult = remoteResultFromCache;
                    return remoteResult;
                }
            }


            HostFederatedParams hostFederatedParams = new HostFederatedParams();


            hostFederatedParams.setCaseId(guestFederatedParams.getCaseId());
            hostFederatedParams.setSeqNo(guestFederatedParams.getSeqNo());
            hostFederatedParams.setFeatureIdMap(guestFederatedParams.getFeatureIdMap());
            hostFederatedParams.setLocal(dstParty);
            hostFederatedParams.setPartnerLocal(srcParty);
            hostFederatedParams.setRole(federatedRoles);
            hostFederatedParams.setPartnerModelInfo(guestFederatedParams.getModelInfo());
            hostFederatedParams.setData(guestFederatedParams.getData());


//        Map<String, Object> requestData = new HashMap<>();
//        Arrays.asList("caseid", "seqno").forEach((field -> {
//            requestData.put(field, federatedParams.get(field));
//        }));
//        requestData.put("partner_local", ObjectTransform.bean2Json(srcParty));
//        requestData.put("partner_model_info", ObjectTransform.bean2Json(federatedParams.get("model_info")));
//        requestData.put("feature_id", ObjectTransform.bean2Json(federatedParams.get("feature_id")));
//        requestData.put("local", ObjectTransform.bean2Json(dstParty));
//        requestData.put("role", ObjectTransform.bean2Json(federatedParams.get("role")));
//        federatedParams.put("getRemotePartyResult", true);
            context.putData(Dict.GET_REMOTE_PARTY_RESULT, true);
            remoteResult = getFederatedPredictFromRemote(context, srcParty, dstParty, hostFederatedParams, remoteMethodName);
            if (useCache) {
                CacheManager.getInstance().putRemoteModelInferenceResult(dstParty, federatedRoles, featureIds, remoteResult);
                LOGGER.info("caseid {} get remote party model inference result from federated request.", context.getCaseId());
            }
            return remoteResult;
        } finally {
            context.setFederatedResult(remoteResult);
        }
    }

    protected ReturnResult getFederatedPredictFromRemote(Context context, FederatedParty srcParty, FederatedParty dstParty, HostFederatedParams hostFederatedParams, String remoteMethodName) {


        long beginTime = System.currentTimeMillis();
        ReturnResult remoteResult = null;
        try {

            Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();

//            packetBuilder.setBody(Proxy.Data.newBuilder()
//                    .setValue(ByteString.copyFrom(ObjectTransform.bean2Json(requestData).getBytes()))
//                    .build());


            packetBuilder.setBody(Proxy.Data.newBuilder()
                    .setValue(ByteString.copyFrom(JSON.toJSONBytes(hostFederatedParams)))
                    .build());

            Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

            metaDataBuilder.setSrc(
                    topicBuilder.setPartyId(String.valueOf(srcParty.getPartyId())).
                            setRole(Configuration.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                            .setName(Dict.PARTNER_PARTY_NAME)
                            .build());
            metaDataBuilder.setDst(
                    topicBuilder.setPartyId(String.valueOf(dstParty.getPartyId()))
                            .setRole(Configuration.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
                            .setName(Dict.PARTY_NAME)
                            .build());
            metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName(remoteMethodName).build());
            metaDataBuilder.setConf(Proxy.Conf.newBuilder().setOverallTimeout(60 * 1000));
            String version =  Configuration.getProperty(Dict.VERSION,"");
            metaDataBuilder.setOperator(Configuration.getProperty(Dict.VERSION,""));
            packetBuilder.setHeader(metaDataBuilder.build());
            GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
            String routerByZkString = Configuration.getProperty(Dict.USE_ZK_ROUTER, Dict.FALSE);
            boolean routerByzk = Boolean.valueOf(routerByZkString);
            String address = null;
            if (!routerByzk) {
                address = Configuration.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
            } else {

                URL paramUrl = URL.valueOf(Dict.PROPERTY_PROXY_ADDRESS + "/" + Dict.ONLINE_ENVIROMMENT + "/" + Dict.UNARYCALL);
                URL newUrl =paramUrl.addParameter(Constants.VERSION_KEY,version);
                List<URL> urls = routerService.router(newUrl);
                if (urls.size() > 0) {
                    URL url = urls.get(0);
                    String ip = url.getAddress();
                    int port = url.getPort();
                    address = ip + ":" + port;
                }
            }
            ManagedChannel channel1 = grpcConnectionPool.getManagedChannel(address);
            try {

                DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(channel1);
                Proxy.Packet packet = stub1.unaryCall(packetBuilder.build());
                remoteResult = (ReturnResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), ReturnResult.class);
            } finally {
                grpcConnectionPool.returnPool(channel1, address);
            }

            return remoteResult;
        } catch (Exception e) {
            LOGGER.error("getFederatedPredictFromRemote error", e);
            throw new RuntimeException(e);
        } finally {
            long end = System.currentTimeMillis();
            long cost = end - beginTime;
            LOGGER.info("caseid {} getFederatedPredictFromRemote cost {} remote retcode {}", context.getCaseId(), cost, remoteResult != null ? remoteResult.getRetcode() : Dict.NONE);
        }

    }


    public  static  void main(String[] args){


        NettyChannelBuilder builder = NettyChannelBuilder
                .forAddress("localhost", 8000)
                .keepAliveTime(6, TimeUnit.MINUTES)
                .keepAliveTimeout(1, TimeUnit.HOURS)
                .keepAliveWithoutCalls(true)
                .idleTimeout(1, TimeUnit.HOURS)
                .perRpcBufferLimit(128 << 20)
                .flowControlWindow(32 << 20)
                .maxInboundMessageSize(32 << 20)
                .enableRetry()
                .retryBufferSize(16 << 20)
                .maxRetryAttempts(20);      // todo: configurable


        builder.negotiationType(NegotiationType.PLAINTEXT)
                .usePlaintext();
        Proxy.Packet.Builder  packetBuilder =Proxy.Packet.newBuilder();
            DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(builder.build());
            Proxy.Packet packet = stub1.unaryCall(packetBuilder.build());
        ReturnResult  remoteResult = (ReturnResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), ReturnResult.class);



    }





}
