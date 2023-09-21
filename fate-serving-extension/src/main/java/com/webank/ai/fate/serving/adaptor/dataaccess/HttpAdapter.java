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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.ai.fate.serving.common.utils.HttpAdapterClientPool;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class HttpAdapter extends AbstractSingleFeatureDataAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(HttpAdapter.class);

    private final static String HTTP_ADAPTER_URL = MetaInfo.PROPERTY_HTTP_ADAPTER_URL;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void init() {
        environment.getProperty("port");
    }

    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
        ReturnResult returnResult = new ReturnResult();
        HttpAdapterResponse responseResult ;
        try {
            //get data by http
            responseResult = HttpAdapterClientPool.doPost(HTTP_ADAPTER_URL, featureIds);
            int responseCode = responseResult.getCode();
            switch (responseCode) {
                case HttpAdapterResponseCodeEnum.COMMON_HTTP_SUCCESS_CODE:
                    Map<String, Object> responseResultData = responseResult.getData();
                    if (responseResultData == null || responseResultData.size() == 0) {
                        returnResult.setRetcode(StatusCode.FEATURE_DATA_ADAPTOR_ERROR);
                        returnResult.setRetmsg("responseData is null ");
                    } else if (!responseResultData.get("code").equals(HttpAdapterResponseCodeEnum.SUCCESS_CODE)) {
                        returnResult.setRetcode(StatusCode.FEATURE_DATA_ADAPTOR_ERROR);
                        returnResult.setRetmsg("responseData is : " + objectMapper.writeValueAsString(responseResultData.get("data")));
                    } else {
                        ((Map<String, Object>)responseResultData.get("data")).remove("code");
                        returnResult.setRetcode(StatusCode.SUCCESS);
                        returnResult.setData(responseResultData);
                    }
                    break;

                case HttpAdapterResponseCodeEnum.ERROR_CODE:
                    returnResult.setRetcode(StatusCode.FEATURE_DATA_ADAPTOR_ERROR);
                    returnResult.setRetmsg(" data not found ");
                    break;

                default:
                    returnResult.setRetcode(StatusCode.FEATURE_DATA_ADAPTOR_ERROR);
                    returnResult.setRetmsg("responseCode unknown ");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("HttpAdapter result, {}", JsonUtil.object2Json(returnResult));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            returnResult.setRetcode(StatusCode.SYSTEM_ERROR);
        }
        return returnResult;
    }

    public static void main(String[] args) {

    }
}
