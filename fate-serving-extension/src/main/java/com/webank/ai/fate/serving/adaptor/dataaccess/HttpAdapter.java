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

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.webank.ai.fate.serving.common.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class HttpAdapter extends AbstractSingleFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(HttpAdapter.class);

    private final static String HTTP_ADAPTER_URL = MetaInfo.PROPERTY_HTTP_ADAPTER_URL;

    @Override
    public void init() {
        environment.getProperty("port");
    }

    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
        ReturnResult returnResult = new ReturnResult();
        Map<String, Object> data = new HashMap<>();
        try {
            //get data by http
            String bodyJsonString = JsonUtil.object2Json(featureIds);
            String responseBody = HttpClientPool.doPost(HTTP_ADAPTER_URL, bodyJsonString);
            logger.info("responseBody = {" + responseBody + "}");
            if (StringUtils.isBlank(responseBody)) {
                returnResult.setRetcode(StatusCode.HOST_FEATURE_NOT_EXIST);
                return returnResult;
            }

            try {
                data = JsonUtil.json2Object(responseBody, Map.class);
            } catch (Exception e) {
                logger.error(e.getMessage());
                returnResult.setRetcode(StatusCode.HOST_PARAM_ERROR);
                returnResult.setRetmsg("responseBody not is not json string ");
                return returnResult;
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
