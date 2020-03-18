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

package com.webank.ai.fate.serving.guest.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.adapter.processing.PostProcessing;
import com.webank.ai.fate.serving.adapter.processing.PreProcessing;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.federatedml.PipelineTask;
import com.webank.ai.fate.serving.interfaces.ModelManager;
import com.webank.ai.fate.serving.manager.InferenceWorkerManager;
import com.webank.ai.fate.serving.pojo.InferenceRequest;
import com.webank.ai.fate.serving.rpc.FederatedRpcInvoker;
import com.webank.ai.fate.serving.utils.InferenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

//@Service
public class DefaultGuestInferenceProvider implements GuestInferenceProvider, InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(DefaultGuestInferenceProvider.class);
    @Autowired
    ModelManager modelManager;
    @Autowired
    CacheManager cacheManager;
    private PostProcessing postProcessing;
    private PreProcessing preProcessing;

    FederatedRpcInvoker federatedRpcInvoker;



    private static void logInference(Context context, InferenceRequest inferenceRequest, ModelNamespaceData modelNamespaceData, ReturnResult inferenceResult, long elapsed, boolean getRemotePartyResult, boolean billing) {
        InferenceUtils.logInference(context, FederatedInferenceType.INITIATED, modelNamespaceData.getLocal(), modelNamespaceData.getRole(), inferenceRequest.getCaseid(), inferenceRequest.getSeqno(), inferenceResult.getRetcode(), elapsed, getRemotePartyResult, billing, new ObjectMapper().convertValue(inferenceRequest, HashMap.class), inferenceResult);
    }

    private static void logInference(Context context, Map<String, Object> federatedParams, FederatedParty federatedParty, FederatedRoles federatedRoles, ReturnResult inferenceResult, long elapsed, boolean getRemotePartyResult, boolean billing) {
        InferenceUtils.logInference(context, FederatedInferenceType.FEDERATED, federatedParty, federatedRoles, federatedParams.get(Dict.CASEID).toString(), federatedParams.get(Dict.SEQNO).toString(), inferenceResult.getRetcode(), elapsed, getRemotePartyResult, billing, federatedParams, inferenceResult);
    }

    public ReturnResult runInference(Context context, InferenceRequest inferenceRequest) {
        long startTime = System.currentTimeMillis();

        context.setCaseId(inferenceRequest.getCaseid());
        ReturnResult inferenceResult = new ReturnResult();
        inferenceResult.setCaseid(inferenceRequest.getCaseid());
        String modelName = inferenceRequest.getModelVersion();
        String modelNamespace = inferenceRequest.getModelId();
        String serviceId = inferenceRequest.getServiceId();
        context.setServiceId(serviceId);
        context.setApplyId(inferenceRequest.getApplyId());
        String modelKey = "";
        if (StringUtils.isEmpty(modelNamespace)&& StringUtils.isEmpty(modelName) ) {
            if(StringUtils.isNotEmpty(inferenceRequest.getServiceId())){
                modelKey = modelManager.getModelNamespaceByPartyId(context,inferenceRequest.getServiceId());
            }

            if (StringUtils.isEmpty(modelKey)) {
                inferenceResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED + 1000);
                return inferenceResult;
            }
            String[]  modelKeyElement = modelKey.split(":");
            Preconditions.checkArgument(modelKeyElement!=null&&modelKeyElement.length==2);
            modelName = modelKeyElement[1];
            modelNamespace = modelKeyElement[0];

//            else if(inferenceRequest.haveAppId()) {
//                modelKey = modelManager.getModelNamespaceByPartyId(context,inferenceRequest.getAppid());
//            }
        }



        ModelNamespaceData modelNamespaceData = modelManager.getModelNamespaceData(context,modelNamespace);
        PipelineTask model;
//        if (StringUtils.isEmpty(modelName)) {
//            modelName = modelNamespaceData.getUsedModelName();
//            model = modelNamespaceData.getUsedModel();
//        } else {
//            model = modelManager.getModel(context,modelName, modelNamespace);
//        }
        Preconditions.checkArgument(StringUtils.isNotEmpty(modelName));
        Preconditions.checkArgument(StringUtils.isNotEmpty(modelNamespace));
        Preconditions.checkArgument(modelNamespaceData!=null);
        model =  modelManager.getModel(context,modelName, modelNamespace);

        if (model == null) {
            inferenceResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED + 1000);
            return inferenceResult;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("use model to inference for {}, id: {}, version: {}", inferenceRequest.getAppid(), modelNamespace, modelName);
        }

        Map<String, Object> rawFeatureData = inferenceRequest.getFeatureData();

        if (rawFeatureData == null) {
            inferenceResult.setRetcode(InferenceRetCode.EMPTY_DATA + 1000);
            inferenceResult.setRetmsg("Can not parse data json.");
            return inferenceResult;
        }

        PreProcessingResult preProcessingResult;
        try {

            preProcessingResult = getPreProcessingFeatureData(context, rawFeatureData);
        } catch (Exception ex) {
            logger.error("feature data preprocessing failed", ex);
            inferenceResult.setRetcode(InferenceRetCode.INVALID_FEATURE + 1000);
            inferenceResult.setRetmsg(ex.getMessage());
            return inferenceResult;
        }
        Map<String, Object> featureData = preProcessingResult.getProcessingResult();
        Map<String, Object> featureIds = preProcessingResult.getFeatureIds();
        if (featureData == null) {
            inferenceResult.setRetcode(InferenceRetCode.NUMERICAL_ERROR + 1000);
            inferenceResult.setRetmsg("Can not preprocessing data");
            return inferenceResult;
        }
        Map<String, Object> predictParams = new HashMap<>(8);
        Map<String, Object> modelFeatureData = Maps.newHashMap(featureData);
        FederatedParams federatedParams = new FederatedParams();
        if(inferenceRequest.getSendToRemoteFeatureData()!=null&&federatedParams.getFeatureIdMap()!=null) {
            federatedParams.getFeatureIdMap().putAll(inferenceRequest.getSendToRemoteFeatureData());
        }
        federatedParams.setCaseId(inferenceRequest.getCaseid());
        federatedParams.setSeqNo(inferenceRequest.getSeqno());
        federatedParams.setLocal(modelNamespaceData.getLocal());
        federatedParams.setModelInfo(new ModelInfo(modelName, modelNamespace));
        federatedParams.setRole(modelNamespaceData.getRole());
        if(featureIds!=null&&featureIds.size()>0) {
            federatedParams.getFeatureIdMap().putAll(featureIds);
        }
        Map<String, Object> modelResult = model.predict(context, modelFeatureData, federatedParams);
        PostProcessingResult postProcessingResult;
        try {
            postProcessingResult = getPostProcessedResult(context, featureData, modelResult);
            inferenceResult = postProcessingResult.getProcessingResult();
        } catch (Exception ex) {
            logger.error("model result postprocessing failed", ex);
            if(inferenceResult!=null) {
                inferenceResult.setRetcode(InferenceRetCode.COMPUTE_ERROR);
                inferenceResult.setRetmsg(ex.getMessage());
            }
        }
        inferenceResult = handleResult(context, inferenceRequest, modelNamespaceData, inferenceResult);

        return inferenceResult;
    }

    private ReturnResult handleResult(Context context, InferenceRequest inferenceRequest, ModelNamespaceData modelNamespaceData, ReturnResult inferenceResult) {

        boolean getRemotePartyResult = (boolean) context.getDataOrDefault(Dict.GET_REMOTE_PARTY_RESULT, false);
        boolean billing = true;
        try {
            int partyInferenceRetcode = 0;
            inferenceResult.setCaseid(context.getCaseId());
            ReturnResult federatedResult = context.getFederatedResult();
            if (!getRemotePartyResult) {
                billing = false;
            } else if (federatedResult != null) {
                if (federatedResult.getRetcode() == InferenceRetCode.GET_FEATURE_FAILED || federatedResult.getRetcode() == InferenceRetCode.INVALID_FEATURE || federatedResult.getRetcode() == InferenceRetCode.NO_FEATURE) {
                    billing = false;
                }
                if (federatedResult.getRetcode() != 0) {
                    partyInferenceRetcode += 2;
                    inferenceResult.setRetcode(federatedResult.getRetcode());
                }
            }else{
                partyInferenceRetcode += 2;
            }
            if (inferenceResult.getRetcode() != 0&&partyInferenceRetcode==0) {
                partyInferenceRetcode += 1;
            }
            inferenceResult.setRetcode(inferenceResult.getRetcode() + partyInferenceRetcode * 1000);
            inferenceResult = postProcessing.handleResult(context, inferenceResult);
            return inferenceResult;
        } finally {
            long endTime = System.currentTimeMillis();
            long inferenceElapsed = endTime - context.getTimeStamp();
            logInference(context, inferenceRequest, modelNamespaceData, inferenceResult, inferenceElapsed, getRemotePartyResult, billing);

        }

    }

    private PreProcessingResult getPreProcessingFeatureData(Context context, Map<String, Object> originFeatureData) {
        long beginTime = System.currentTimeMillis();
        try {
            return preProcessing.getResult(context, ObjectTransform.bean2Json(originFeatureData));
        } finally {
            long endTime = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("preprocess caseid {} cost time {}", context.getCaseId(), endTime - beginTime);
            }
        }
    }

    private PostProcessingResult getPostProcessedResult(Context context, Map<String, Object> featureData, Map<String, Object> modelResult) {
        long beginTime = System.currentTimeMillis();
        try {
            return postProcessing.getResult(context, featureData, modelResult);
        } finally {
            long endTime = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("postprocess caseid {} cost time {}", context.getCaseId(), endTime - beginTime);
            }
        }
    }

    @Override
    public ReturnResult syncInference(Context context, InferenceRequest inferenceRequest) {
        long inferenceBeginTime = System.currentTimeMillis();
        ReturnResult cacheResult = getReturnResultFromCache(context, inferenceRequest);

        if (cacheResult != null) {
            return cacheResult;
        }

        ReturnResult inferenceResultFromCache = cacheManager.getInferenceResultCache(inferenceRequest.getAppid(), inferenceRequest.getCaseid());

        if (logger.isDebugEnabled()) {
            logger.debug("caseid {} query cache cost {}", inferenceRequest.getCaseid(), System.currentTimeMillis() - inferenceBeginTime);
        }
        if (inferenceResultFromCache != null) {
            logger.info("request caseId {} cost time {}  hit cache true", inferenceRequest.getCaseid(), System.currentTimeMillis() - inferenceBeginTime);
            return inferenceResultFromCache;
        }

        ReturnResult inferenceResult = runInference(context, inferenceRequest);
        if (inferenceResult != null && inferenceResult.getRetcode() == 0) {
            cacheManager.putInferenceResultCache(context, inferenceRequest.getAppid(), inferenceRequest.getCaseid(), inferenceResult);
        }

        return inferenceResult;
    }


    private ReturnResult getReturnResultFromCache(Context context, InferenceRequest inferenceRequest) {
        long inferenceBeginTime = System.currentTimeMillis();
        ReturnResult inferenceResultFromCache = cacheManager.getInferenceResultCache(inferenceRequest.getAppid(), inferenceRequest.getCaseid());

        if (logger.isDebugEnabled()) {
            logger.debug("caseid {} query cache cost {}", inferenceRequest.getCaseid(), System.currentTimeMillis() - inferenceBeginTime);
        }

        if (inferenceResultFromCache != null) {
            logger.info("request caseId {} cost time {}  hit cache true", inferenceRequest.getCaseid(), System.currentTimeMillis() - inferenceBeginTime);
        }
        return inferenceResultFromCache;
    }

    @Override
    public ReturnResult asynInference(Context context, InferenceRequest inferenceRequest) {
        long beginTime = System.currentTimeMillis();
        ReturnResult cacheResult = getReturnResultFromCache(context, inferenceRequest);
        if (cacheResult != null) {
            return cacheResult;
        }
        InferenceWorkerManager.exetute(new Runnable() {

            @Override
            public void run() {
                ReturnResult inferenceResult = null;
                Context subContext = context.subContext();
                subContext.preProcess();
                try {
                    subContext.setActionType(Dict.ACTION_TYPE_ASYNC_EXECUTE);
                    inferenceResult = runInference(subContext, inferenceRequest);
                    if (inferenceResult != null && inferenceResult.getRetcode() == 0) {
                        cacheManager.putInferenceResultCache(subContext, inferenceRequest.getAppid(), inferenceRequest.getCaseid(), inferenceResult);
                    }
                } catch (Throwable e) {
                    logger.error("asynInference error", e);
                } finally {
                    subContext.postProcess(inferenceRequest, inferenceResult);
                }
            }
        });
        ReturnResult startInferenceJobResult = new ReturnResult();
        startInferenceJobResult.setRetcode(InferenceRetCode.OK);
        startInferenceJobResult.setCaseid(inferenceRequest.getCaseid());
        return startInferenceJobResult;
    }

    @Override
    public ReturnResult getResult(Context context, InferenceRequest inferenceRequest) {

        ReturnResult cacheResult = this.getReturnResultFromCache(context, inferenceRequest);
        if (cacheResult != null) {
            return cacheResult;
        }
        ReturnResult noCacheInferenceResult = new ReturnResult();
        noCacheInferenceResult.setRetcode(InferenceRetCode.NO_RESULT);
        return noCacheInferenceResult;

    }

    @Override
    public ReturnResult batchInference(Context context, BatchInferenceRequest batchInferenceRequest) {

//
//
//        context.setCaseId(batchInferenceRequest.getSeqNo());
//        ReturnResult inferenceResult = new ReturnResult();
//        inferenceResult.setCaseid(batchInferenceRequest.getSeqNo());
//
//        String serviceId = batchInferenceRequest.getServiceId();
//        context.setServiceId(serviceId);
//        context.setApplyId(batchInferenceRequest.getApplyId());
//        String modelKey = "";
//
//        if(StringUtils.isNotEmpty(batchInferenceRequest.getServiceId())){
//        modelKey = modelManager.getModelNamespaceByPartyId(context,batchInferenceRequest.getServiceId());
//        }
//
////        if (StringUtils.isEmpty(modelKey)) {
////                inferenceResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED + 1000);
////                return inferenceResult;
////            }
//        String[]  modelKeyElement = modelKey.split(":");
//        Preconditions.checkArgument(modelKeyElement!=null&&modelKeyElement.length==2);
//        String modelName = modelKeyElement[1];
//        String modelNamespace = modelKeyElement[0];
//
//        ModelNamespaceData modelNamespaceData = modelManager.getModelNamespaceData(context,modelNamespace);
//        PipelineTask model;
//        Preconditions.checkArgument(StringUtils.isNotEmpty(modelName));
//        Preconditions.checkArgument(StringUtils.isNotEmpty(modelNamespace));
//        Preconditions.checkArgument(modelNamespaceData!=null);
//        model =  modelManager.getModel(context,modelName, modelNamespace);
//
////        if (model == null) {
////            inferenceResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED + 1000);
////            return inferenceResult;
////        }
//
//
//
//
//
//        Map<String, Object> rawFeatureData = inferenceRequest.getFeatureData();
//
//        if (rawFeatureData == null) {
//            inferenceResult.setRetcode(InferenceRetCode.EMPTY_DATA + 1000);
//            inferenceResult.setRetmsg("Can not parse data json.");
//            return inferenceResult;
//        }
//
//        PreProcessingResult preProcessingResult;
//        try {
//
//            preProcessingResult = getPreProcessingFeatureData(context, rawFeatureData);
//        } catch (Exception ex) {
//            logger.error("feature data preprocessing failed", ex);
//            inferenceResult.setRetcode(InferenceRetCode.INVALID_FEATURE + 1000);
//            inferenceResult.setRetmsg(ex.getMessage());
//            return inferenceResult;
//        }
//        Map<String, Object> featureData = preProcessingResult.getProcessingResult();
//        Map<String, Object> featureIds = preProcessingResult.getFeatureIds();
//        if (featureData == null) {
//            inferenceResult.setRetcode(InferenceRetCode.NUMERICAL_ERROR + 1000);
//            inferenceResult.setRetmsg("Can not preprocessing data");
//            return inferenceResult;
//        }
//        Map<String, Object> predictParams = new HashMap<>(8);
//        Map<String, Object> modelFeatureData = Maps.newHashMap(featureData);
//        FederatedParams federatedParams = new FederatedParams();
//        if(inferenceRequest.getSendToRemoteFeatureData()!=null&&federatedParams.getFeatureIdMap()!=null) {
//            federatedParams.getFeatureIdMap().putAll(inferenceRequest.getSendToRemoteFeatureData());
//        }
//        federatedParams.setCaseId(batchInferenceRequest.getSeqNo());
//        federatedParams.setSeqNo(batchInferenceRequest.getSeqNo());
//        federatedParams.setLocal(modelNamespaceData.getLocal());
//        federatedParams.setModelInfo(new ModelInfo(modelName, modelNamespace));
//        federatedParams.setRole(modelNamespaceData.getRole());
//        if(featureIds!=null&&featureIds.size()>0) {
//            federatedParams.getFeatureIdMap().putAll(featureIds);
//        }
//
//        FederatedParty srcParty = modelNamespaceData.getLocal();
//
//        FederatedParty dstParty = new FederatedParty(Dict.HOST, modelNamespaceData.getRole().getRole(Dict.HOST).get(0));
//=============
//
//        ModelInfo  modelInfo =new ModelInfo(modelName, modelNamespace);
//        HostFederatedParams hostFederatedParams = new HostFederatedParams();
//        hostFederatedParams.setCaseId(bat);
//        hostFederatedParams.setSeqNo(guestFederatedParams.getSeqNo());
//       // hostFederatedParams.getFeatureIdMap().putAll(guestFederatedParams.getFeatureIdMap());
//        hostFederatedParams.setBatchFeatureIdMapList();
//        hostFederatedParams.setBatch(true);
//        hostFederatedParams.setLocal(dstParty);
//        hostFederatedParams.setPartnerLocal(srcParty);
//        hostFederatedParams.setRole(modelNamespaceData.getRole());
//        hostFederatedParams.setPartnerModelInfo(modelInfo);
//       // hostFederatedParams.setData(guestFederatedParams.getData());
//
//
//        ==========
//
//        ListenableFuture<Proxy.Packet>  remoteFuture = federatedRpcInvoker.async(context,srcParty,dstParty,hostFederatedParams,"batch");
//
//        context.setRemoteFuture(remoteFuture);
//
//
//
//
//
//
//        Map<String, Object> modelResult = model.predict(context, modelFeatureData, federatedParams);
//
//        PostProcessingResult postProcessingResult;
//        try {
//            postProcessingResult = getPostProcessedResult(context, featureData, modelResult);
//            inferenceResult = postProcessingResult.getProcessingResult();
//        } catch (Exception ex) {
//            logger.error("model result postprocessing failed", ex);
//            if(inferenceResult!=null) {
//                inferenceResult.setRetcode(InferenceRetCode.COMPUTE_ERROR);
//                inferenceResult.setRetmsg(ex.getMessage());
//            }
//        }
//        inferenceResult = handleResult(context, inferenceRequest, modelNamespaceData, inferenceResult);

        return null;

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        try {
            String classPathPre = PostProcessing.class.getPackage().getName();
            String postClassPath = classPathPre + "." + Configuration.getProperty(Dict.POST_PROCESSING_CONFIG);
            postProcessing = (PostProcessing) InferenceUtils.getClassByName(postClassPath);
            String preClassPath = classPathPre + "." + Configuration.getProperty(Dict.PRE_PROCESSING_CONFIG);
            preProcessing = (PreProcessing) InferenceUtils.getClassByName(preClassPath);
        } catch (Throwable e) {
            logger.error("load post/pre processing error", e);
        }

    }
}
