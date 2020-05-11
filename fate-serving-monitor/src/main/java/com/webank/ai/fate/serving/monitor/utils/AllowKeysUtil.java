package com.webank.ai.fate.serving.monitor.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class AllowKeysUtil {

    private static final Logger logger = LoggerFactory.getLogger(AllowKeysUtil.class);

    private static final Set<String> ALLOW_KEYS = new HashSet<>();

    public static void reload(JSONArray dataArray) {
        Set<String> tempSet = new HashSet<>();
        try {
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject data = dataArray.getJSONObject(i);
                Preconditions.checkArgument(data.get("userAuthKey") != null);
                Preconditions.checkArgument(data.get("componentName") != null);
                tempSet.add(data.getString("userAuthKey") + ":" + data.getString("componentName"));
            }
        } catch (Exception e) {
            logger.info("load allow keys file error", e);
        }

        if (tempSet.size() > 0) {
            ALLOW_KEYS.clear();
            ALLOW_KEYS.addAll(tempSet);
        }
    }

    public static boolean contains(String userAuthKey, String componentName) {
        if (ALLOW_KEYS.contains(userAuthKey + ":" + componentName)) {
            return true;
        }
        return false;
    }
}
