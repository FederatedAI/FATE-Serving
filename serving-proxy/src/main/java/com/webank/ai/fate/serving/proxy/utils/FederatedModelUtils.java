package com.webank.ai.fate.serving.proxy.utils;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.bean.EncryptMethod;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

//import com.webank.ai.fate.serving.core.bean.HostFederatedParams;

public class FederatedModelUtils {

    private static final String MODEL_KEY_SEPARATOR = "&";

    public static String genModelKey(String tableName, String namespace) {
        return StringUtils.join(Arrays.asList(tableName, namespace), MODEL_KEY_SEPARATOR);
    }

    public static String getModelRouteKey(Proxy.Packet packet) {
        String data = packet.getBody().getValue().toStringUtf8();

        Proxy.Model model = packet.getHeader().getTask().getModel();
//        HostFederatedParams requestData = JsonUtil.json2Object(data, HostFederatedParams.class);
//        ModelInfo partnerModelInfo = requestData.getPartnerModelInfo();

        String key = genModelKey(model.getTableName(), model.getNamespace());
        String md5Key = EncryptUtils.encrypt(key, EncryptMethod.MD5);
        return md5Key;
    }


}
