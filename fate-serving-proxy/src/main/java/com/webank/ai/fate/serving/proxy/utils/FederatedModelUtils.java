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

package com.webank.ai.fate.serving.proxy.utils;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.EncryptMethod;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class FederatedModelUtils {

    private static final Logger logger = LoggerFactory.getLogger(FederatedModelUtils.class);

    private static final String MODEL_KEY_SEPARATOR = "&";

    public static String genModelKey(String tableName, String namespace) {
        return StringUtils.join(Arrays.asList(tableName, namespace), MODEL_KEY_SEPARATOR);
    }

    public static String getModelRouteKey(Context context, Proxy.Packet packet) {
        String namespace;
        String tableName;
        if (StringUtils.isBlank(context.getVersion()) || Double.parseDouble(context.getVersion()) < 200) {
            // version 1.x
            String data = packet.getBody().getValue().toStringUtf8();
            Map hostFederatedParams = JsonUtil.json2Object(data, Map.class);
            Map partnerModelInfo = (Map) hostFederatedParams.get("partnerModelInfo");
            namespace = partnerModelInfo.get("namespace").toString();
            tableName = partnerModelInfo.get("name").toString();
        } else {
            // version 2.0.0+
            Proxy.Model model = packet.getHeader().getTask().getModel();
            namespace = model.getNamespace();
            tableName = model.getTableName();
        }

        String key = genModelKey(tableName, namespace);
        logger.info("get model route key by version: {} namespace: {} tablename: {}, key: {}", context.getVersion(), namespace, tableName, key);

        return EncryptUtils.encrypt(key, EncryptMethod.MD5);
    }

}
