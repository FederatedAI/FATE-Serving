package com.webank.ai.fate.serving.proxy.rpc.services;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.exceptions.ErrorCode;
import com.webank.ai.fate.serving.core.rpc.core.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.ProxyService;
import com.webank.ai.fate.serving.proxy.common.Dict;
import com.webank.ai.fate.serving.proxy.rpc.core.AbstractServiceAdaptor;

import java.util.List;
import java.util.Map;

@ProxyService(name ="NotFound")
public class NotFoundService  extends AbstractServiceAdaptor<String, String> {
    @Override
    public String doService(Context context, InboundPackage<String> data, OutboundPackage<String> outboundPackage) {
        Map result = Maps.newHashMap();
        result.put(Dict.CODE, ErrorCode.SERVICE_NOT_FOUND);
        result.put(Dict.MESSAGE,"SERVICE_NOT_FOUND");
        return  JSON.toJSONString(result);
    }

    @Override
    public OutboundPackage<String> serviceFail(Context context, InboundPackage<String> data, List<Throwable> e) throws Exception {
        return null;
    }

    @Override
    protected String transformErrorMap(Context context,Map data) {
        return null;
    }
}
