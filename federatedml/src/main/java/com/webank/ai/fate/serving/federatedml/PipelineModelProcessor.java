package com.webank.ai.fate.serving.federatedml;


import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.core.mlmodel.buffer.PipelineProto;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.federatedml.model.BaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.webank.ai.fate.serving.core.bean.Dict.PIPLELINE_IN_MODEL;

public class PipelineModelProcessor implements ModelProcessor{
    @Override
    public BatchInferenceResult guestBatchInference(Context context, BatchInferenceRequest batchInferenceRequest, Future remoteFuture) {
        BatchInferenceResult batchFederatedResult = new BatchInferenceResult();
        Map<Integer, Map<String, Object>> localResult = localPredict(context, batchInferenceRequest);

        try {
            Proxy.Packet packet = (Proxy.Packet) remoteFuture.get();
            if (packet != null) {
                BatchInferenceResult remoteInferenceResult = (BatchInferenceResult) ObjectTransform.json2Bean(packet.getBody().getValue().toStringUtf8(), BatchInferenceResult.class);
                if (remoteInferenceResult.getRetcode() != InferenceRetCode.OK) {
                    logger.info("get remote inference result error");
                    batchFederatedResult.setRetcode(remoteInferenceResult.getRetcode());
                    batchFederatedResult.setDataList(remoteInferenceResult.getDataList());
                    return batchFederatedResult;
                }
                batchFederatedResult = mergeHostResult(context, localResult, remoteInferenceResult);
            } else {
                batchFederatedResult.setRetcode(InferenceRetCode.NETWORK_ERROR);
                logger.error("can not get future result");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return batchFederatedResult;
    }

    /**
     *  host 端只需要本地预测即可
     * @param context
     * @param batchHostFederatedParams
     * @return
     */
    @Override
    public BatchInferenceResult hostBatchInference(Context context, BatchHostFederatedParams batchHostFederatedParams) {

        Map<Integer ,Map<String,Object>>  localResult = localPredict(context,batchHostFederatedParams);
        BatchInferenceResult batchFederatedResult = new BatchInferenceResult() ;
        localResult.forEach((k,v)->{

            BatchInferenceResult.SingleInferenceResult singleInferenceResult=  new  BatchInferenceResult.SingleInferenceResult();

            // TODO: 2020/3/4  这里需要添加对每个返回的结果是否成功的判断逻辑，目前只是简单用是否返回数据来表示
            if(v!=null){
                singleInferenceResult.setData(v);
                singleInferenceResult.setIndex(k);
                singleInferenceResult.setRetcode(InferenceRetCode.OK);
            }

            batchFederatedResult.getDataList().add(singleInferenceResult);

        });

        batchFederatedResult.setRetcode(InferenceRetCode.OK);

        return  batchFederatedResult;
    }

    private static final Logger logger = LoggerFactory.getLogger(PipelineTask.class);
    private List<BaseModel> pipeLineNode = new ArrayList<>();
    private Map<String, BaseModel> modelMap = new HashMap<String, BaseModel>();
    private DSLParser dslParser = new DSLParser();
    private String modelPackage = "com.webank.ai.fate.serving.federatedml.model";
    public BaseModel getModelByComponentName(String name) {
        return this.modelMap.get(name);
    }
    public int initModel(Map<String, byte[]> modelProtoMap) {
        if(modelProtoMap!=null) {
            logger.info("start init pipeline,model components {}", modelProtoMap.keySet());
            try {
                Map<String, byte[]> newModelProtoMap = changeModelProto(modelProtoMap);
                logger.info("after parse pipeline {}", newModelProtoMap.keySet());
                Preconditions.checkArgument(newModelProtoMap.get(PIPLELINE_IN_MODEL) != null);
                PipelineProto.Pipeline pipeLineProto = PipelineProto.Pipeline.parseFrom(newModelProtoMap.get(PIPLELINE_IN_MODEL));
                //inference_dsl
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
                        BaseModel mlNode = (BaseModel) modelClass.getConstructor().newInstance();
                        mlNode.setComponentName(componentName);
                        byte[] protoMeta = newModelProtoMap.get(componentName + ".Meta");
                        byte[] protoParam = newModelProtoMap.get(componentName + ".Param");
                        int returnCode = mlNode.initModel(protoMeta, protoParam);
                        if (returnCode == StatusCode.OK) {
                            modelMap.put(componentName, mlNode);
                            pipeLineNode.add(mlNode);
                            logger.info(" Add class {} to pipeline task list", className);
                        } else {
                            throw new RuntimeException("initModel error");
                        }
                    } catch (Exception ex) {
                        pipeLineNode.add(null);
                        logger.warn("Can not instance {} class", className);
                    }
                }
            } catch (Exception ex) {
                // ex.printStackTrace();
                logger.info("PipelineTask initModel error:{}", ex);
                throw new RuntimeException("initModel error");
            }
            logger.info("Finish init Pipeline");
            return StatusCode.OK;
        }else{
            logger.error("model content is null ");
            throw new RuntimeException("model content is null");
        }
    }

    public Map<Integer ,Map<String,Object>>  localPredict(Context context,
                                                  BatchInferenceRequest batchFederatedParams){

        List<BatchInferenceRequest.SingleInferenceData> inputList = batchFederatedParams.getDataList();
        Map<Integer ,Map<String,Object>> result = new HashMap<>();

        for(int i=0;i<inputList.size();i++){
            try {
                BatchInferenceRequest.SingleInferenceData input = inputList.get(i);

                Map<String, Object> singleResult = singleLocalPredict(context, input.getFeatureData());
                if (singleResult != null) {
                    checkResult(singleResult);
                    result.put(input.getIndex(), singleResult);
                } else {
                    logger.error("local predict return null");
                }
            }catch (Throwable e){
                logger.error("localPredict error",e);
            }
        }
        return   result;


    }


    private BatchInferenceResult mergeHostResult(Context context, Map<Integer, Map<String, Object>> localResult, BatchInferenceResult remoteResult) {
        Preconditions.checkArgument(localResult != null);
        Preconditions.checkArgument(remoteResult != null);
        Preconditions.checkArgument(remoteResult.getDataList() != null);

        BatchInferenceResult batchFederatedResult = new BatchInferenceResult();

        if (localResult.size() != remoteResult.getDataList().size()) {
            logger.info("The number of local prediction results is not consistent with the number of remote prediction results");
            batchFederatedResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
            return batchFederatedResult;
        }

        int dataSize = localResult.size();
        for (int i = 0; i < dataSize; i++) {
            BatchInferenceResult.SingleInferenceResult singleRemoteResult = remoteResult.getDataList().get(i);
            int index = singleRemoteResult.getIndex();
            Map<String, Object> localData = localResult.get(index);
            Map<String, Object> remoteData = singleRemoteResult.getData();

            Map<String, Object> input = new HashMap<>();
            input.put(Dict.LOCAL_INFERENCE_DATA, localData);
            input.put(Dict.REMOTE_INFERENCE_DATA, remoteData);

            for (BaseModel model : this.pipeLineNode) {
                Map<String, Object> mergeResult = model.mergeRemoteInference(context, input);
                batchFederatedResult.getDataList().set(index, new BatchInferenceResult.SingleInferenceResult(index, InferenceRetCode.OK, Dict.SUCCESS, mergeResult));
            }
        }

        return batchFederatedResult;
    }


    /**
     * 单个返回必须携带能够标记此次请求的caseid
     * @return
     */
    private void checkResult(Map<String,Object>  result){
        Preconditions.checkArgument( result.get(Dict.CASEID)!=null);

    }
/*    public  BatchInferenceResult  mergeHostResult(Context context,
                                              Map<Integer ,Map<String, Object>> localData,
                                                  Map<Integer ,Map<String,Object>> remoteData  ){

        return null;
    }*/


////    @Override
//    public BatchInferenceResult  batchPredict(Context  context, BatchInferenceRequest  batchInferenceRequest,
//                                              Future  remoteFuture ){
//
//        Map<Integer ,Map<String,Object>>  localResult = localPredict(context,batchInferenceRequest);
//        BatchInferenceResult batchFederatedResult =null;
//        if(remoteFuture!=null) {
//            Object remoteObject = null;
//            try {
//                remoteObject = remoteFuture.get(3000, TimeUnit.MILLISECONDS);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (TimeoutException e) {
//                e.printStackTrace();
//            }
//            Preconditions.checkArgument(remoteObject != null);
//            // BatchFederatedResult  batchFederatedResult =  (BatchFederatedResult)  remoteObject;
//            List<Map<String, Object>> remoteList = null;
//            batchFederatedResult= mergeRemote(context, localResult, remoteList);
//        }else{
//
//            /**
//             * host无future
//             */
//
//        }
//        return  batchFederatedResult;
//
//    }



    public Map<String, Object> singleLocalPredict(Context context, Map<String, Object> inputData) {
        //logger.info("Start Pipeline predict use {} model node.", this.pipeLineNode.size());
        List<Map<String, Object>> outputData = Lists.newArrayList();

        List<Map<String, Object>>  result = Lists.newArrayList();
        int pipelineSize = this.pipeLineNode.size();
        for (int i = 0; i < pipelineSize; i++) {
            if(logger.isDebugEnabled()) {
                if (this.pipeLineNode.get(i) != null) {
                    logger.debug("component class is {}", this.pipeLineNode.get(i).getClass().getName());
                } else {
                    logger.debug("component class is {}", this.pipeLineNode.get(i));
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
            if (this.pipeLineNode.get(i) != null) {


                Map<String, Object>  modelResult = this.pipeLineNode.get(i).localInference(context, inputs);
                outputData.add(modelResult);
                result.add(modelResult);

            } else {
                outputData.add(inputs.get(0));

            }

        }
//        ReturnResult federatedResult = context.getFederatedResult();
//        if (federatedResult != null) {
//            inputData.put(Dict.RET_CODE, federatedResult.getRetcode());
//        }
        if(result.size()>0){
            return result.get(result.size() - 1);
        }else{
            return Maps.newHashMap();
        }


    }

    private  LocalInferenceParam buildLocalInferenceParam(){

        LocalInferenceParam  param = new  LocalInferenceParam();
        return   param;
    }





    public Map<String, Object> predict(Context context, Map<String, Object> inputData, FederatedParams predictParams) {
        //logger.info("Start Pipeline predict use {} model node.", this.pipeLineNode.size());
        List<Map<String, Object>> outputData = Lists.newArrayList();

        List<Map<String,Object>>  result = Lists.newArrayList();
        int pipelineSize = this.pipeLineNode.size();
        for (int i = 0; i < pipelineSize; i++) {
            if(logger.isDebugEnabled()) {
                if (this.pipeLineNode.get(i) != null) {
                    logger.debug("component class is {}", this.pipeLineNode.get(i).getClass().getName());
                } else {
                    logger.debug("component class is {}", this.pipeLineNode.get(i));
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
            if (this.pipeLineNode.get(i) != null) {
                Map<String, Object>  modelResult = this.pipeLineNode.get(i).handlePredict(context, inputs, predictParams);
                outputData.add(modelResult);
                result.add(modelResult);

            } else {
                outputData.add(inputs.get(0));

            }

        }
        ReturnResult federatedResult = context.getFederatedResult();
        if (federatedResult != null) {
            inputData.put(Dict.RET_CODE, federatedResult.getRetcode());
        }
        if(result.size()>0){
            return result.get(result.size() - 1);
        }else{
            return Maps.newHashMap();
        }


    }

    private HashMap<String, byte[]> changeModelProto(Map<String, byte[]> modelProtoMap) {
        HashMap<String, byte[]> newModelProtoMap = new HashMap<String, byte[]>(8);
        for (Map.Entry<String, byte[]> entry : modelProtoMap.entrySet()) {
            String key = entry.getKey();
            if (!"pipeline.pipeline:Pipeline".equals(key)) {
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
}
