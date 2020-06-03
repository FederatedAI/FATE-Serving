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


import com.google.common.collect.Lists;
import com.webank.ai.fate.core.mlmodel.buffer.LRModelParamProto.LRModelParam;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.federatedml.PipelineModelProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public abstract class HeteroLR extends BaseComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeteroLR.class);
    private Map<String, Double> weight;
    private Double intercept;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        logger.info("start init HeteroLR class");
        try {
            LRModelParam lrModelParam = this.parseModel(LRModelParam.parser(), protoParam);
            this.weight = lrModelParam.getWeightMap();
            this.intercept = lrModelParam.getIntercept();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ILLEGALDATA;
        }
        logger.info("Finish init HeteroLR class, model weight is {}", this.weight);
        return OK;
    }
    Map<String, Double> forward(List<Map<String, Object>> inputDatas) {
        Map<String, Object> inputData = inputDatas.get(0);
        int modelWeightHitCount = 0;
        int inputDataHitCount = 0;
        int weightNum = this.weight.size();
        int inputFeaturesNum = inputData.size();
        if (logger.isDebugEnabled()) {
            logger.debug("model weight number:{}", weightNum);
            logger.debug("input data features number:{}", inputFeaturesNum);
        }
        double score = 0;
        for (String key : inputData.keySet()) {
            if (this.weight.containsKey(key)) {
                Double x = new Double(inputData.get(key).toString());
                Double w = new Double(this.weight.get(key).toString());
                score += w * x;
                modelWeightHitCount += 1;
                inputDataHitCount += 1;
                if (logger.isDebugEnabled()) {
                    logger.debug("key {} weight is {}, value is {}", key, this.weight.get(key), inputData.get(key));
                }
            }
        }
        score += this.intercept;
        double modelWeightHitRate = -1.0;
        double inputDataHitRate = -1.0;
        try {
            modelWeightHitRate = (double) modelWeightHitCount / weightNum;
            inputDataHitRate = (double) inputDataHitCount / inputFeaturesNum;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("model weight hit rate:{}", modelWeightHitRate);
            logger.debug("input data features hit rate:{}", inputDataHitRate);
        }
        Map<String, Double> ret = new HashMap<>(8);
        ret.put(Dict.SCORE, score);
        ret.put(Dict.MODEL_WRIGHT_HIT_RATE, modelWeightHitRate);
        ret.put(Dict.INPUT_DATA_HIT_RATE, inputDataHitRate);

        return ret;
    }

    public class LRTask  extends RecursiveTask<LRTaskResult>{

        double modelWeightHitRate = -1.0;
        double inputDataHitRate = -1.0;
        int  splitSize = MetaInfo.LR_SPLIT_SIZE;

        public LRTask(Map<String, Double> weight, Map<String, Object> inputData ,List<String>  keys){
            this.keys = keys;
            this.inputData = inputData;
            this.weight = weight;
        }
        List<String>  keys;
        Map<String, Object>  inputData;
        Map<String, Double>  weight;

        @Override
        protected LRTaskResult compute() {

            double score = 0;
            int modelWeightHitCount = 0;
            int inputDataHitCount = 0;

            if(keys.size()<=splitSize) {
                for (String key : keys) {
                    inputData.get(key);
                    if (this.weight.containsKey(key)) {
                        Double x = new Double(inputData.get(key).toString());
                        Double w = new Double(this.weight.get(key).toString());
                        score += w * x;
                        modelWeightHitCount += 1;
                        inputDataHitCount += 1;
                        if (logger.isDebugEnabled()) {
                            logger.debug("key {} weight is {}, value is {}", key, this.weight.get(key), inputData.get(key));
                        }
                    }
                }
            }else{
                List<List<Integer>> splits = new ArrayList<List<Integer>>();
                int size = keys.size();
                int count = (size + splitSize - 1) / splitSize;
                List<LRTask> subJobs = Lists.newArrayList();
                for (int i = 0; i < count; i++) {
                    List<String> subList = keys.subList(i * splitSize, ((i + 1) * splitSize > size ? size : splitSize * (i + 1)));
                   // logger.info("new subLRTask {}",i);
                    LRTask subLRTask = new LRTask(weight,inputData,subList);
                    subLRTask.fork();
                    subJobs.add(subLRTask);
                }
                for(LRTask lrTask:subJobs){
                    LRTaskResult subResult = lrTask.join();
                    if(subResult!=null){
                        score= score+subResult.score;
                        modelWeightHitCount =  modelWeightHitCount+subResult.modelWeightHitCount;
                        inputDataHitCount = inputDataHitCount + subResult.inputDataHitCount;
                    }
                }
            }
            LRTaskResult  lrTaskResult = new  LRTaskResult(score,modelWeightHitCount,inputDataHitCount);
            return lrTaskResult;
        };
    }

    public  class LRTaskResult  {
        public  LRTaskResult(double  score,int  modelWeightHitCount,int inputDataHitCount){
            this.score = score;
            this.modelWeightHitCount  = modelWeightHitCount;
            this.inputDataHitCount =  inputDataHitCount;
        }
        double score = 0;
        int modelWeightHitCount = 0;
        int inputDataHitCount = 0;
    }

    Map<String, Double> forwardParallel(List<Map<String, Object>> inputDatas) {
        Map<String, Object> inputData = inputDatas.get(0);
        //logger.info("size {}",inputData.size());
        Map<String, Double> ret = new HashMap<>(8);
        double modelWeightHitRate = -1.0;
        double inputDataHitRate = -1.0;

        int modelWeightHitCount = 0;
        int inputDataHitCount = 0;
        int weightNum = this.weight.size();
        int inputFeaturesNum = inputData.size();
        //logger.info("model weight number:{}", weightNum);
        if (logger.isDebugEnabled()) {
            logger.debug("model weight number:{}", weightNum);
            logger.debug("input data features number:{}", inputFeaturesNum);
        }
        double score = 0;
        for (String key : inputData.keySet()) {
            if (this.weight.containsKey(key)) {
                Double x = new Double(inputData.get(key).toString());
                Double w = new Double(this.weight.get(key).toString());
                score += w * x;
                modelWeightHitCount += 1;
                inputDataHitCount += 1;
                if (logger.isDebugEnabled()) {
                    logger.debug("key {} weight is {}, value is {}", key, this.weight.get(key), inputData.get(key));
                }
            }
        }
        ForkJoinTask<LRTaskResult>  result = forkJoinPool.submit(new LRTask(weight,inputData,Lists.newArrayList(inputData.keySet())));
        if(result!=null){
            try {
                LRTaskResult  lrTaskResult = result.get();
                score = lrTaskResult.score;
                modelWeightHitCount =  lrTaskResult.modelWeightHitCount;
                inputDataHitCount  =  lrTaskResult.inputDataHitCount;
                score += this.intercept;
                ret.put(Dict.SCORE, score);
                modelWeightHitRate = (double) modelWeightHitCount / weightNum;
                inputDataHitRate = (double) inputDataHitCount / inputFeaturesNum;
                ret.put(Dict.MODEL_WRIGHT_HIT_RATE,modelWeightHitRate);
                ret.put(Dict.INPUT_DATA_HIT_RATE, inputDataHitRate);
            }catch(Exception e){
                throw new  RuntimeException(e);
            }

        }

        return ret;
    }
}
