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

import com.webank.ai.fate.core.mlmodel.buffer.ScaleParamProto.ColumnScaleParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class StandardScale {
    private static final Logger LOGGER = LogManager.getLogger();

    public Map<String, Object> transform(Map<String, Object> inputData, Map<String, ColumnScaleParam> standardScalesMap) {
        LOGGER.info("Start StandardScale transform");
        for (String key : inputData.keySet()) {
            try {
                if (standardScalesMap.containsKey(key)) {
                    ColumnScaleParam standardScale = standardScalesMap.get(key);

                    double value = Double.parseDouble(inputData.get(key).toString());
                    double upper = standardScale.getColumnUpper();
                    double lower = standardScale.getColumnLower();
                    if (value > upper)
                        value = upper;
                    else if (value < lower)
                        value = lower;

                    double std = standardScale.getStd();
                    if (std == 0)
                        std = 1;

                    value = (value - standardScale.getMean()) / std;
                    inputData.put(key, value);
                } else {
                    LOGGER.warn("feature {} is not in scale, maybe missing or do not need to be scaled");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return inputData;
    }
}
