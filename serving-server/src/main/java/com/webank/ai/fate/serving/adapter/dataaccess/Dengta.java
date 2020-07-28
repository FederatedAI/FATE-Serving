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
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.bean.EncryptMethod;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Dengta implements FeatureData {
    private static final Logger logger = LoggerFactory.getLogger(MockAdapter.class);

    @Override
    public ReturnResult getData(Context context, Map<String, Object> featureIds) {
        ReturnResult returnResult = new ReturnResult();
        try {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("accessId", 1);
            requestData.put("accessKey", "1");
            requestData.put("userType", 1);
            requestData.put("tagIds", Arrays.asList(1003301));
            requestData.put("requestId", "1");

            String userId = getUserId(featureIds);
            if (StringUtils.isBlank(userId)) {
                logger.error("feature userId is blank");
                returnResult.setRetcode(InferenceRetCode.INVALID_FEATURE);
                return returnResult;
            }

            requestData.put("userId", userId);
            if (StringUtils.isEmpty(requestData.get("userId").toString())) {
                returnResult.setRetcode(InferenceRetCode.EMPTY_DATA);
                logger.info("request {} remote feature use 0 ms, retcode is {}", featureIds.get("device_id"), returnResult.getRetcode());
                return returnResult;
            }
            long startTime = System.currentTimeMillis();
            String responseBody = HttpClientPool.post("http://webank.datashare.sparta.html5.qq.com/datashare/query_tag", requestData);
            long endTime = System.currentTimeMillis();
            long elapsed = endTime - startTime;
            if (StringUtils.isEmpty(responseBody)) {
                returnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
                logger.info("request {} remote feature use {} ms, retcode is {}", featureIds.get("device_id"), elapsed, returnResult.getRetcode());
                return returnResult;
            }
            Map<String, Object> responseData = (Map<String, Object>) ObjectTransform.json2Bean(responseBody, HashMap.class);
            int responseRetcode = (int) Optional.ofNullable(responseData.get("exitCode")).orElse(101);
            switch (responseRetcode) {
                case 101:
                    returnResult.setRetcode(InferenceRetCode.GET_FEATURE_FAILED);
                    return returnResult;
                case 102:
                    returnResult.setRetcode(InferenceRetCode.INVALID_FEATURE);
                    return returnResult;
                case 103:
                    returnResult.setRetcode(InferenceRetCode.NO_FEATURE);
                    return returnResult;
            }
            String featureString = responseData.get("data").toString().replace("[", "").replace("]", "");
            String[] features = StringUtils.split(featureString, "\t");
            Map<String, Object> featureData = new HashMap<>();
            for (int i = 1; i < features.length; i++) {
                featureData.put(features[i], 1);
            }
            logger.info(JSONObject.toJSONString(featureData));
            returnResult.setData(featureData);
            if (featureData.size() > 0) {
                returnResult.setRetcode(InferenceRetCode.OK);
            } else {
                returnResult.setRetcode(InferenceRetCode.NO_FEATURE);
            }
            logger.info("request {} remote feature use {} ms, retcode is {}", featureIds.get("device_id"), elapsed, returnResult.getRetcode());
            return returnResult;
        } catch (Exception ex) {
            returnResult.setRetcode(InferenceRetCode.DEAL_FEATURE_FAILED);
            returnResult.setRetmsg(String.format("get feature data from remote failed: %s", ex.getMessage()));
            logger.error(String.format("get feature data from remote failed %s :", ObjectTransform.bean2Json(featureIds)), ex);
            return returnResult;
        }
    }

    private String getUserId(Map<String, Object> featureIds) {
        String deviceId = (String) featureIds.get("device_id");
        String encryptType = (String) featureIds.get("encrypt_type");
        if (StringUtils.isBlank(deviceId)) {
            return deviceId;
        }
        if (StringUtils.isBlank(encryptType) || encryptType.equalsIgnoreCase("raw")) {
            return EncryptUtils.encrypt(deviceId, EncryptMethod.MD5);
        } else if (encryptType.equalsIgnoreCase("md5")) {
            return deviceId;
        } else {
            logger.error("encryptType: {} not support", encryptType);
            return null;
        }
    }

}
