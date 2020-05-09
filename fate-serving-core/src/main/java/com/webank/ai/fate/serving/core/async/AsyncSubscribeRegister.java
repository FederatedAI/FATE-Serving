package com.webank.ai.fate.serving.core.async;

import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AsyncSubscribeRegister {

    public static final Map<String, Set<Method>> SUBSCRIBE_METHOD_MAP = new HashMap<>();

    public static final Map<Method,Object>  METHOD_INSTANCE_MAP  = Maps.newHashMap();

}
