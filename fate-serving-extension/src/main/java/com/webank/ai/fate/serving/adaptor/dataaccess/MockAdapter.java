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

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MockAdapter extends AbstractSingleFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(MockAdapter.class);

    @Override
    public void init() {
        environment.getProperty("port");
    }

    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
        ReturnResult returnResult = new ReturnResult();
        Map<String, Object> data = new HashMap<>();
        try {
            String mockData = "x0:1,x1:5,x2:13,x3:58,x4:95,x5:352,x6:418,x7:833,x8:888,x9:937,x10:32776";
            for (String kv : StringUtils.split(mockData, ",")) {
                String[] a = StringUtils.split(kv, ":");
                data.put(a[0], Double.valueOf(a[1]));
            }
            returnResult.setData(data);
            returnResult.setRetcode(StatusCode.SUCCESS);

            if (logger.isDebugEnabled()) {
                logger.debug("MockAdapter result, {}", JsonUtil.object2Json(returnResult));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            returnResult.setRetcode(StatusCode.SYSTEM_ERROR);
        }
        return returnResult;
    }
}
