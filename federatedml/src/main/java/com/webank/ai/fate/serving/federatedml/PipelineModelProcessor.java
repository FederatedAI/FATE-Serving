package com.webank.ai.fate.serving.federatedml;


import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.core.mlmodel.buffer.PipelineProto;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.GuestMergeException;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.model.MergeInferenceAware;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.federatedml.model.BaseComponent;
import com.webank.ai.fate.serving.federatedml.model.PrepareRemoteable;
import com.webank.ai.fate.serving.federatedml.model.Returnable;
import io.grpc.StatusRuntimeException;
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
    public BatchInferenceResult guestBatchInference(Context context, BatchInferenceRequest batchInferenceRequest, Map<String,Future> remoteFutureMap,long  timeout) {

        BatchInferenceResult batchFederatedResult = new BatchInferenceResult();

        Map<Integer, Map<String, Object>> localResult = batchLocalInference(context, batchInferenceRequest);

        Map<String,BatchInferenceResult>  remoteResultMap =  Maps.newHashMap();

        remoteFutureMap.forEach((partyId,future)->{

            Proxy.Packet packet  =null;

            try {
                packet   = (Proxy.Packet) future.get(timeout, TimeUnit.MILLISECONDS);
                String   remoteContent =  packet.getBody().getValue().toStringUtf8();
                logger.info("caseid {} remote result is {} ",context.getCaseId(),remoteContent);

                BatchInferenceResult  remoteInferenceResult = (BatchInferenceResult) JSON.parseObject(remoteContent, BatchInferenceResult.class);

                if (!remoteInferenceResult.getRetcode() .equals(StatusCode.SUCCESS)) {

                    throw  new RemoteRpcException(buildRemoteRpcErrorMsg(remoteInferenceResult.getRetcode(),remoteInferenceResult.getRetmsg()));
                }

                remoteResultMap.put(partyId,remoteInferenceResult);

            }  catch (TimeoutException e) {
                throw  new  RemoteRpcException("party id "+partyId+ " time out");
            }catch(Exception e){
                e.printStackTrace();
                throw new  SysException(e.getMessage());
            }

        });



        batchFederatedResult = batchMergeHostResult(context, localResult, remoteResultMap);

        return batchFederatedResult;
    }


    private  String  buildRemoteRpcErrorMsg(String  code ,String  msg){
        return  new StringBuilder().append("host return code ").append(code)
                .append("host msg :").append(msg).toString();

    }

    /**
     *  host 端只需要本地预测即可
     * @param context
     * @param batchHostFederatedParams
     * @return
     */
    @Override
    public BatchInferenceResult hostBatchInference(Context context, BatchHostFederatedParams batchHostFederatedParams) {

        Map<Integer ,Map<String,Object>>  localResult = batchLocalInference(context,batchHostFederatedParams);

        logger.info("caseid {} hostBatchInference result {} ",context.getCaseId(),localResult);

        BatchInferenceResult batchFederatedResult = new BatchInferenceResult() ;

        localResult.forEach((k,v)->{

            BatchInferenceResult.SingleInferenceResult singleInferenceResult=  new  BatchInferenceResult.SingleInferenceResult();

            // TODO: 2020/3/4  这里需要添加对每个返回的结果是否成功的判断逻辑，目前只是简单用是否返回数据来表示
            if(v!=null){
                singleInferenceResult.setData(v);
                singleInferenceResult.setIndex(k);
                singleInferenceResult.setRetcode(InferenceRetCode.OK);
            }
            batchFederatedResult.getBatchDataList().add(singleInferenceResult);
        });

        batchFederatedResult.setRetcode(InferenceRetCode.OK);

        return  batchFederatedResult;
    }

    /**
     *   目前给  只有sbt 使用到
     * @param context
     * @param batchInferenceRequest
     * @return
     */
    @Override
    public BatchInferenceRequest guestPrepareDataBeforeInference(Context context, BatchInferenceRequest batchInferenceRequest) {

        List<BatchInferenceRequest.SingleInferenceData>  reqDataList = batchInferenceRequest.getBatchDataList();

        reqDataList.forEach(data->{
                    this.pipeLineNode.forEach(component ->{
                        try{
                            if(component!=null&&component instanceof PrepareRemoteable){
                              PrepareRemoteable  prepareRemoteable =  (PrepareRemoteable) component;
                              prepareRemoteable.prepareRemoteData(context ,data.getSendToRemoteFeatureData());
                            }
                        }catch(Exception e){
                            // TODO: 2020/3/16   这里需要考虑下异常情况怎么处理
                        }
                    });
                    data.getSendToRemoteFeatureData();
                }
        );
        return null;
    }

    @Override
    public  ReturnResult guestInference(Context context, InferenceRequest inferenceRequest, Map<String,Future> futureMap, long timeout) {


        ReturnResult  returnResult = new ReturnResult();

        Map<String, Object> localResult = singleLocalPredict(context, inferenceRequest.getFeatureData());

        logger.info("======================== localResult {}",localResult);

        ReturnResult  remoteResult = new ReturnResult();

        try {
            Map<String,Object>  remoteResultMap = Maps.newHashMap();

            futureMap.forEach((partId,future)->{
                try {
                    Proxy.Packet  packet =   (Proxy.Packet)future.get(timeout, TimeUnit.MILLISECONDS);
                    String   remoteContent = new String(packet.getBody().getValue().toByteArray());
                    logger.info("caseid {} remote partid {} return data {}",context.getCaseId() ,partId,remoteContent);
                    ReturnResult   remoteReturnResult  = JSON.parseObject(remoteContent,ReturnResult.class);
                    if(remoteReturnResult!=null) {
                        if(StatusCode.SUCCESS.equals(remoteReturnResult.getRetcode())) {
                            Map<String, Object> remoeteData = remoteReturnResult.getData();
                            remoteResultMap.put(partId, remoeteData);
                        }else{
                           StringBuilder  sb = new StringBuilder();
                           sb.append("host ").append(partId).append(" return code ").append(remoteReturnResult.getRetcode());
                           throw  new  RemoteRpcException(remoteReturnResult.getRetcode(),sb.toString());
                        }
                    }

                } catch(StatusRuntimeException e){
                    throw new RemoteRpcException("host "+partId+" StatusRuntimeException");
                }
                catch (TimeoutException e) {
                    throw new RemoteRpcException("host "+partId+" timeout");
                } catch (InterruptedException e) {
                    throw new RemoteRpcException("host "+partId+" interrupted");
                } catch (ExecutionException e) {
                    throw new RemoteRpcException("host "+partId+" execution exception");
                }

            });


            Map<String, Object> tempResult= singleMerge(context,localResult,remoteResultMap );

            remoteResult.setData(tempResult);

            remoteResult.setRetcode(StatusCode.SUCCESS);

        }catch (Exception e){
            e.printStackTrace();
            throw  e;
        }

        return remoteResult;


    }

    @Override
    public ReturnResult hostInference(Context context, InferenceRequest InferenceRequest) {
        Map<String,Object> featureData = InferenceRequest.getFeatureData();
        Map<String, Object> returnData = this.singleLocalPredict(context,featureData);
        ReturnResult  returnResult =  new ReturnResult();
        returnResult.setRetcode(StatusCode.SUCCESS);
        returnResult.setData(returnData);
        return returnResult;
    }


    @Override
    public Object getComponent(String name) {

        return this.componentMap.get(name);
    }

    private static final Logger logger = LoggerFactory.getLogger(PipelineModelProcessor.class);
    private List<BaseComponent> pipeLineNode = new ArrayList<>();
    private Map<String, BaseComponent> componentMap = new HashMap<String, BaseComponent>();
    private DSLParser dslParser = new DSLParser();
    private String modelPackage = "com.webank.ai.fate.serving.federatedml.model";

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
                        BaseComponent mlNode = (BaseComponent) modelClass.getConstructor().newInstance();
                        mlNode.setComponentName(componentName);
                        byte[] protoMeta = newModelProtoMap.get(componentName + ".Meta");
                        byte[] protoParam = newModelProtoMap.get(componentName + ".Param");
                        int returnCode = mlNode.initModel(protoMeta, protoParam);
                        if (returnCode == Integer.valueOf(InferenceRetCode.OK)) {
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
                // ex.printStackTrace();
                logger.info("initModel error:{}", ex);
                throw new RuntimeException("initModel error");
            }
            logger.info("Finish init Pipeline");
            return Integer.valueOf(InferenceRetCode.OK);
        }else{
            logger.error("model content is null ");
            throw new RuntimeException("model content is null");
        }
    }

    public Map<Integer ,Map<String,Object>>  batchLocalInference(Context context,
                                                  BatchInferenceRequest batchFederatedParams){
        List<BatchInferenceRequest.SingleInferenceData> inputList = batchFederatedParams.getBatchDataList();
        Map<Integer ,Map<String,Object>> result = new HashMap<>();
        for(int i=0;i<inputList.size();i++){
            try {
                BatchInferenceRequest.SingleInferenceData input = inputList.get(i);

                Map<String, Object> singleResult = singleLocalPredict(context, input.getFeatureData());
                if (singleResult != null) {
//                    checkResult(singleResult);
                    result.put(input.getIndex(), singleResult);
                } else {
                    logger.error("local predict return null");
                }
            }catch (Throwable e){
                logger.error("localPredict error",e);

            }
        }
        logger.info("case id {} return batch local inference result {}",context.getCaseId(),result);
        return   result;
    }

//    private Map changeRemoteResultToMap(BatchInferenceResult  batchInferenceResult){
//
//        Map result  =  Maps.newHashMap();
//
//        List<BatchInferenceResult.SingleInferenceResult>  batchInferences = batchInferenceResult.getBatchDataList();
//
//        for(BatchInferenceResult.SingleInferenceResult  singleInferenceResult:batchInferences){
//
//            result.put(singleInferenceResult.getIndex(),singleInferenceResult);
//        }
//        return  result;
//    }

    private BatchInferenceResult batchMergeHostResult(Context context, Map<Integer, Map<String, Object>> localResult, Map<String,BatchInferenceResult> remoteResult) {

        try {

            Preconditions.checkArgument(localResult != null);
            Preconditions.checkArgument(remoteResult != null);
            //Preconditions.checkArgument(remoteResult.getBatchDataList() != null);
            BatchInferenceResult batchFederatedResult = new BatchInferenceResult();
           // Map remoteResultMap = changeRemoteResultToMap(remoteResult);
            localResult.forEach((index,data )->{


                Map<String ,Object>  remoteSingleMap = Maps.newHashMap();

                remoteResult.forEach((partyId,batchResult)->{
                    if(batchResult.getSingleInferenceResultMap()!=null&& batchResult.getSingleInferenceResultMap().get(index)!=null) {
                        Map<String,Object> realRemoteData = batchResult.getSingleInferenceResultMap().get(index).getData();
                        remoteSingleMap.put(partyId,realRemoteData);
                    }
                });

                try {
                    Map<String, Object> localData = localResult.get(index);
                   // BatchInferenceResult.SingleInferenceResult singleRemoteResult = (BatchInferenceResult.SingleInferenceResult) remoteResultMap.get(index);
                   // Map<String, Object> remoteData = singleRemoteResult.getData();

                    Map<String, Object> mergeResult =this.singleMerge(context,localData,remoteSingleMap);
                    batchFederatedResult.getBatchDataList().add( new BatchInferenceResult.SingleInferenceResult(index, StatusCode.SUCCESS, Dict.SUCCESS, mergeResult));
//                    Map<String, Object> input = new HashMap<>();
//                    for (BaseComponent component : this.pipeLineNode) {
//                        // TODO: 2020/3/16 错误码之后再规整
//                      if (component instanceof MergeInferenceAware) {
//
//
//                                Map<String, Object> mergeResult =((MergeInferenceAware)component).mergeRemoteInference(context, localData,remoteData);
//                                batchFederatedResult.getBatchDataList().set(index, new BatchInferenceResult.SingleInferenceResult(index, "0", Dict.SUCCESS, mergeResult));
//                      }
//                    }
                }catch(Exception e){
                    logger.error("merge remote error", e);
                    // TODO: 2020/3/16  错误码之后再定
                    batchFederatedResult.getBatchDataList().add( new BatchInferenceResult.SingleInferenceResult(index, "0000dddd", e.getMessage(), null));
                }

            });

            return batchFederatedResult;
        }catch(Exception  e){
            logger.error("merge remote result error",e);
            throw  new GuestMergeException(e.getMessage());
        }
    }


    /**
     * 单个返回必须携带能够标记此次请求的caseid
     * @return
     */
    private void checkResult(Map<String,Object>  result){
        Preconditions.checkArgument( result.get(Dict.CASEID)!=null);

    }






    public Map<String, Object> singleMerge(Context context, Map<String, Object> localData,Map<String,Object> remoteData) {
        logger.info("prepare merge local data {} remote data {}",localData,remoteData);
        List<Map<String, Object>> outputData = Lists.newArrayList();
        List<Map<String, Object>>  tempList = Lists.newArrayList();
        Map<String,Object>  result = Maps.newHashMap();
        int pipelineSize = this.pipeLineNode.size();
        for (int i = 0; i < pipelineSize; i++) {
            BaseComponent component   = this.pipeLineNode.get(i);
            if(logger.isDebugEnabled()) {
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
                        inputs.add(localData);
                    } else {
                        inputs.add(outputData.get(upInput));
                    }
                }
            } else {
                inputs.add(localData);
            }
            if (component != null) {
                Map<String,Object> mergeResult= null;
                if(component instanceof MergeInferenceAware){
                    logger.info("component {} is instanceof MergeInferenceAware local inputs {}",component,inputs);
                    String  componentResultKey = component.getComponentName();
//                    Map<String,Object> remoteComponentData = (Map<String,Object> )remoteData.get(componentResultKey);
//                    if(remoteComponentData==null){
//                        logger.error("remoteComponentData is null ,key {} remote data {}",componentResultKey,remoteData);
//                        throw new GuestMergeException("");
//                    }


                    mergeResult = ((MergeInferenceAware) component).mergeRemoteInference(context, inputs,remoteData);
                    outputData.add(mergeResult);
                    tempList.add(mergeResult);
                }else{

                    outputData.add(inputs.get(0));

                }

                if(component instanceof  Returnable&& mergeResult!=null){
                    logger.info("component {} is instanceof Returnable",component);
                    tempList.add(mergeResult);
                }


            } else {
                outputData.add(inputs.get(0));
            }
        }
//        ReturnResult federatedResult = context.getFederatedResult();
//        if (federatedResult != null) {
//            inputData.put(Dict.RET_CODE, federatedResult.getRetcode());
//        }

        /**
         *   这里只是实验 ，不再返回最后一个 ，而是返回多个组件的值
         */
        if(tempList.size()>0){
             result.putAll(tempList.get(tempList.size() - 1));
        }
        logger.info("merge result {}",result);
        return result;


    }



    public Map<String, Object> singleLocalPredict(Context context, Map<String, Object> inputData) {
        List<Map<String, Object>> outputData = Lists.newArrayList();
        List<Map<String, Object>>  tempList = Lists.newArrayList();
        Map<String,Object>  result = Maps.newHashMap();
        int pipelineSize = this.pipeLineNode.size();
        for (int i = 0; i < pipelineSize; i++) {
            BaseComponent component   = this.pipeLineNode.get(i);
            if(logger.isDebugEnabled()) {
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
                Map<String, Object>  componentResult = component.localInference(context, inputs);
                outputData.add(componentResult);
                tempList.add(componentResult);
                if(component instanceof   Returnable){
                    result.put(component.getComponentName(),componentResult);
                    logger.info("component {} is Returnable return data {}",component,result);
                }
            } else {
                outputData.add(inputs.get(0));
            }
        }
//        ReturnResult federatedResult = context.getFederatedResult();
//        if (federatedResult != null) {
//            inputData.put(Dict.RET_CODE, federatedResult.getRetcode());
//        }

        /**
         *   这里只是实验 ，不再返回最后一个 ，而是返回多个组件的值
         */
//        if(tempList.size()>0){
//             result.putAll(tempList.get(tempList.size() - 1));
//        }
        return result;


    }

    private  LocalInferenceParam buildLocalInferenceParam(){

        LocalInferenceParam  param = new  LocalInferenceParam();
        return   param;
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
