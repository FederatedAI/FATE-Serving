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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Outlier {
    private static final Logger logger = LoggerFactory.getLogger(Outlier.class);
    public HashSet<String> outlierValueSet;
    public Map<String, String> outlierReplaceValues;

    public Outlier(List<String> outlierValues, Map<String, String> outlierReplaceValue) {
        this.outlierValueSet = new HashSet<String>(outlierValues);
        this.outlierReplaceValues = outlierReplaceValue;
    }

    public Map<String, Object> transform(Map<String, Object> inputData) {
        if (inputData != null) {
            for (String key : inputData.keySet()) {
                if (inputData.get(key) != null) {
                    String value = inputData.get(key).toString();
                    if (this.outlierValueSet.contains(value.toLowerCase())) {
                        try {
                            inputData.put(key, outlierReplaceValues.get(key));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            inputData.put(key, 0.);
                        }
                    }
                }
            }
        }

        return inputData;
    }
}
