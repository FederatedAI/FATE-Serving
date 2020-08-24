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
import com.webank.ai.fate.serving.core.bean.EncryptMethod;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class FederatedModelUtils {

    private static final String MODEL_KEY_SEPARATOR = "&";

    public static String genModelKey(String tableName, String namespace) {
        return StringUtils.join(Arrays.asList(tableName, namespace), MODEL_KEY_SEPARATOR);
    }

    public static String getModelRouteKey(Proxy.Packet packet) {
        String data = packet.getBody().getValue().toStringUtf8();
        Proxy.Model model = packet.getHeader().getTask().getModel();
        String key = genModelKey(model.getTableName(), model.getNamespace());
        String md5Key = EncryptUtils.encrypt(key, EncryptMethod.MD5);
        return md5Key;
    }

}
