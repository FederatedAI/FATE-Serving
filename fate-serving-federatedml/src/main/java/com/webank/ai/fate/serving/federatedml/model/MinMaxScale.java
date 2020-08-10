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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MinMaxScale {
    private static final Logger logger = LoggerFactory.getLogger(MinMaxScale.class);
    public Map<String, Object> transform(Map<String, Object> inputData, Map<String, ColumnScaleParam> scales) {
        if (logger.isDebugEnabled()) {
            logger.info("Start MinMaxScale transform");
        }
        for (String key : inputData.keySet()) {
            try {
                if (scales.containsKey(key)) {
                    ColumnScaleParam scale = scales.get(key);
                    double value = Double.parseDouble(inputData.get(key).toString());
                    if (value > scale.getColumnUpper()) {
                        value = 1;
                    } else if (value < scale.getColumnLower()) {
                        value = 0;
                    } else {
                        double range = scale.getColumnUpper() - scale.getColumnLower();
                        if (range < 0) {
                            if (logger.isDebugEnabled()) {
                                logger.warn("min_max_scale range may be error, it should be larger than 0, but is {}, set value to 0 ", range);
                            }
                            value = 0;
                        } else {
                            if (Math.abs(range - 0) < 1e-6) {
                                range = 1;
                            }
                            value = (value - scale.getColumnLower()) / range;
                        }
                    }

                    inputData.put(key, value);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.warn("feature {} is not in scale, maybe missing or do not need to be scaled", key);
                    }
                }
            } catch (Exception ex) {
                //ex.printStackTrace();
                logger.error("MinMaxScale transform error", ex);
            }
        }
        return inputData;
    }
}
