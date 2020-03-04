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


import com.alibaba.fastjson.JSONObject;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MockAdapter implements SingleFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(MockAdapter.class);

    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
        ReturnResult returnResult = new ReturnResult();
        Map<String, Object> data = new HashMap<>();
        try {
            String mockData = "x0:1.88669,x1:-1.359293,x2:2.303601,x3:2.001237,x4:1.307686,x5:2.616665,x6:2.109526,x7:2.296076,x8:2.750622,x9:1.937015";
            for (String kv : StringUtils.split(mockData, ",")) {
                String[] a = StringUtils.split(kv, ":");
                data.put(a[0], Double.valueOf(a[1]));
            }
            returnResult.setData(data);
            returnResult.setRetcode(InferenceRetCode.OK);

            if (logger.isDebugEnabled()) {
                logger.debug("MockAdapter result, {}", JSONObject.toJSONString(returnResult));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            returnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
        }
        return returnResult;
    }
}
