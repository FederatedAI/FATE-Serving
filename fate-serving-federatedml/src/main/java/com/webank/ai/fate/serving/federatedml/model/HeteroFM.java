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


import com.webank.ai.fate.core.mlmodel.buffer.fm.FMModelParamProto.Embedding;
import com.webank.ai.fate.core.mlmodel.buffer.fm.FMModelParamProto.FMModelParam;
import com.webank.ai.fate.serving.core.bean.Dict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HeteroFM extends BaseComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeteroFM.class);
    private Map<String, Double> weight;
    private Double intercept;
    private Map<String, Embedding> embedding;
    private int embedSize;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        logger.info("start init HeteroFM class");
        try {
            FMModelParam fmModelParam = this.parseModel(FMModelParam.parser(), protoParam);
            this.weight = fmModelParam.getWeightMap();
            this.intercept = fmModelParam.getIntercept();
            this.embedding = fmModelParam.getEmbeddingMap();
            this.embedSize = fmModelParam.getEmbedSize();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ILLEGALDATA;
        }
        logger.info("Finish init HeteroFM class, model weight is {}, model embedding is {}", this.weight, this.embedding);
        return OK;
    }

    Map<String, Object> forward(List<Map<String, Object>> inputDatas) {
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

        double[] multiplies = new double[this.embedSize];
        double[] squares = new double[this.embedSize];
        for (String key : this.embedding.keySet()) {
            if (inputData.containsKey(key)) {
                Double x = new Double(inputData.get(key).toString());
                List<Double> wList = this.embedding.get(key).getWeightList();
                for (int i = 0; i < this.embedSize; i++) {
                    multiplies[i] = multiplies[i] + wList.get(i) * x;
                    squares[i] = squares[i] + Math.pow(wList.get(i) * x, 2);
                }
            }
        }
        double cross = 0.0;
        for (int i = 0; i < this.embedSize; i++) {
            cross += (Math.pow(multiplies[i], 2) - squares[i]);
        }
        score += cross * 0.5;
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

        Map<String, Object> ret = new HashMap<>();
        ret.put(Dict.SCORE, score);
        ret.put(Dict.MODEL_WRIGHT_HIT_RATE, modelWeightHitRate);
        ret.put(Dict.INPUT_DATA_HIT_RATE, inputDataHitRate);
        ret.put(Dict.FM_CROSS, multiplies);

        return ret;
    }

}
