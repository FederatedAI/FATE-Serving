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

package com.webank.ai.fate.serving.adapter.dataaccess;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DTest implements SingleFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(DTest.class);

    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
        ReturnResult returnResult = new ReturnResult();
        Map<String, Object> requestData = new HashMap<>(8);
        requestData.putAll(featureIds);
        String responseBody = HttpClientPool.post("http://127.0.0.1:1234/feature", requestData);
        if (StringUtils.isEmpty(responseBody)) {
            return null;
        }
        Map<String, Object> tmp = (Map<String, Object>) ObjectTransform.json2Bean(responseBody, HashMap.class);
        if ((int) Optional.ofNullable(tmp.get(Dict.STATUS)).orElse(1) != 0) {
            return null;
        }
        String[] features = StringUtils.split(((List<String>) tmp.get(Dict.DATA)).get(0), "\t");
        Map<String, Object> featureData = new HashMap<>(8);
        for (int i = 1; i < features.length; i++) {
            featureData.put(features[i], i);
        }
        returnResult.setData(featureData);
        returnResult.setRetcode(InferenceRetCode.OK);
        return returnResult;
    }
}
