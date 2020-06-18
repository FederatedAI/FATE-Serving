package com.webank.ai.fate.serving.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;

public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public static String object2Json(Object o) {
        if (o == null) {
            return null;
        }
        String s = "";
        try {
            s = mapper.writeValueAsString(o);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }

    public static <T> T json2Object(String json, Class<T> c) {
        if (StringUtils.isBlank(json))
            return null;
        T t = null;
        try {
            t = mapper.readValue(json, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }


    public static <T> T json2Object(byte[] json, Class<T> c) {
        T t = null;
        try {
            t = mapper.readValue(json, c);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }


    public static <T> T json2List(String json, TypeReference<T>  typeReference) {
        if (StringUtils.isBlank(json))
            return null;
        T result=null;
        try {
            result =  mapper.readValue(json,typeReference);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T json2Object(String json, TypeReference<T> tr) {
        if (StringUtils.isBlank(json))
            return null;

        T t = null;
        try {
            t = (T) mapper.readValue(json, tr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) t;
    }

}
