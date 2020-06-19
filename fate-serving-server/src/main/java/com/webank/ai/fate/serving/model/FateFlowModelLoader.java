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

package com.webank.ai.fate.serving.model;

import com.google.common.collect.Maps;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.common.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.federatedml.PipelineModelProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FateFlowModelLoader extends AbstractModelLoader<Map<String, byte[]>> {
    private static final Logger logger = LoggerFactory.getLogger(FateFlowModelLoader.class);

    @Autowired(required = false)
    private RouterService routerService;

    @Autowired
    private Environment environment;

    @Override
    protected byte[] serialize(Context context, Map<String, byte[]> data) {
        Map<String, String> result = Maps.newHashMap();
        if (data != null) {
            data.forEach((k, v) -> {
                String base64String = new String(Base64.getEncoder().encode(v));
                result.put(k, base64String);
            });
            return JsonUtil.object2Json(result).getBytes();
        }
        return null;
    }

    @Override
    protected Map<String, byte[]> unserialize(Context context, byte[] data) {
        Map<String, byte[]> result = Maps.newHashMap();
        if (data != null) {
            String dataString = new String(data);
            Map originData = JsonUtil.json2Object(dataString, Map.class);
            if (originData != null) {
                originData.forEach((k, v) -> {
                    result.put(k.toString(), Base64.getDecoder().decode(v.toString()));
                });
                return result;
            }
        }
        return null;
    }

    @Override
    protected ModelProcessor initPipeLine(Context context, Map<String, byte[]> stringMap) {
        if (stringMap != null) {
            PipelineModelProcessor modelProcessor = new PipelineModelProcessor();
            modelProcessor.initModel(stringMap);
            return modelProcessor;
        } else {
            return null;
        }
    }

    @Override
    protected Map<String, byte[]> doLoadModel(Context context, ModelLoaderParam modelLoaderParam) {

        logger.info("read model, name: {} namespace: {}", modelLoaderParam.tableName, modelLoaderParam.nameSpace);
        try {
            String requestUrl = "";

            String filePath = modelLoaderParam.getFilePath();
            if (StringUtils.isNotBlank(filePath)) {
                requestUrl = URLDecoder.decode(filePath);
            } else if (routerService != null) {
                URL url = URL.valueOf("flow/online/transfer");
                List<URL> urls = routerService.router(url);
                if (urls == null || urls.isEmpty()) {
                    logger.info("register url not found, {}", url);
                } else {
                    url = urls.get(0);
                    requestUrl = url.toFullString();
                }
            }

            if (StringUtils.isBlank(requestUrl)) {
                requestUrl = MetaInfo.MODEL_TRANSFER_URL;
            }

            if (StringUtils.isBlank(requestUrl)) {
                logger.info("fateflow address not found");
                return null;
            }

            logger.info("use request url: {}", requestUrl);

            Map<String, Object> requestData = new HashMap<>(8);
            requestData.put("name", modelLoaderParam.tableName);
            requestData.put("namespace", modelLoaderParam.nameSpace);

            long start = System.currentTimeMillis();
            String responseBody = HttpClientPool.transferPost(requestUrl, requestData);
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("{}|{}|{}|{}", requestUrl, start, end, (end - start) + " ms");
            }
            if (StringUtils.isEmpty(responseBody)) {
                logger.info("read model fail, {}, {}", modelLoaderParam.tableName, modelLoaderParam.nameSpace);
                return null;
            }
            Map responseData = JsonUtil.json2Object(responseBody, Map.class);
            if (responseData.get(Dict.RET_CODE) != null && !responseData.get(Dict.RET_CODE).toString().equals(StatusCode.SUCCESS)) {
                logger.info("read model fail, {}, {}, {}", modelLoaderParam.tableName, modelLoaderParam.nameSpace, responseData.get("retmsg"));
                return null;
            }
            Map<String, byte[]> resultMap = new HashMap<>(8);
            Map<String, Object> dataMap = responseData.get(Dict.DATA) != null ? (Map<String, Object>) responseData.get(Dict.DATA) : null;
            if (dataMap == null || dataMap.isEmpty()) {
                logger.info("read model fail, {}, {}, {}", modelLoaderParam.tableName, modelLoaderParam.nameSpace, dataMap);
                return null;
            }
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                resultMap.put(entry.getKey(), Base64.getDecoder().decode(String.valueOf(entry.getValue())));
            }
            return resultMap;
        } catch (Exception e) {
            logger.error("get model info from fateflow error", e);
        }
        return null;
    }


}