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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeteroFMHost extends HeteroFM {
    private static final Logger logger = LoggerFactory.getLogger(HeteroFMHost.class);

    @Override
    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {

        HashMap<String, Object> result = new HashMap<>();
        Map<String, Object> ret = forward(inputData);
        result.put(Dict.SCORE, ret.get(Dict.SCORE));
        result.put(Dict.FM_CROSS, ret.get(Dict.FM_CROSS));

        if(logger.isDebugEnabled()) {
            logger.debug("hetero fm host predict ends, result is {}", result);
        }

        return result;
    }

    @Override
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> input) {
        return null;
    }

    @Override
    public Map<String, Object> mergeRemoteInference(Context context, Map<String, Object> input) {
        return null;
    }
}
