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


import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;

import com.webank.ai.fate.serving.core.model.MergeInferenceAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.exp;

public class HeteroLRGuest extends HeteroLR implements MergeInferenceAware ,Returnable{

    private static final Logger logger = LoggerFactory.getLogger(HeteroLRGuest.class);

    private double sigmod(double x) {
        return 1. / (1. + exp(-x));
    }
    @Override
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> input
                                             ) {
        Map<String, Object> result = new HashMap<>(8);
        Map<String, Double> forwardRet = forward(input);
        double score = forwardRet.get(Dict.SCORE);
        logger.info("caseid {} score:{}", context.getCaseId(), score);
        result.put(Dict.SCORE, score);
        return result;
    }

    @Override
    public Map<String, Object> mergeRemoteInference(Context context, List<Map<String, Object>> guestData,
                                                    Map<String,Object> hostData) {
        Map<String, Object> result = new HashMap<>(8);
        result.put(Dict.RET_CODE,InferenceRetCode.OK);
        hostData.forEach((k,v)->{
                Map<String,Object> onePartyData=(Map<String,Object>) v;
                double score;
                Map<String,Object > tempMap =guestData.get(0);
                //logger.info("pppppppppppp  {}  ,{} ",tempMap,this.getComponentName());
                Map<String,Object> componentData = (Map<String,Object>)tempMap.get(this.getComponentName());
                //logger.info("componentData  {}",componentData);
                double localScore = ((Number) componentData.get(Dict.SCORE)).doubleValue();
                Map<String ,Object >  remoteComopnentData = (Map<String ,Object >)onePartyData.get(this.getComponentName());
                double remoteScore;
                if(remoteComopnentData!=null){
                    remoteScore = ((Number)remoteComopnentData.get(Dict.SCORE)).doubleValue();
                }else{
                    /**
                     *   此处是为兼容老版host
                     */
                    remoteScore = ((Number) hostData.get(Dict.SCORE)).doubleValue();
                }

                //logger.info("merge inference result,partid {} caseid {} local score:{} remote scope:{}",k ,context.getCaseId(), localScore, remoteScore);
                score = localScore;
                score += remoteScore;
                double prob = sigmod(score);
                result.put(Dict.SCORE, prob);

        });

        return result;
    }
}
