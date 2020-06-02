package com.webank.ai.fate.serving.proxy.rpc.services;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;

import java.util.List;
import java.util.Map;

@FateService(name = "NotFound")
public class NotFoundService extends AbstractServiceAdaptor<String, String> {
    @Override
    public String doService(Context context, InboundPackage<String> data, OutboundPackage<String> outboundPackage) {
        Map result = Maps.newHashMap();
        result.put(Dict.CODE, StatusCode.SERVICE_NOT_FOUND);
        result.put(Dict.MESSAGE, "SERVICE_NOT_FOUND");
        return JSON.toJSONString(result);
    }

    @Override
    public OutboundPackage<String> serviceFail(Context context, InboundPackage<String> data, List<Throwable> e) {
        return null;
    }

    @Override
    protected String transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        return null;
    }
}
