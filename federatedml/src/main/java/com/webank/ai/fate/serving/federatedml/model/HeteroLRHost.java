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

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeteroLRHost extends HeteroLR {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override

    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {


        LOGGER.info("hetero lr host begin to predict");
        HashMap<String, Object> result = new HashMap<>();
        Map<String, Double> ret = forward(inputData);
        result.put(Dict.SCORE, ret.get(Dict.SCORE));

        LOGGER.info("hetero lr host predict ends, result is {}", result);

        return result;
    }
}
