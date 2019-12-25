package com.webank.ai.fate.serving.proxy.utils;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptUtil {

    public static final String UTF8 = "UTF-8";
    private static final String HMACSHA1 = "HmacSHA1";

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
            encryptText = new String(encryptText.getBytes(), EncryptUtil.UTF8);
            return Base64.getEncoder().encodeToString(EncryptUtil.HmacSHA1Encrypt(encryptText, appSecret));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
