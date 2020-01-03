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

package com.webank.ai.fate.serving.manger;

import com.alibaba.fastjson.JSONObject;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.bean.Configuration;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.FederatedRoles;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
import com.webank.ai.fate.serving.federatedml.PipelineTask;
import com.webank.ai.fate.serving.utils.HttpClientPool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class ModelUtils {
    private static final Logger logger = LoggerFactory.getLogger(ModelUtils.class);
    private static final String MODEL_KEY_SEPARATOR = "&";

    private static ModelUtils modelUtils;

    @Autowired
    private RouterService routerService;

    @PostConstruct
    private void init() {
        modelUtils = this;
    }

    public static Map<String, byte[]> readModel(String name, String namespace) {
        logger.info("read model, name: {} namespace: {}", name, namespace);

        String requestUrl = "";
        boolean useRegister = Boolean.valueOf(Configuration.getProperty(Dict.USE_REGISTER));
        if (useRegister) {
            URL url = URL.valueOf("flow/online/transfer");
            List<URL> urls = modelUtils.routerService.router(url);
            if (urls == null || urls.isEmpty()) {
                logger.info("url not found, {}", url);
                return null;
            }

            url = urls.get(0);
            requestUrl = url.toFullString();
        }

        if (StringUtils.isBlank(requestUrl)) {
            requestUrl = Configuration.getProperty(Dict.MODEL_TRANSFER_URL);
        }

        if (StringUtils.isBlank(requestUrl)) {
            logger.info("roll address not found");
            return null;
        }

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("name", name);
        requestData.put("namespace", namespace);

        long start = System.currentTimeMillis();
        String responseBody = HttpClientPool.transferPost(requestUrl, requestData);
        long end = System.currentTimeMillis();

        logger.info("{}|{}|{}|{}", requestUrl, start, end, (end - start) + " ms");

        if (StringUtils.isEmpty(responseBody)) {
            logger.info("read model fail, {}, {}", name, namespace);
            return null;
        }

        JSONObject responseData = JSONObject.parseObject(responseBody);
        if (responseData.getInteger("retcode") != 0) {
            logger.info("read model fail, {}, {}, {}", name, namespace, responseData.getString("retmsg"));
            return null;
        }

        Map<String, byte[]> resultMap = new HashMap<>();
        Map<String, Object> dataMap = responseData.getJSONObject("data");
        if (dataMap == null || dataMap.isEmpty()) {
            logger.info("read model fail, {}, {}, {}", name, namespace, dataMap);
            return null;
        }

        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            resultMap.put(entry.getKey(), Base64.getDecoder().decode(String.valueOf(entry.getValue())));
        }

        return resultMap;
    }

    public static PipelineTask loadModel(String name, String namespace) {
        Map<String, byte[]> modelBytes = readModel(name, namespace);
        if (modelBytes == null || modelBytes.size() == 0) {
            logger.info("loadModel error {} {}",name,namespace);
            return null;
        }
        PipelineTask pipelineTask = new PipelineTask();
        pipelineTask.initModel(modelBytes);
        return pipelineTask;
    }

    public static String genModelKey(String name, String namespace) {
        return StringUtils.join(Arrays.asList(name, namespace), MODEL_KEY_SEPARATOR);
    }

    public static String[] splitModelKey(String key) {
        return StringUtils.split(key, MODEL_KEY_SEPARATOR);
    }


    public static FederatedRoles getFederatedRoles(Map<String, ModelServiceProto.Party> federatedRolesProto) {
        FederatedRoles federatedRoles = new FederatedRoles();
        federatedRolesProto.forEach((roleName, party) -> {
            federatedRoles.setRole(roleName, party.getPartyIdList());
        });
        return federatedRoles;
    }

    public static Map<String, Map<String, ModelInfo>> getFederatedRolesModel(Map<String, ModelServiceProto.RoleModelInfo> federatedRolesModelProto) {
        Map<String, Map<String, ModelInfo>> federatedRolesModel = new HashMap<>(8);
        federatedRolesModelProto.forEach((roleName, roleModelInfo) -> {
            federatedRolesModel.put(roleName, new HashMap<>(8));
            roleModelInfo.getRoleModelInfoMap().forEach((partyId, modelInfo) -> {
                federatedRolesModel.get(roleName).put(partyId, new ModelInfo(modelInfo.getTableName(), modelInfo.getNamespace()));
            });
        });
        return federatedRolesModel;
    }
}