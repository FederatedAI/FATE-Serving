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

import com.webank.ai.fate.serving.common.model.MergeInferenceAware;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.GuestMergeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scorecard extends BaseComponent implements Returnable, MergeInferenceAware{

    private static final Logger logger = LoggerFactory.getLogger(Scorecard.class);
    private static final double FLOAT_ZERO = 1e-8;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        return 0;
    }

    @Override
    public Object getParam() {
        return null;
    }

    @Override
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> input
    ) {
        // do nothing, calc in merge
        return new HashMap<>(0);
    }

    @Override
    public Map<String, Object> mergeRemoteInference(Context context, List<Map<String, Object>> localData, Map<String, Object> remoteData) {
        logger.error("score card merge input local {}, remote {}", localData, remoteData);
        Map<String, Object> data = localData.get(0);
        Map<String, Object> result = this.handleRemoteReturnData(remoteData);
        double localScore = 0;
        if ((int) result.get(Dict.RET_CODE) == StatusCode.SUCCESS) {
            if (data.get(Dict.SCORE) != null) {
                localScore = ((Number) data.get(Dict.SCORE)).doubleValue();
            } else {
                throw new GuestMergeException("local result is invalid ");
            }
            result.put(Dict.SCORE, this.scorecardCalc(localScore));
        }
        return result;
    }

    private double scorecardCalc(double score) {
        int upper_limit_ratio = 3;
        int offset = 500;
        int upper_limit_value = upper_limit_ratio * offset;
        int lower_limit_value = 0;

        double credit_score;
        if(Math.abs(score - 0) <= FLOAT_ZERO && score >= 0){
            credit_score = upper_limit_value;
        }else if(Math.abs(score - 1) <= FLOAT_ZERO && score > 0){
            credit_score = lower_limit_value;
        }else if(score > 1 || score < 0){
            credit_score = -1;
        } else{
            double odds = (double) ((1 - score) / score);
            int factor = 20;
            int factor_base = 2;
            credit_score = offset + factor / Math.log(factor_base) * Math.log(odds);
        }
        if (credit_score > upper_limit_value) {
            credit_score = upper_limit_value;
        }
        if(credit_score < lower_limit_value){
            credit_score = lower_limit_value;
        }
        credit_score = Math.round(credit_score * 100.0) / 100.0;
        return credit_score;
    }
}
