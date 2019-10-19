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

import com.webank.ai.fate.core.constant.StatusCode;
import com.webank.ai.fate.core.mlmodel.buffer.LRModelParamProto.LRModelParam;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HeteroLR extends BaseModel {
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<String, Double> weight;
    private Double intercept;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        LOGGER.info("start init HeteroLR class");
        try {
            LRModelParam lrModelParam = this.parseModel(LRModelParam.parser(), protoParam);

            this.weight = lrModelParam.getWeightMap();
            this.intercept = lrModelParam.getIntercept();
        } catch (Exception ex) {
            ex.printStackTrace();
            return StatusCode.ILLEGALDATA;
        }
        LOGGER.info("Finish init HeteroLR class, model weight is {}", this.weight);
        return StatusCode.OK;
    }

    Map<String, Double> forward(List<Map<String, Object>> inputDatas) {
        Map<String, Object> inputData = inputDatas.get(0);

        int modelWeightHitCount = 0;
        int inputDataHitCount = 0;
        int weightNum = this.weight.size();
        int inputFeaturesNum = inputData.size();
        LOGGER.info("model weight number:{}", weightNum);
        LOGGER.info("input data features number:{}", inputFeaturesNum);

        double score = 0;
        for (String key : inputData.keySet()) {
            if (this.weight.containsKey(key)) {
                Double x = new Double(inputData.get(key).toString());
                Double w = new Double(this.weight.get(key).toString());
                score += w * x;
                modelWeightHitCount += 1;
                inputDataHitCount += 1;
                LOGGER.info("key {} weight is {}, value is {}", key, this.weight.get(key), inputData.get(key));
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

        LOGGER.info("model weight hit rate:{}", modelWeightHitRate);
        LOGGER.info("input data features hit rate:{}", inputDataHitRate);

        Map<String, Double> ret = new HashMap<>();
        ret.put(Dict.SCORE, score);
        ret.put(Dict.MODEL_WRIGHT_HIT_RATE, modelWeightHitRate);
        ret.put(Dict.INPUT_DATA_HIT_RATE, inputDataHitRate);

        return ret;
    }

    @Override
    public abstract Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams);
}
