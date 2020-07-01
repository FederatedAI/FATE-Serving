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

package com.webank.ai.fate.serving.adaptor.dataaccess;

import com.webank.ai.fate.serving.core.bean.BatchHostFeatureAdaptorResult;
import com.webank.ai.fate.serving.core.bean.BatchHostFederatedParams;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.FeatureDataAdaptorException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchTestFilePickAdapter extends AbstractBatchFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(BatchTestFilePickAdapter.class);
    private static final Map<String, Map<String, Object>> featureMaps = new HashMap<>();

    @Override
    public void init() {
        try {
            if (featureMaps.isEmpty()) {
                List<String> lines = Files.readAllLines(Paths.get(System.getProperty(Dict.PROPERTY_USER_DIR), "host_data.csv"));
                for (int i = 0; i < lines.size(); i++) {
                    String[] idFeats = StringUtils.split(lines.get(i), ",");
                    if(idFeats.length == 2){
                        Map<String, Object> data = new HashMap<>();
                        for (String kv : StringUtils.split(idFeats[1], ";")) {
                            String[] a = StringUtils.split(kv, ":");
                            data.put(a[0], Double.valueOf(a[1]));
                        }
                        featureMaps.put(idFeats[0], data);
                    } else {
                        logger.error("please check the format for line " + (i + 1));
                        throw new FeatureDataAdaptorException("please check the host_data.csv format for line " + (i + 1));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("init BatchTestFilePick error, {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public BatchHostFeatureAdaptorResult getFeatures(Context context, List<BatchHostFederatedParams.SingleInferenceData> featureIdList) {

        BatchHostFeatureAdaptorResult batchHostFeatureAdaptorResult = new BatchHostFeatureAdaptorResult();

        try {
            featureIdList.forEach(singleInferenceData -> {
                Map<Integer, BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult> indexMap = batchHostFeatureAdaptorResult.getIndexResultMap();
                BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult singleBatchHostFeatureAdaptorResult = new BatchHostFeatureAdaptorResult.SingleBatchHostFeatureAdaptorResult();

                if (singleInferenceData.getSendToRemoteFeatureData().containsKey(Dict.DEVICE_ID)) {
                    Map<String, Object> featureData = featureMaps.get(singleInferenceData.getSendToRemoteFeatureData().get(Dict.DEVICE_ID));
                    if (featureData != null) {
                        Map clone = (Map) ((HashMap) featureData).clone();
                        singleBatchHostFeatureAdaptorResult.setFeatures(clone);
                        singleBatchHostFeatureAdaptorResult.setRetcode(StatusCode.SUCCESS);
                    } else {
                        logger.error("cant not find features for {}.", singleInferenceData.getSendToRemoteFeatureData().get(Dict.DEVICE_ID));
                        singleBatchHostFeatureAdaptorResult.setRetcode(StatusCode.HOST_FEATURE_NOT_EXIST);
                    }
                    indexMap.put(singleInferenceData.getIndex(), singleBatchHostFeatureAdaptorResult);
                }
            });
            batchHostFeatureAdaptorResult.setRetcode(StatusCode.SUCCESS);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            batchHostFeatureAdaptorResult.setRetcode(StatusCode.HOST_FEATURE_ERROR);
        }
        return batchHostFeatureAdaptorResult;
    }

    @Override
    public List<ParamDescriptor> desc() {
        return null;
    }
}
