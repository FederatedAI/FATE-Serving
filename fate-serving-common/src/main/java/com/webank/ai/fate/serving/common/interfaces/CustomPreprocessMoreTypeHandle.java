package com.webank.ai.fate.serving.common.interfaces;

import com.webank.ai.fate.serving.core.bean.Context;

import java.util.Map;

public interface CustomPreprocessMoreTypeHandle<T,R> {
    public Map<String,Object> handle(Context context, T TData, R RData);

}
