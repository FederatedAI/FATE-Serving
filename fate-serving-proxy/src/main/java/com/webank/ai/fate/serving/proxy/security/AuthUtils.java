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

package com.webank.ai.fate.serving.proxy.security;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.proxy.utils.FileUtils;
import com.webank.ai.fate.serving.proxy.utils.ToStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class AuthUtils implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);
    private static Map<String, String> KEY_SECRET_MAP = new HashMap<>();
    private static Map<String, String> PARTYID_KEY_MAP = new HashMap<>();
    private static int validRequestTimeoutSecond = 10;
    private static final long VALID_REQUEST_TIMEOUT_MILLIS = validRequestTimeoutSecond * 1000L;
    private static boolean ifUseAuth = false;
    private static String applyId = "";

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private final String userDir = System.getProperty(Dict.PROPERTY_USER_DIR);

    private final String DEFAULT_AUTH_FILE = "conf" + System.getProperty(Dict.PROPERTY_FILE_SEPARATOR) + "auth_config.json";
    private final String fileSeparator = System.getProperty(Dict.PROPERTY_FILE_SEPARATOR);

    @Autowired
    private ToStringUtils toStringUtils;

    private String selfPartyId;

    private String lastFileMd5 = "";

    @Scheduled(fixedRate = 30000)
    public void loadConfig() throws IOException {
        if (MetaInfo.PROPERTY_AUTH_OPEN) {

            String filePath;
            if (StringUtils.isNotEmpty(MetaInfo.PROPERTY_AUTH_FILE)) {
                filePath = MetaInfo.PROPERTY_AUTH_FILE;
            } else {
                filePath = userDir + this.fileSeparator + DEFAULT_AUTH_FILE;
            }
            logger.info("start refreshed auth config ,file path is {}", filePath);
            String fileMd5 = FileUtils.fileMd5(filePath);
            if (null != fileMd5 && fileMd5.equals(lastFileMd5)) {
                return;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                logger.error("auth table {} is not exist", filePath);
                return;
            }

            Gson gson = new Gson();
            JsonObject jsonObject;
            try (JsonReader jsonReader = new JsonReader(new FileReader(filePath))) {
                jsonObject = gson.fromJson(jsonReader, JsonObject.class);
            } catch (FileNotFoundException e) {
                logger.error("auth file not found: {}", filePath, e);
                throw new FileNotFoundException(e.getMessage());
            }

            selfPartyId = jsonObject.get("self_party_id").getAsString();
            applyId = jsonObject.get("apply_id") != null ? jsonObject.get("apply_id").getAsString() : "";
            ifUseAuth = jsonObject.get("if_use_auth").getAsBoolean();
            validRequestTimeoutSecond = jsonObject.get("request_expire_seconds").getAsInt();

            JsonArray jsonArray = jsonObject.getAsJsonArray("access_keys");
            List<Map> allowKeys = gson.fromJson(jsonArray, ArrayList.class);
            KEY_SECRET_MAP.clear();
            for (Map allowKey : allowKeys) {
                KEY_SECRET_MAP.put(allowKey.get("app_key").toString(), allowKey.get("app_secret").toString());
                PARTYID_KEY_MAP.put(allowKey.get("party_id").toString(), allowKey.get("app_key").toString());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("refreshed auth cfg using file {}.", filePath);
            }
            lastFileMd5 = fileMd5;
        }
    }

    private String getSecret(String appKey) {
        return KEY_SECRET_MAP.get(appKey);
    }

    private String getAppKey(String partyId) {
        return PARTYID_KEY_MAP.get(partyId);
    }

    private String calSignature(Proxy.Metadata header, Proxy.Data body, long timestamp, String appKey) throws Exception {
        String signature = "";
        String appSecret = getSecret(appKey);
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("appSecret not found");
            return signature;
        }

        StringBuilder encryptTextBuilder = new StringBuilder();
        encryptTextBuilder.append(timestamp).append("\n")
                .append(toStringUtils.toOneLineString(header)).append("\n")
                .append(toStringUtils.toOneLineString(body));
        return BASE64_ENCODER.encodeToString(EncryptUtils.hmacSha1Encrypt(new String(encryptTextBuilder.toString().getBytes(),
                StandardCharsets.UTF_8), appSecret));
    }

    public Proxy.Packet addAuthInfo(Proxy.Packet packet) throws Exception {
        if (MetaInfo.PROPERTY_AUTH_OPEN && !StringUtils.equals(selfPartyId, packet.getHeader().getDst().getPartyId())) {
            Proxy.Packet.Builder packetBuilder = packet.toBuilder();

            Proxy.AuthInfo.Builder authBuilder = packetBuilder.getAuthBuilder();
            long timestamp = System.currentTimeMillis();
            authBuilder.setTimestamp(timestamp);
            String appKey = getAppKey(selfPartyId);
            authBuilder.setAppKey(appKey);
            String signature = calSignature(packet.getHeader(), packet.getBody(), timestamp, appKey);
            authBuilder.setSignature(signature);
            authBuilder.setServiceId(packet.getAuth().getServiceId());
            authBuilder.setApplyId(StringUtils.defaultIfBlank(packet.getAuth().getApplyId(), applyId));
            packetBuilder.setAuth(authBuilder.build());
            return packetBuilder.build();
        }
        return packet;
    }

    public boolean checkAuthentication(Proxy.Packet packet) throws Exception {
        if (MetaInfo.PROPERTY_AUTH_OPEN) {
            // check timestamp
            long currentTimeMillis = System.currentTimeMillis();
            long requestTimeMillis = packet.getAuth().getTimestamp();
            if (currentTimeMillis >= (requestTimeMillis + VALID_REQUEST_TIMEOUT_MILLIS)) {
                logger.error("receive an expired request, currentTimeMillis:{}, requestTimeMillis{}.", currentTimeMillis, requestTimeMillis);
                return false;
            }
            // check signature
            String reqSignature = packet.getAuth().getSignature();
            String validSignature = calSignature(packet.getHeader(), packet.getBody(), requestTimeMillis, packet.getAuth().getAppKey());
            if (!StringUtils.equals(reqSignature, validSignature)) {
                logger.error("invalid signature, request:{}, valid:{}", reqSignature, validSignature);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() {
        try {
            loadConfig();
        } catch (Throwable e) {
            logger.error("load authentication keys fail. ", e);
        }
    }
}
