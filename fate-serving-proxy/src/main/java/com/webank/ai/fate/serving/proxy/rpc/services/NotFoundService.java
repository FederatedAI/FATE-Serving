package com.webank.ai.fate.serving.proxy.rpc.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@FateService(name = "NotFound")
public class NotFoundService extends AbstractServiceAdaptor<String, Map> {
    @Override
    public Map doService(Context context, InboundPackage<String> data, OutboundPackage<Map> outboundPackage) {
        ReturnResult returnResult = ReturnResult.build(StatusCode.SERVICE_NOT_FOUND, "SERVICE_NOT_FOUND");
        return JSONObject.parseObject(JSON.toJSONString(returnResult), Map.class);
    }

    @Override
    public OutboundPackage<Map> serviceFail(Context context, InboundPackage<String> data, List<Throwable> e) {
        return null;
    }

    @Override
    protected Map transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        return null;
    }
}
