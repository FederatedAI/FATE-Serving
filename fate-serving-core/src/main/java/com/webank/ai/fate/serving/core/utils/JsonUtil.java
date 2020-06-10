package com.webank.ai.fate.serving.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Maps;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
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

//    public static <T> List<String> listObject2ListJson(List<T> objects) {
//        if (objects == null)
//            return null;
//
//        List<String> lists = new ArrayList<String>();
//        for (T t : objects) {
//            lists.add(JsonUtil.object2Json(t));
//        }
//
//        return lists;
//    }

//    public static <T> List<T> listJson2ListObject(List<String> jsons, Class<T> c) {
//        if (jsons == null)
//            return null;
//
//        List<T> ts = new ArrayList<T>();
//        for (String j : jsons) {
//            ts.add(JsonUtil.json2Object(j, c));
//        }
//
//        return ts;
//    }

    public static <T> T json2Object(String json, Class<T> c) {
        if (StringUtils.hasLength(json) == false)
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
        if (StringUtils.hasLength(json) == false)
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
        if (StringUtils.hasLength(json) == false)
            return null;

        T t = null;
        try {
            t = (T) mapper.readValue(json, tr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) t;
    }


    public  static  void  main(String[] args){

            Map map = new HashMap();

            List  test = new ArrayList();
            map.put("xxx",test);

            Map inner = Maps.newHashMap();
            inner.put("cccc",3333);
            map.put("testInner",inner);
            test.add(1);
            test.add(2);
            test.add(3);



           String  x =  object2Json(map);





           System.err.println(x);

           Map  temp =json2Object(x,Map.class);

           System.err.println(temp);

//            List<Double>  list =  json2List(x, new TypeReference<List<Double>>() {
//            });

//            for(Object cd:list) {
//                System.err.println(cd.getClass());
//            }
    }


}
