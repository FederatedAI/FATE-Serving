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

package com.webank.ai.fate.serving.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObjectTransform {

    private static final Logger logger = LoggerFactory.getLogger(ObjectTransform.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public ObjectTransform() {
    }

    public static String bean2Json(Object object) {
        if (object == null) {
            return "";
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to convert object to JSON: {}", e.getMessage(), e);
            return "";
        }
    }

    public static <T> T json2Bean(String json, Class<T> objectType) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, objectType);
        } catch (Exception e) {
            logger.error("Failed to convert JSON to object: {}", e.getMessage(), e);
            return null;
        }
    }
}
