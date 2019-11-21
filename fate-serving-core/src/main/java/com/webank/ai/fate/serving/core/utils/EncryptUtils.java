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

import com.webank.ai.fate.serving.core.bean.EncryptMethod;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class EncryptUtils {

    public static final String UTF8 = "UTF-8";
    private static final String HMACSHA1 = "HmacSHA1";

    public static String encrypt(String originString, EncryptMethod encryptMethod) {
        try {
            MessageDigest m = MessageDigest.getInstance(getEncryptMethodString(encryptMethod));
            m.update(originString.getBytes("UTF8"));
            byte s[] = m.digest();
            String result = "";
            for (int i = 0; i < s.length; i++) {
                result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private static String getEncryptMethodString(EncryptMethod encryptMethod) {
        String methodString = "";
        switch (encryptMethod) {
            case MD5:
                methodString = "MD5";
                break;
            case SHA256:
                methodString = "SHA-256";
                break;
        }
        return methodString;
    }

    public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
        byte[] data = encryptKey.getBytes(UTF8);
        SecretKey secretKey = new SecretKeySpec(data, HMACSHA1);
        Mac mac = Mac.getInstance(HMACSHA1);
        mac.init(secretKey);

        byte[] text = encryptText.getBytes(UTF8);
        return mac.doFinal(text);
    }

    public static String generateSignature(String applyId, String timestamp, String nonce,
                                           String appKey, String appSecret, String uri, String body) {
        try {
            String encryptText = applyId + "\n" + timestamp + "\n" + nonce + "\n" + appKey + "\n" + uri + "\n" + body;
            encryptText = new String(encryptText.getBytes(), UTF8);
            return Base64.getEncoder().encodeToString(HmacSHA1Encrypt(encryptText, appSecret));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
