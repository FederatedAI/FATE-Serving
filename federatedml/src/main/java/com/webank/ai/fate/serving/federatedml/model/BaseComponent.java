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

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.cache.Cache;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.model.LocalInferenceAware;
import com.webank.ai.fate.serving.core.rpc.core.ErrorMessageUtil;
import com.webank.ai.fate.serving.core.rpc.core.FederatedRpcInvoker;
import com.webank.ai.fate.serving.core.utils.ProtobufUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseComponent implements LocalInferenceAware {

    protected static final int OK = 0;
    protected static final int UNKNOWNERROR = 1;
    protected static final int PARAMERROR = 2;
    protected static final int ILLEGALDATA = 3;
    protected static final int NOMODEL = 4;
    protected static final int NOTME = 5;
    protected static final int FEDERATEDERROR = 6;
    protected static final int TIMEOUT = -1;
    protected static final int NOFILE = -2;
    protected static final int NETWORKERROR = -3;
    protected static final int IOERROR = -4;
    protected static final int RUNTIMEERROR = -5;
    private static final Logger logger = LoggerFactory.getLogger(BaseComponent.class);
    protected String componentName;
    protected String shortName;
    protected int index;
    protected FederatedRpcInvoker<Proxy.Packet> federatedRpcInvoker;
    protected Cache cache;

    public abstract int initModel(byte[] protoMeta, byte[] protoParam);

    protected <T> T parseModel(com.google.protobuf.Parser<T> protoParser, byte[] protoString) throws com.google.protobuf.InvalidProtocolBufferException {
        return ProtobufUtils.parseProtoObject(protoParser, protoString);
    }


    protected Map<String, Object> handleRemoteReturnData(Map<String, Object> hostData) {
        Map<String, Object> result = new HashMap<>(8);
        result.put(Dict.RET_CODE, InferenceRetCode.OK);
        hostData.forEach((partId, partyDataObject) -> {
            Map partyData = (Map) partyDataObject;
            if (partyData.get(Dict.RET_CODE) != null && !StatusCode.SUCCESS.equals(partyData.get(Dict.RET_CODE))) {
                String remoteCode = partyData.get(Dict.RET_CODE).toString();
                String remoteMsg = partyData.get(Dict.MESSAGE) != null ? partyData.get(Dict.MESSAGE).toString() : "";
                String errorMsg = ErrorMessageUtil.buildRemoteRpcErrorMsg(remoteCode, remoteMsg);
                String retcode = ErrorMessageUtil.transformRemoteErrorCode(remoteCode);
                result.put(Dict.RET_CODE, retcode);
                result.put(Dict.MESSAGE, errorMsg);
                return;
            }

        });
        return result;
    }


//    @Override
//    public Map<String, Object> predict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {
//
//        long beginTime = System.currentTimeMillis();
//        try {
//            this.preprocess(context, inputData, predictParams);
//            Map<String, Object> result = handlePredict(context, inputData, predictParams);
//            result = this.postprocess(context, inputData, predictParams, result);
//            return result;
//        } finally {
//            long endTime = System.currentTimeMillis();
//            long cost = endTime - beginTime;
//            String caseId = context.getCaseId();
//            String className = this.getClass().getSimpleName();
//            if(logger.isDebugEnabled()) {
//                logger.debug("model {} caseid {} predict cost time {}", className, caseId, cost);
//            }
//        }
//
//    }


//    @Override
//    public void preprocess(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {
//
//    }
//
//
//    @Override
//    public Map<String, Object> postprocess(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams, Map<String, Object> res) {
//
//        return res;
//
//    }

//    public abstract Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams);


//    protected ReturnResult getFederatedPredict(Context context, FederatedParams guestFederatedParams, String remoteMethodName, boolean useCache) {
//        ReturnResult remoteResult = null;
//
//        try {
//            FederatedParty srcParty = guestFederatedParams.getLocal();
//            FederatedRoles federatedRoles = guestFederatedParams.getRole();
//            Map<String, Object> featureIds = (Map<String, Object>) guestFederatedParams.getFeatureIdMap();
//            FederatedParty dstParty = new FederatedParty(Dict.HOST, federatedRoles.getRole(Dict.HOST).get(0));
//            if (useCache) {
//                ReturnResult remoteResultFromCache = CacheManager.getInstance().getRemoteModelInferenceResult(guestFederatedParams);
//                if (remoteResultFromCache != null) {
//                    if(logger.isDebugEnabled()) {
//                        logger.debug("caseid {} get remote party model inference result from cache", context.getCaseId());
//                    }
//                    context.putData(Dict.GET_REMOTE_PARTY_RESULT, false);
//                    context.hitCache(true);
//                    remoteResult = remoteResultFromCache;
//                    return remoteResult;
//                }
//            }
//            HostFederatedParams hostFederatedParams = new HostFederatedParams();
//            hostFederatedParams.setCaseId(guestFederatedParams.getCaseId());
//            hostFederatedParams.setSeqNo(guestFederatedParams.getSeqNo());
//            hostFederatedParams.getFeatureIdMap().putAll(guestFederatedParams.getFeatureIdMap());
//            hostFederatedParams.setLocal(dstParty);
//            hostFederatedParams.setPartnerLocal(srcParty);
//            hostFederatedParams.setRole(federatedRoles);
//            hostFederatedParams.setPartnerModelInfo(guestFederatedParams.getModelInfo());
//            hostFederatedParams.setData(guestFederatedParams.getData());
//            context.putData(Dict.GET_REMOTE_PARTY_RESULT, true);
//            remoteResult = getFederatedPredictFromRemote(context, srcParty, dstParty, hostFederatedParams, remoteMethodName);
//            if (useCache&& remoteResult!=null&&remoteResult.getRetcode()==0) {
//                CacheManager.getInstance().putRemoteModelInferenceResult(guestFederatedParams, remoteResult);
//                if(logger.isDebugEnabled()) {
//                    logger.info("caseid {} get remote party model inference result from federated request.", context.getCaseId());
//                }
//            }
//            return remoteResult;
//        } finally {
//            context.setFederatedResult(remoteResult);
//        }
//    }

//    protected ReturnResult getFederatedPredictFromRemote(Context context, FederatedParty srcParty, FederatedParty dstParty, HostFederatedParams hostFederatedParams, String remoteMethodName) {
//
//
//        long beginTime = System.currentTimeMillis();
//        ReturnResult remoteResult = null;
//        try {
//
//            Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
//            packetBuilder.setBody(Proxy.Data.newBuilder()
//                    .setValue(ByteString.copyFrom(JSON.toJSONBytes(hostFederatedParams)))
//                    .build());
//
//            Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
//            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();
//
//            metaDataBuilder.setSrc(
//                    topicBuilder.setPartyId(String.valueOf(srcParty.getPartyId())).
//                            setRole(Configuration.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
//                            .setName(Dict.PARTNER_PARTY_NAME)
//                            .build());
//            metaDataBuilder.setDst(
//                    topicBuilder.setPartyId(String.valueOf(dstParty.getPartyId()))
//                            .setRole(Configuration.getProperty(Dict.PROPERTY_SERVICE_ROLE_NAME, Dict.PROPERTY_SERVICE_ROLE_NAME_DEFAULT_VALUE))
//                            .setName(Dict.PARTY_NAME)
//                            .build());
//            metaDataBuilder.setCommand(Proxy.Command.newBuilder().setName(remoteMethodName).build());
//            metaDataBuilder.setConf(Proxy.Conf.newBuilder().setOverallTimeout(60 * 1000));
//            String version =  Configuration.getProperty(Dict.VERSION,"");
//            metaDataBuilder.setOperator(Configuration.getProperty(Dict.VERSION,""));
//            packetBuilder.setHeader(metaDataBuilder.build());
//			Proxy.AuthInfo.Builder authBuilder = Proxy.AuthInfo.newBuilder();
//			if(context.getCaseId()!=null) {
//                authBuilder.setNonce(context.getCaseId());
//            }
//            if(version!=null) {
//                authBuilder.setVersion(version);
//            }
//            if(context.getServiceId()!=null) {
//                authBuilder.setServiceId(  context.getServiceId());
//            }
//            if(context.getApplyId()!=null) {
//                authBuilder.setApplyId(  context.getApplyId());
//            }
//            packetBuilder.setAuth(authBuilder.build());
//
//            GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
//            String routerByZkString = Configuration.getProperty(Dict.USE_ZK_ROUTER, Dict.FALSE);
//            boolean routerByzk = Boolean.valueOf(routerByZkString);
//            String address = null;
//            if (!routerByzk) {
//                address = Configuration.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
//            } else {
//
//                URL paramUrl = URL.valueOf(Dict.PROPERTY_PROXY_ADDRESS + "/" + Dict.ONLINE_ENVIROMMENT + "/" + Dict.UNARYCALL);
//                URL newUrl =paramUrl.addParameter(Constants.VERSION_KEY,version);
//                List<URL> urls = routerService.router(newUrl);
//                if (urls!=null&&urls.size() > 0) {
//                    URL url = urls.get(0);
//                    String ip = url.getHost();
//                    int port = url.getPort();
//                    address = ip + ":" + port;
//                }
//            }
//
//
//            logger.info("send to remote address {}",address);
//            Preconditions.checkArgument(StringUtils.isNotEmpty(address));
//            ManagedChannel channel1 = grpcConnectionPool.getManagedChannel(address);
//            ListenableFuture<Proxy.Packet> future= null;
//
//                //DataTransferServiceGrpc.DataTransferServiceBlockingStub stub1 = DataTransferServiceGrpc.newBlockingStub(channel1);
//            DataTransferServiceGrpc.DataTransferServiceFutureStub stub1 = DataTransferServiceGrpc.newFutureStub(channel1);
//            future =stub1.unaryCall(packetBuilder.build());
//
//            if(future!=null){
//                Proxy.Packet packet = future.get(configMap.getOrDefault("rpc.time.out",3000), TimeUnit.MILLISECONDS);
//                remoteResult = (ReturnResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), ReturnResult.class);
//            }
//            return remoteResult;
//        } catch (Exception e) {
//            logger.error("getFederatedPredictFromRemote error", e.getMessage());
//            throw new RuntimeException(e);
//        } finally {
//            long end = System.currentTimeMillis();
//            long cost = end - beginTime;
//            logger.info("caseid {} getFederatedPredictFromRemote cost {} remote retcode {}", context.getCaseId(), cost, remoteResult != null ? remoteResult.getRetcode() : Dict.NONE);
//        }
//
//    }


    /*public  static  void main(String[] args){


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

    }*/


    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }


    public FederatedRpcInvoker getFederatedRpcInvoker() {
        return federatedRpcInvoker;
    }

    public void setFederatedRpcInvoker(FederatedRpcInvoker federatedRpcInvoker) {
        this.federatedRpcInvoker = federatedRpcInvoker;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        this.cache = cache;
    }

//    public Map getConfigMap() {
//        return configMap;
//    }
//
//    public void setConfigMap(Map configMap) {
//        this.configMap = configMap;
//    }


}
