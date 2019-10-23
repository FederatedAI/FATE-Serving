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

package com.webank.ai.fate.serving.adapter.processing;

import com.webank.ai.fate.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.bean.PreProcessingResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PassPreProcessing implements PreProcessing {
    @Override
    public PreProcessingResult getResult(Context context, String paras) {
        PreProcessingResult preProcessingResult = new PreProcessingResult();
        preProcessingResult.setProcessingResult((Map<String, Object>) ObjectTransform.json2Bean(paras, HashMap.class));
        Map<String, Object> featureIds = new HashMap<>();
        Arrays.asList(Dict.DEVICE_ID, Dict.PHONE_NUM).forEach((field -> {
            featureIds.put(field, Optional.ofNullable(preProcessingResult.getProcessingResult().get(field)).orElse(""));
        }));
        preProcessingResult.setFeatureIds(featureIds);
        return preProcessingResult;
    }
}
