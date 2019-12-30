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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.networking.proxy.manager.ServerConfManager;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
public class AuthUtils {
    private static final Logger logger = LoggerFactory.getLogger(AuthUtils.class);
    private static String confFilePath = System.getProperty(Dict.AUTH_FILE);
    private static Map<String, String> KEY_SECRET_MAP = new HashMap<>();
    private static Map<String, String> PARTYID_KEY_MAP = new HashMap<>();
    private static int validRequestTimeoutSecond = 10;
    private static String applyId = "";
    private static boolean ifUseAuth = false;
    private static String selfPartyId = "";
    @Autowired
    private ToStringUtils toStringUtils;

    @Autowired
    private ServerConfManager serverConfManager;

    @Scheduled(fixedRate = 10000)
    public void loadConfig(){
        JsonParser jsonParser = new JsonParser();
        JsonReader jsonReader = null;
        JsonObject jsonObject = null;

        if (StringUtils.isEmpty(confFilePath)) {
            confFilePath = serverConfManager.getServerConf().getData(Dict.AUTH_FILE, "");
        }

        try {
            jsonReader = new JsonReader(new FileReader(confFilePath));
            jsonObject = jsonParser.parse(jsonReader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", confFilePath);
            throw new RuntimeException(e);
        } finally {
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (IOException ignore) {
                }
            }
        }
        selfPartyId = jsonObject.get("self_party_id").getAsString();
        ifUseAuth = jsonObject.get("if_use_auth").getAsBoolean();
        validRequestTimeoutSecond = jsonObject.get("request_expire_seconds").getAsInt();
        applyId = jsonObject.get("apply_id").getAsString();

        JsonArray jsonArray = jsonObject.getAsJsonArray("access_keys");
        Gson gson = new Gson();
        List<Map> allowKeys = gson.fromJson(jsonArray, ArrayList.class);
        KEY_SECRET_MAP.clear();
        for (Map allowKey : allowKeys) {
            KEY_SECRET_MAP.put(allowKey.get("app_key").toString(), allowKey.get("app_secret").toString());
            PARTYID_KEY_MAP.put(allowKey.get("party_id").toString(), allowKey.get("app_key").toString());
        }
        logger.debug("refreshed auth cfg using file {}.", confFilePath);
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
        String encryptText = String.valueOf(timestamp) + "\n"
                + toStringUtils.toOneLineString(header) + "\n"
                + toStringUtils.toOneLineString(body);
        encryptText = new String(encryptText.getBytes(), EncryptUtils.UTF8);
        signature = Base64.getEncoder().encodeToString(EncryptUtils.hmacSha1Encrypt(encryptText, appSecret));
        return signature;
    }

    public Proxy.Packet addAuthInfo(Proxy.Packet packet) throws Exception {
        if(!StringUtils.equals(selfPartyId, packet.getHeader().getDst().getPartyId())) {
            Proxy.Packet.Builder packetBuilder = packet.toBuilder();
            Proxy.AuthInfo.Builder authBuilder = packetBuilder.getAuthBuilder();

            long timestamp = System.currentTimeMillis();
            authBuilder.setTimestamp(timestamp);
            authBuilder.setApplyId(applyId);

            if(ifUseAuth) {
                String appKey = getAppKey(selfPartyId);
                authBuilder.setAppKey(appKey);
                String signature = calSignature(packet.getHeader(), packet.getBody(), timestamp, appKey);
                authBuilder.setSignature(signature);
            }

            packetBuilder.setAuth(authBuilder.build());
            return packetBuilder.build();
        }
        return packet;
    }

    public boolean checkAuthentication(Proxy.Packet packet) throws Exception {
        if(ifUseAuth
                && StringUtils.equals(selfPartyId, packet.getHeader().getDst().getPartyId())) {
            // check timestamp
            long currentTimeMillis = System.currentTimeMillis();
            long requestTimeMillis = packet.getAuth().getTimestamp();
            if (currentTimeMillis >= (requestTimeMillis + validRequestTimeoutSecond * 1000)) {
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

    /*@Override
    public void afterPropertiesSet() throws Exception {

        try {
            loadConfig();
        } catch (Throwable e) {
            logger.error("load authencation keys error", e);
        }

    }*/
}
