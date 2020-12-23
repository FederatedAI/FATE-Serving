package com.webank.ai.fate.serving.common.interfaces;


import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class CustomInterfaceInstanceManager {

    private static Map<String, Object> interfaceMap = new HashMap<>();

    public static Object getInstanceForName(String className) {
        if (StringUtils.isBlank(className)) {
            return null;
        }
        Object classInstance = null;
        if (interfaceMap.containsKey(className)) {
            return interfaceMap.get(className);
        }
        try {
            Class newClass = Class.forName(className);
            classInstance = newClass.newInstance();
            interfaceMap.put(className, classInstance);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return classInstance;
    }

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        CustomInterfaceInstanceManager a = new CustomInterfaceInstanceManager();
        Object instanceForName = a.getInstanceForName("com.webank.ai.fate.serving.common.utils.DisruptorUtil");
        System.out.println("instanceForName111  :" + instanceForName);
    }

}
