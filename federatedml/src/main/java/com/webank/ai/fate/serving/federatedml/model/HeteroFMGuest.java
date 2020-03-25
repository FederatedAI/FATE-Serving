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
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.model.MergeInferenceAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static java.lang.Math.exp;

public class HeteroFMGuest extends HeteroFM implements MergeInferenceAware {
    private static final Logger logger = LoggerFactory.getLogger(HeteroFMGuest.class);

    private double sigmod(double x) {
        return 1. / (1. + exp(-x));
    }

//    @Override
//    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {
//        Map<String, Object> result = new HashMap<>();
//        Map<String, Object> forwardRet = forward(inputData);
//        double score = new Double(forwardRet.get(Dict.SCORE).toString());
//        double[] guestCrosses = (double[]) forwardRet.get(Dict.FM_CROSS);
//
//
//        if(logger.isDebugEnabled()) {
//            logger.debug("caseid {} guest score:{}, cross data:{}", context.getCaseId(), score, guestCrosses);
//        }
//        try {
//            ReturnResult hostPredictResponse = this.getFederatedPredict(context, predictParams, Dict.FEDERATED_INFERENCE, true);
//            if(hostPredictResponse !=null) {
//                result.put(Dict.RET_CODE,hostPredictResponse.getRetcode());
//                if(logger.isDebugEnabled()) {
//                    logger.debug("caseid {} host response is {}",context.getCaseId(),hostPredictResponse.getData());
//                }
//                if (hostPredictResponse.getData() != null && hostPredictResponse.getData().get(Dict.SCORE) != null) {
//                    double hostScore = ((Number) hostPredictResponse.getData().get(Dict.SCORE)).doubleValue();
//                    List<Double> hostCrosses = JSON.parseArray(hostPredictResponse.getData().get(Dict.FM_CROSS).toString(),double.class);
//                    logger.info("caseid {} host score:{}, cross data: {}",context.getCaseId(), hostScore, hostCrosses);
//                    score += hostScore;
//                    if (hostCrosses == null || hostCrosses.size() != guestCrosses.length) {
//                        throw new RuntimeException("the length of the cross part is not match");
//                    }
//                    for (int i = 0; i < guestCrosses.length; i++) {
//                        score += hostCrosses.get(i) * guestCrosses[i];
//                    }
//                }
//            }else{
//                logger.info("caseid {} host response is null",context.getCaseId());
//            }
//        } catch (io.grpc.StatusRuntimeException ex) {
//            logger.error("merge host predict failed:", ex);
//            result.put(Dict.RET_CODE, InferenceRetCode.NETWORK_ERROR);
//        }
//        catch(Exception ex){
//            logger.error("merge host predict failed:", ex);
//            result.put(Dict.RET_CODE, InferenceRetCode.SYSTEM_ERROR);
//        }
//        double prob = sigmod(score);
//        result.put(Dict.PROB, prob);
//        result.put(Dict.GUEST_MODEL_WEIGHT_HIT_RATE + ":{}", forwardRet.get(Dict.MODEL_WRIGHT_HIT_RATE));
//        result.put(Dict.GUEST_INPUT_DATA_HIT_RATE + ":{}", forwardRet.get(Dict.INPUT_DATA_HIT_RATE));
//        return result;
//    }

    @Override
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> input) {

        Map<String, Object> forwardRet = forward(input);

        return  forwardRet;
    }



    @Override
    public Map<String, Object> mergeRemoteInference(Context context, List<Map<String, Object>> localDataList, Map<String, Object> remoteData) {
        Map<String, Object> result = Maps.newHashMap();
        Map<String, Object> localData = localDataList.get(0);
        try {
            Preconditions.checkArgument(localData != null);
            Preconditions.checkArgument(remoteData != null);
            Preconditions.checkArgument(localData.get(Dict.SCORE) != null);
            Preconditions.checkArgument(localData.get(Dict.FM_CROSS) != null);
            Preconditions.checkArgument(remoteData.get(Dict.RET_CODE) != null);
            String remoteCode = remoteData.get(Dict.RET_CODE).toString();
            double localScore = ((Number) localData.get(Dict.SCORE)).doubleValue();
            double[] guestCrosses = (double[]) localData.get(Dict.FM_CROSS);
            localData.get(Dict.FM_CROSS);
            double score = localScore;
            result.put(Dict.RET_CODE, remoteCode);
            if (remoteCode.equals(InferenceRetCode.OK)) {
                double hostScore = ((Number) remoteData.get(Dict.SCORE)).doubleValue();
                Preconditions.checkArgument(remoteData.get(Dict.FM_CROSS) != null);
                List<Double> hostCrosses = JSON.parseArray(remoteData.get(Dict.FM_CROSS).toString(), double.class);
                Preconditions.checkArgument(hostCrosses.size() == guestCrosses.length,"");
                score += hostScore;
                for (int i = 0; i < guestCrosses.length; i++) {
                    score += hostCrosses.get(i) * guestCrosses[i];
                }
                double prob = sigmod(score);
                result.put(Dict.SCORE, prob);
                result.put(Dict.GUEST_MODEL_WEIGHT_HIT_RATE, localData.get(Dict.MODEL_WRIGHT_HIT_RATE));
                result.put(Dict.GUEST_INPUT_DATA_HIT_RATE, localData.get(Dict.INPUT_DATA_HIT_RATE));
            }
        }catch (Exception  e){
            logger.error("hetero fm guest merge remote error",e);
            // TODO: 2020/3/19    错误码之后再定 ，目前先用这个
            result.put(Dict.RET_CODE,InferenceRetCode.SYSTEM_ERROR);

        }
        return   result;

    }
}
