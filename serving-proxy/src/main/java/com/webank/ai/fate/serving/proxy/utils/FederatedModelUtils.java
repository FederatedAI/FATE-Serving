package com.webank.ai.fate.serving.proxy.utils;

import com.alibaba.fastjson.JSON;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.bean.EncryptMethod;
import com.webank.ai.fate.serving.core.bean.HostFederatedParams;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class FederatedModelUtils {

    private static final String MODEL_KEY_SEPARATOR = "&";

    public static String genModelKey(String name, String namespace) {
        return StringUtils.join(Arrays.asList(name, namespace), MODEL_KEY_SEPARATOR);
    }

    public static String getModelRouteKey(Proxy.Packet  packet) {
        String data = packet.getBody().getValue().toStringUtf8();
        HostFederatedParams requestData = JSON.parseObject(data, HostFederatedParams.class);
        ModelInfo partnerModelInfo = requestData.getPartnerModelInfo();
        String key =genModelKey(partnerModelInfo.getName(), partnerModelInfo.getNamespace());
        String md5Key = EncryptUtils.encrypt(key, EncryptMethod.MD5);
        return md5Key;
    }



}
