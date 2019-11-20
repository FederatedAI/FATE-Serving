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

package com.webank.ai.fate.networking.proxy.util;

import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.api.networking.proxy.Proxy;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
public class AuthUtils implements InitializingBean{
    private static final Logger LOGGER = LogManager.getLogger();
    @Value("${auth.key.path}")
    private String keysFilePath;
    private static Map<String, String> ACCESS_KEYS_MAP = new HashMap<>();
    private static int validRequestTimeoutSecond = 10;
    private static String applyId = "";
    private static boolean ifUseAuth = false;
    @Autowired
    private ToStringUtils toStringUtils;

    @Scheduled(fixedRate = 10000)
    public void loadConfig(){
        JsonParser jsonParser = new JsonParser();
        JsonReader jsonReader = null;
        JsonObject jsonObject = null;
        try {
            jsonReader = new JsonReader(new FileReader(keysFilePath));
            jsonObject = jsonParser.parse(jsonReader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", keysFilePath);
            throw new RuntimeException(e);
        } finally {
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (IOException ignore) {
                }
            }
        }

        ifUseAuth = jsonObject.get("if_use_auth").getAsBoolean();
        validRequestTimeoutSecond = jsonObject.get("request_expire_seconds").getAsInt();
        applyId = jsonObject.get("apply_id").getAsString();

        JsonArray jsonArray = jsonObject.getAsJsonArray("access_keys");
        List<Map> allowKeys = gson.fromJson(jsonArray, ArrayList.class);
        ACCESS_KEYS_MAP.clear();
        for (Map allowKey : allowKeys) {
            ACCESS_KEYS_MAP.put(allowKey.get("appKey").toString(), allowKey.get("appSecret").toString());
        }
    }

    private String getSecret(String appKey) {
        return ACCESS_KEYS_MAP.get(appKey);
    }

    private String calSignature(Proxy.Metadata header, Proxy.Data body) {
        String signature = "";
        String appSecret = getSecret(header.getDst().getPartyId());
        if (StringUtils.isEmpty(appSecret)) {
            logger.error("appSecret not found");
            return signature;
        }
        String encryptText = String.valueOf(timestamp) + "\n"
                + toStringUtils.toOneLineString(header) + "\n"
                + toStringUtils.toOneLineString(body);
        encryptText = new String(encryptText.getBytes(), EncryptUtils.UTF8);
        signature = Base64.getEncoder().encodeToString(EncryptUtils.HmacSHA1Encrypt(encryptText, appSecret));
        return signature;
    }

    public Proxy.Packet.Builder addAuthInfo(Context context, Proxy.Metadata header, Proxy.Data body, Proxy.Packet.Builder packetBuilder) {

        Proxy.AuthInfo.Builder authBuilder = Proxy.AuthInfo.newBuilder();
        long timestamp = System.currentTimeMillis();
        authBuilder.setTimestamp(timestamp);
        authBuilder.setNonce(context.getCaseId());
        authBuilder.setApplyId(applyId);
        if(ifUseAuth){
            String signature = calSignature(header, body);
            authBuilder.setSignature(signature);
        }
        packetBuilder.setAuth(authBuilder.build());

        return packetBuilder;
    }

    public boolean checkAuthentication(Proxy.Packet packet) {
        if(ifUseAuth) {
            // check timestamp
            long currentTimeMillis = System.currentTimeMillis();
            long requestTimeMillis = packet.getAuth().getTimestamp();
            if (currentTimeMillis >= (requestTimeMillis + validRequestTimeoutSecond * 1000)) {
                logger.error("receive an expired request, currentTimeMillis:{}, requestTimeMillis{}.", currentTimeMillis, requestTimeMillis);
                return false;
            }
            // check signature
            String reqSignature = packet.getAuth().getSignature();
            String validSignature = calSignature(packet.getHeader(), packet.getBody());
            if (!StringUtils.equals(signature, validSignature)) {
                logger.error("invalid signature, request:{}, valid:{}", reqSignature, validSignature);
                return false;
            }
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        try {
            loadConfig();
        } catch (Throwable e) {
            LOGGER.error("load authencation keys error", e);
        }

    }
}
