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


import com.webank.ai.fate.core.mlmodel.buffer.FeatureSelectionMetaProto.FeatureSelectionMeta;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureSelectionParamProto.FeatureSelectionParam;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureSelectionParamProto.LeftCols;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import com.webank.ai.fate.serving.core.bean.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureSelection extends BaseModel {
    private static final Logger logger = LoggerFactory.getLogger(FeatureSelection.class);
    private FeatureSelectionParam featureSelectionParam;
    private FeatureSelectionMeta featureSelectionMeta;
    private LeftCols finalLeftCols;
    private boolean needRun;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        logger.info("start init Feature Selection class");
        this.needRun = false;
        try {
            this.featureSelectionMeta = this.parseModel(FeatureSelectionMeta.parser(), protoMeta);
            this.needRun = this.featureSelectionMeta.getNeedRun();
            this.featureSelectionParam = this.parseModel(FeatureSelectionParam.parser(), protoParam);
            this.finalLeftCols = featureSelectionParam.getFinalLeftCols();
        } catch (Exception ex) {
            ex.printStackTrace();
            return StatusCode.ILLEGALDATA;
        }
        logger.info("Finish init Feature Selection class");
        return StatusCode.OK;
    }

    @Override
    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {
        HashMap<String, Object> outputData = new HashMap<>(8);
        Map<String, Object> firstData = inputData.get(0);

        if (!this.needRun) {
            return firstData;
        }

        for (String key : firstData.keySet()) {
            if (this.finalLeftCols.getLeftCols().containsKey(key)) {
                Boolean isLeft = this.finalLeftCols.getLeftCols().get(key);
                if (isLeft) {
                    outputData.put(key, firstData.get(key));
                }
            }
        }
        return outputData;
    }

}
