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

package com.webank.ai.fate.serving.federatedml;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.core.mlmodel.buffer.PipelineProto;
import com.webank.ai.fate.serving.common.model.MergeInferenceAware;
import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.common.rpc.core.ErrorMessageUtil;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.*;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.federatedml.model.BaseComponent;
import com.webank.ai.fate.serving.federatedml.model.Returnable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

import static com.webank.ai.fate.serving.common.rpc.core.ErrorMessageUtil.buildRemoteRpcErrorMsg;
import static com.webank.ai.fate.serving.common.rpc.core.ErrorMessageUtil.transformRemoteErrorCode;
import static com.webank.ai.fate.serving.core.bean.Dict.PIPLELINE_IN_MODEL;

public class PipelineModelProcessor implements ModelProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PipelineModelProcessor.class);
    private static String flower = "pipeline.pipeline:Pipeline";
    private static ForkJoinPool forkJoinPool = new ForkJoinPool();
    private List<BaseComponent> pipeLineNode = new ArrayList<>();
    private Map<String, BaseComponent> componentMap = new HashMap<String, BaseComponent>();
    private DSLParser dslParser = new DSLParser();
    private String modelPackage = "com.webank.ai.fate.serving.federatedml.model";
    private int splitSize = MetaInfo.PROPERTY_BATCH_SPLIT_SIZE;

    @Override
    public BatchInferenceResult guestBatchInference(Context context, BatchInferenceRequest batchInferenceRequest, Map<String, Future> remoteFutureMap, long timeout) {
        BatchInferenceResult batchFederatedResult = new BatchInferenceResult();
        Map<Integer, Map<String, Object>> localResult = batchLocalInference(context, batchInferenceRequest);
        Map<String, BatchInferenceResult> remoteResultMap = Maps.newHashMap();
        remoteFutureMap.forEach((partyId, future) -> {
            Proxy.Packet packet = null;
            try {
                BatchInferenceResult remoteInferenceResult = (BatchInferenceResult) future.get(timeout, TimeUnit.MILLISECONDS);
                if (StatusCode.SUCCESS != remoteInferenceResult.getRetcode()) {
                    throw new RemoteRpcException(transformRemoteErrorCode(remoteInferenceResult.getRetcode()), buildRemoteRpcErrorMsg(remoteInferenceResult.getRetcode(), remoteInferenceResult.getRetmsg()));
                }
                remoteResultMap.put(partyId, remoteInferenceResult);
            } catch (Exception e) {
                if (!(e instanceof RemoteRpcException)) {
                    throw new RemoteRpcException("party id " + partyId + " remote error");
                } else {
                    throw (RemoteRpcException) e;
                }
            } finally {
                context.setDownstreamCost(System.currentTimeMillis() - context.getDownstreamBegin());
            }
        });
        batchFederatedResult = batchMergeHostResult(context, localResult, remoteResultMap);
        return batchFederatedResult;
    }

    /**
     * host 端只需要本地预测即可
     *
     * @param context
     * @param batchHostFederatedParams
     * @return
     */
    @Override
    public BatchInferenceResult hostBatchInference(Context context, BatchHostFederatedParams batchHostFederatedParams) {
        Map<Integer, Map<String, Object>> localResult = batchLocalInference(context, batchHostFederatedParams);
        BatchInferenceResult batchFederatedResult = new BatchInferenceResult();
        localResult.forEach((index, data) -> {
            BatchInferenceResult.SingleInferenceResult singleInferenceResult = new BatchInferenceResult.SingleInferenceResult();
            if (data != null) {
                int retcode = data.get(Dict.RET_CODE) != null ? (int) data.get(Dict.RET_CODE) : StatusCode.SYSTEM_ERROR;
                data.remove(Dict.RET_CODE);
                singleInferenceResult.setData(data);
                singleInferenceResult.setIndex(index);
                singleInferenceResult.setRetcode(retcode);
            }
            batchFederatedResult.getBatchDataList().add(singleInferenceResult);
        });
        batchFederatedResult.setRetcode(StatusCode.SUCCESS);
        return batchFederatedResult;
    }

    @Override
    public ReturnResult guestInference(Context context, InferenceRequest inferenceRequest, Map<String, Future> futureMap, long timeout) {
        Map<String, Object> localResult = singleLocalPredict(context, inferenceRequest.getFeatureData());
        ReturnResult remoteResult = new ReturnResult();
        Map<String, Object> remoteResultMap = Maps.newHashMap();
        futureMap.forEach((partId, future) -> {
            try {
                ReturnResult remoteReturnResult = (ReturnResult) future.get(timeout, TimeUnit.MILLISECONDS);
                if (remoteReturnResult != null) {
                    HashMap<String, Object> remoteData = Maps.newHashMap(remoteReturnResult.getData());
                    remoteData.put(Dict.RET_CODE, remoteReturnResult.getRetcode());
                    remoteData.put(Dict.MESSAGE, remoteReturnResult.getRetmsg());
                    remoteData.put(Dict.DATA, remoteReturnResult.getData());
                    remoteResultMap.put(partId, remoteData);
                }
            } catch (Exception e) {
                logger.error("host " + partId + " remote error : " + e.getMessage());
                throw new RemoteRpcException("host " + partId + " remote error : " + e.getMessage());
            } finally {
                context.setDownstreamCost(System.currentTimeMillis() - context.getDownstreamBegin());
            }
        });
        Map<String, Object> tempResult = singleMerge(context, localResult, remoteResultMap);
        int retcode = (int) tempResult.get(Dict.RET_CODE);
        String message = tempResult.get(Dict.MESSAGE) == null ? "" : tempResult.get(Dict.MESSAGE).toString();
        tempResult.remove(Dict.RET_CODE);
        tempResult.remove(Dict.MESSAGE);
        remoteResult.setData(tempResult);
        remoteResult.setRetcode(retcode);
        remoteResult.setRetmsg(message);
        return remoteResult;
    }

    @Override
    public ReturnResult hostInference(Context context, InferenceRequest InferenceRequest) {
        Map<String, Object> featureData = InferenceRequest.getFeatureData();
        Map<String, Object> returnData = this.singleLocalPredict(context, featureData);
        ReturnResult returnResult = new ReturnResult();
        returnResult.setRetcode(StatusCode.SUCCESS);
        returnResult.setData(returnData);
        return returnResult;
    }

    @Override
    public Object getComponent(String name) {
        return this.componentMap.get(name);
    }

    public int initModel(Map<String, byte[]> modelProtoMap) {
        if (modelProtoMap != null) {
            logger.info("start init pipeline,model components {}", modelProtoMap.keySet());
            try {
                Map<String, byte[]> newModelProtoMap = changeModelProto(modelProtoMap);
                logger.info("after parse pipeline {}", newModelProtoMap.keySet());
                Preconditions.checkArgument(newModelProtoMap.get(PIPLELINE_IN_MODEL) != null);
                PipelineProto.Pipeline pipeLineProto = PipelineProto.Pipeline.parseFrom(newModelProtoMap.get(PIPLELINE_IN_MODEL));
                String dsl = pipeLineProto.getInferenceDsl().toStringUtf8();
                dslParser.parseDagFromDSL(dsl);
                ArrayList<String> components = dslParser.getAllComponent();
                HashMap<String, String> componentModuleMap = dslParser.getComponentModuleMap();
                for (int i = 0; i < components.size(); ++i) {
                    String componentName = components.get(i);
                    String className = componentModuleMap.get(componentName);
                    logger.info("try to get class:{}", className);
                    try {
                        Class modelClass = Class.forName(this.modelPackage + "." + className);
                        BaseComponent mlNode = (BaseComponent) modelClass.getConstructor().newInstance();
                        mlNode.setComponentName(componentName);
                        byte[] protoMeta = newModelProtoMap.get(componentName + ".Meta");
                        byte[] protoParam = newModelProtoMap.get(componentName + ".Param");
                        int returnCode = mlNode.initModel(protoMeta, protoParam);
                        if (returnCode == Integer.valueOf(StatusCode.SUCCESS)) {
                            componentMap.put(componentName, mlNode);
                            pipeLineNode.add(mlNode);
                            logger.info(" add class {} to pipeline task list", className);
                        } else {
                            throw new RuntimeException("init model error");
                        }
                    } catch (Exception ex) {
                        pipeLineNode.add(null);
                        logger.warn("Can not instance {} class", className);
                    }
                }
            } catch (Exception ex) {
                logger.info("initModel error:{}", ex);
                throw new RuntimeException("initModel error");
            }
            logger.info("Finish init Pipeline");
            return Integer.valueOf(StatusCode.SUCCESS);
        } else {
            logger.error("model content is null ");
            throw new RuntimeException("model content is null");
        }
    }

    public Map<Integer, Map<String, Object>> batchLocalInference(Context context,
                                                                 BatchInferenceRequest batchFederatedParams) {
        long  begin =  System.currentTimeMillis();
        List<BatchInferenceRequest.SingleInferenceData> inputList = batchFederatedParams.getBatchDataList();
        Map<String, Map<String, Object>> tempCache = Maps.newConcurrentMap();
        ForkJoinTask<Map<Integer, Map<String, Object>>> future = forkJoinPool.submit(new LocalInferenceTask(context, inputList, tempCache));
        Map<Integer, Map<String, Object>> result = null;
        try {
            result = future.get();
        } catch (Exception e) {
            throw new SysException("get local inference result error");
        }
        long end = System.currentTimeMillis();
        logger.info("batchLocalInference cost {}",end-begin);
        return result;
    }

    private BatchInferenceResult batchMergeHostResult(Context context, Map<Integer, Map<String, Object>> localResult, Map<String, BatchInferenceResult> remoteResult) {
        long begin = System.currentTimeMillis();
        try {
            Preconditions.checkArgument(localResult != null);
            Preconditions.checkArgument(remoteResult != null);
            BatchInferenceResult batchFederatedResult = new BatchInferenceResult();
            batchFederatedResult.setRetcode(StatusCode.SUCCESS);
            List<Integer> keys = Lists.newArrayList(localResult.keySet().iterator());
            ForkJoinTask<List<BatchInferenceResult.SingleInferenceResult>> forkJoinTask = forkJoinPool.submit(new MergeTask(context, localResult, remoteResult, keys));
            List<BatchInferenceResult.SingleInferenceResult> resultList = forkJoinTask.get();
            batchFederatedResult.setBatchDataList(resultList);
            return batchFederatedResult;
        } catch (Exception e) {
            throw new GuestMergeException(e.getMessage());
        }
    }

    private boolean checkResult(Map<String, Object> result) {
        if (result == null) {
            return false;
        }
        if (result.get(Dict.RET_CODE) == null) {
            return false;
        }
        int retCode = (int) result.get(Dict.RET_CODE);
        if (StatusCode.SUCCESS != retCode) {
            return false;
        }
        return true;
    }

    public Map<String, Object> singleMerge(Context context, Map<String, Object> localData, Map<String, Object> remoteData) {

        if (localData == null || localData.size() == 0) {
            throw new BaseException(StatusCode.GUEST_MERGE_ERROR, "local inference result is null");
        }
        if (remoteData == null || remoteData.size() == 0) {
            throw new BaseException(StatusCode.GUEST_MERGE_ERROR, "remote inference result is null");
        }
        List<Map<String, Object>> outputData = Lists.newArrayList();
        List<Map<String, Object>> tempList = Lists.newArrayList();
        Map<String, Object> result = Maps.newHashMap();
        result.put(Dict.RET_CODE, StatusCode.SUCCESS);
        int pipelineSize = this.pipeLineNode.size();
        for (int i = 0; i < pipelineSize; i++) {
            BaseComponent component = this.pipeLineNode.get(i);
            List<Map<String, Object>> inputs = new ArrayList<>();
            HashSet<Integer> upInputComponents = this.dslParser.getUpInputComponents(i);
            if (upInputComponents != null) {
                Iterator<Integer> iters = upInputComponents.iterator();
                while (iters.hasNext()) {
                    Integer upInput = iters.next();
                    if (upInput == -1) {
                        inputs.add(localData);
                    } else {
                        inputs.add(outputData.get(upInput));
                    }
                }
            } else {
                inputs.add(localData);
            }
            if (component != null) {
                Map<String, Object> mergeResult = null;
                if (component instanceof MergeInferenceAware) {
                    String componentResultKey = component.getComponentName();
                    mergeResult = ((MergeInferenceAware) component).mergeRemoteInference(context, inputs, remoteData);
                    outputData.add(mergeResult);
                    tempList.add(mergeResult);
                } else {
                    outputData.add(inputs.get(0));
                }
                if (component instanceof Returnable && mergeResult != null) {
                    tempList.add(mergeResult);
                }
            } else {
                outputData.add(inputs.get(0));
            }
        }
        if (tempList.size() > 0) {
            result.putAll(tempList.get(tempList.size() - 1));
        }
        return result;
    }

    public Map<String, Object> singleLocalPredict(Context context, Map<String, Object> inputData) {
        List<Map<String, Object>> outputData = Lists.newArrayList();
        List<Map<String, Object>> tempList = Lists.newArrayList();
        Map<String, Object> result = Maps.newHashMap();
        result.put(Dict.RET_CODE, StatusCode.SUCCESS);
        context.putData(Dict.ORIGINAL_PREDICT_DATA,inputData);
        int pipelineSize = this.pipeLineNode.size();
        for (int i = 0; i < pipelineSize; i++) {
            BaseComponent component = this.pipeLineNode.get(i);
            if (logger.isDebugEnabled()) {
                if (component != null) {
                    logger.debug("component class is {}", component.getClass().getName());
                } else {
                    logger.debug("component class is {}", component);
                }
            }
            List<Map<String, Object>> inputs = new ArrayList<>();
            HashSet<Integer> upInputComponents = this.dslParser.getUpInputComponents(i);
            if (upInputComponents != null) {
                Iterator<Integer> iters = upInputComponents.iterator();
                while (iters.hasNext()) {
                    Integer upInput = iters.next();
                    if (upInput == -1) {
                        inputs.add(inputData);
                    } else {
                        inputs.add(outputData.get(upInput));
                    }
                }
            } else {
                inputs.add(inputData);
            }
            if (component != null) {
                Map<String, Object> componentResult = component.localInference(context, inputs);
                outputData.add(componentResult);
                tempList.add(componentResult);
                if (component instanceof Returnable) {
                    result.put(component.getComponentName(), componentResult);
                    if (logger.isDebugEnabled()) {
                        logger.debug("component {} is Returnable return data {}", component, result);
                    }
                    if (StringUtils.isBlank(context.getVersion()) || Long.parseLong(context.getVersion()) < 200) {
                        result.putAll(componentResult);
                    }
                }
            } else {
                outputData.add(inputs.get(0));
            }
        }
        return result;
    }

    private HashMap<String, byte[]> changeModelProto(Map<String, byte[]> modelProtoMap) {
        HashMap<String, byte[]> newModelProtoMap = new HashMap<String, byte[]>(8);
        for (Map.Entry<String, byte[]> entry : modelProtoMap.entrySet()) {
            String key = entry.getKey();
            if (!flower.equals(key)) {
                String[] componentNameSegments = key.split("\\.", -1);
                if (componentNameSegments.length != 2) {
                    newModelProtoMap.put(entry.getKey(), entry.getValue());
                    continue;
                }
                if (componentNameSegments[1].endsWith("Meta")) {
                    newModelProtoMap.put(componentNameSegments[0] + ".Meta", entry.getValue());
                } else if (componentNameSegments[1].endsWith("Param")) {
                    newModelProtoMap.put(componentNameSegments[0] + ".Param", entry.getValue());
                }
            } else {
                newModelProtoMap.put(entry.getKey(), entry.getValue());
            }
        }
        return newModelProtoMap;
    }

    class LocalInferenceTask extends RecursiveTask<Map<Integer, Map<String, Object>>> {
        Context context;
        Map<String, Map<String, Object>> tempCache;
        List<BatchInferenceRequest.SingleInferenceData> inputList;
        LocalInferenceTask(Context context, List<BatchInferenceRequest.SingleInferenceData> inputList, Map<String, Map<String, Object>> tempCache) {
            this.context = context;
            this.inputList = inputList;
            this.tempCache = tempCache;
        }

        @Override
        protected Map<Integer, Map<String, Object>> compute() {
                Map<Integer, Map<String, Object>> result = new HashMap<>();
                if (inputList.size() <= splitSize) {
                    for (int i = 0; i < inputList.size(); i++) {
                        BatchInferenceRequest.SingleInferenceData input = inputList.get(i);
                        try {
                            String key = EncryptUtils.encrypt(JsonUtil.object2Json(input.getFeatureData()), EncryptMethod.MD5);
                            Map<String, Object> singleResult = tempCache.get(key);
                            if (singleResult == null) {
                                singleResult = singleLocalPredict(context, input.getFeatureData());
                                if (singleResult != null && singleResult.size() != 0) {
                                    tempCache.putIfAbsent(key, singleResult);
                                }
                            } else {
                                singleResult = Maps.newHashMap(tempCache.get(key));
                                logger.info("hit cache");
                            }
                            result.put(input.getIndex(), singleResult);
                            if (input.isNeedCheckFeature()) {
                                if (input.getFeatureData() == null || input.getFeatureData().size() == 0) {
                                    throw new HostGetFeatureErrorException("no feature");
                                }
                            }
                        } catch (Throwable e) {
                            if (result.get(input.getIndex()) == null) {
                                result.put(input.getIndex(), ErrorMessageUtil.handleExceptionToMap(e));
                            } else {
                                result.get(input.getIndex()).putAll(ErrorMessageUtil.handleExceptionToMap(e));
                            }
                        }

                    }



                    return result;
                } else {
                    List<List<BatchInferenceRequest.SingleInferenceData>> splits = new ArrayList<List<BatchInferenceRequest.SingleInferenceData>>();
                    int size = inputList.size();
                    int count = (size + splitSize - 1) / splitSize;
                    List<LocalInferenceTask> subJobs = Lists.newArrayList();
                    for (int i = 0; i < count; i++) {
                        List<BatchInferenceRequest.SingleInferenceData> subList = inputList.subList(i * splitSize, ((i + 1) * splitSize > size ? size : splitSize * (i + 1)));
                        logger.info("input size {} splitsize {} count {}",inputList.size(),splitSize,count);
                        LocalInferenceTask subLocalInferenceTask = new LocalInferenceTask(context, subList, tempCache);
                        subLocalInferenceTask.fork();
                        subJobs.add(subLocalInferenceTask);
                    }
                    subJobs.forEach(localInferenceTask -> {
                        Map<Integer, Map<String, Object>> splitResult = localInferenceTask.join();
                        if (splitResult != null) {
                            result.putAll(splitResult);
                        }
                    });
                    return result;
                }

        }
    }

    class MergeTask extends RecursiveTask<List<BatchInferenceResult.SingleInferenceResult>> {
        Map<Integer, Map<String, Object>> localResult;
        Map<String, BatchInferenceResult> remoteResult;
        List<Integer> keys;
        Context context;

        MergeTask(Context context, Map<Integer, Map<String, Object>> localResult,
                  Map<String, BatchInferenceResult> remoteResult, List<Integer> keys) {
            this.context = context;
            this.localResult = localResult;
            this.remoteResult = remoteResult;
            this.keys = keys;
        }

        @Override
        protected List<BatchInferenceResult.SingleInferenceResult> compute() {
            List<BatchInferenceResult.SingleInferenceResult> singleResultLists = Lists.newArrayList();
            if (keys.size() <= splitSize) {
                localResult.forEach((index, data) -> {
                    Map<String, Object> remoteSingleMap = Maps.newHashMap();
                    remoteResult.forEach((partyId, batchResult) -> {
                        if (batchResult.getSingleInferenceResultMap() != null) {
                            if (batchResult.getSingleInferenceResultMap().get(index) != null) {
                                BatchInferenceResult.SingleInferenceResult singleInferenceResult = batchResult.getSingleInferenceResultMap().get(index);
                                Map<String, Object> realRemoteData = singleInferenceResult.getData();
                                realRemoteData.put(Dict.RET_CODE, singleInferenceResult.getRetcode());
                                remoteSingleMap.put(partyId, realRemoteData);
                            }
                        }
                    });
                    try {
                        Map<String, Object> localData = localResult.get(index);
                        Map<String, Object> mergeResult = singleMerge(context, localData, remoteSingleMap);
                        int retcode = (int) mergeResult.get(Dict.RET_CODE);
                        String msg = mergeResult.get(Dict.MESSAGE) != null ? mergeResult.get(Dict.MESSAGE).toString() : "";
                        mergeResult.remove(Dict.RET_CODE);
                        mergeResult.remove(Dict.MESSAGE);
                        singleResultLists.add(new BatchInferenceResult.SingleInferenceResult(index, retcode, msg, mergeResult));
                    } catch (Exception e) {
                        logger.error("merge remote error", e);
                        int retcode = ErrorMessageUtil.getLocalExceptionCode(e);
                        singleResultLists.add(new BatchInferenceResult.SingleInferenceResult(index, retcode, e.getMessage(), null));
                    }
                });

            } else {
                List<List<Integer>> splits = new ArrayList<List<Integer>>();
                int size = keys.size();
                int count = (size + splitSize - 1) / splitSize;
                List<MergeTask> subJobs = Lists.newArrayList();
                for (int i = 0; i < count; i++) {
                    List<Integer> subList = keys.subList(i * splitSize, ((i + 1) * splitSize > size ? size : splitSize * (i + 1)));
                    MergeTask subMergeTask = new MergeTask(context, localResult, remoteResult, subList);
                    subMergeTask.fork();
                    subJobs.add(subMergeTask);
                }
                for (MergeTask mergeTask : subJobs) {
                    List<BatchInferenceResult.SingleInferenceResult> subResultList = mergeTask.join();
                    singleResultLists.addAll(subResultList);
                }
            }
            return singleResultLists;
        }
    }
}
