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

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningMetaProto.FeatureBinningMeta;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningMetaProto.TransformMeta;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningParamProto.FeatureBinningParam;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningParamProto.FeatureBinningResult;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningParamProto.IVParam;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeteroFeatureBinning extends BaseComponent {
    private static final Logger logger = LoggerFactory.getLogger(HeteroFeatureBinning.class);
    private Map<String, List<Double>> splitPoints;
    private List<Long> transformCols;
    private List<String> header;
    private boolean needRun;
    private FeatureBinningParam featureBinningParam;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        logger.info("start init Feature Binning class");
        this.needRun = false;
        this.splitPoints = new HashMap<>(8);
        try {
            FeatureBinningMeta featureBinningMeta = this.parseModel(FeatureBinningMeta.parser(), protoMeta);
            this.needRun = featureBinningMeta.getNeedRun();
            TransformMeta transformMeta = featureBinningMeta.getTransformParam();
            this.transformCols = transformMeta.getTransformColsList();
            featureBinningParam = this.parseModel(FeatureBinningParam.parser(), protoParam);
            this.header = featureBinningParam.getHeaderList();
            FeatureBinningResult featureBinningResult = featureBinningParam.getBinningResult();
            Map<String, IVParam> binningResult = featureBinningResult.getBinningResultMap();
            for (String key : binningResult.keySet()) {
                IVParam oneColResult = binningResult.get(key);
                List<Double> splitPoints = oneColResult.getSplitPointsList();
                this.splitPoints.put(key, splitPoints);
            }
        } catch (Exception ex) {
            logger.error("init model error:", ex);
            return ILLEGALDATA;
        }
        logger.info("Finish init Feature Binning class");
        return OK;
    }

    @Override
    public Object getParam() {
        return featureBinningParam;
        //return JsonUtil.object2Objcet(featureBinningParam, new TypeReference<Map<String, Object>>() {});
    }

    @Override
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> inputData) {
        HashMap<String, Object> outputData = new HashMap<>(8);
        HashMap<String, Long> headerMap = new HashMap<>(8);
        Map<String, Object> firstData = inputData.get(0);
        if (!this.needRun) {
            return firstData;
        }
        for (int i = 0; i < this.header.size(); i++) {
            headerMap.put(this.header.get(i), (long) i);
        }
        for (String colName : firstData.keySet()) {
            try {
                if (!this.splitPoints.containsKey(colName)) {
                    outputData.put(colName, firstData.get(colName));
                    continue;
                }
                Long thisColIndex = headerMap.get(colName);
                if (!this.transformCols.contains(thisColIndex)) {
                    outputData.put(colName, firstData.get(colName));
                    continue;
                }
                List<Double> splitPoint = this.splitPoints.get(colName);
                Double colValue = Double.valueOf(firstData.get(colName).toString());
                int colIndex = Collections.binarySearch(splitPoint, colValue);
                if (colIndex < 0) {
                    colIndex = Math.min((- colIndex - 1), splitPoint.size() - 1);
                }
                outputData.put(colName, colIndex);
            } catch (Throwable e) {
                logger.error("HeteroFeatureBinning error", e);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("DEBUG: HeteroFeatureBinning output {}", outputData);
        }
        return outputData;
    }

}
