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

public class HeteroLRGuest extends HeteroLR implements MergeInferenceAware {

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


        try {
            double score;
            double localScore = (double) guestData.get(0).get(Dict.SCORE);
            double remoteScore = (double) hostData.get(Dict.SCORE);
            logger.info("merge inference result, caseid {} local score:{} remote scope:{}", context.getCaseId(), localScore, remoteScore);
            score = localScore;
            score += remoteScore;
            double prob = sigmod(score);
            result.put(Dict.SCORE, prob);
        } catch (Exception ex) {
            logger.error("hetero lr guest merge error:", ex);
            result.put(Dict.RET_CODE,InferenceRetCode.SYSTEM_ERROR);
        }
        return result;
    }
}
