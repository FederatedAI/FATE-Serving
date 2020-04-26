package com.webank.ai.fate.serving.admin.rpc.services;

import com.webank.ai.fate.serving.admin.bean.Context;
import com.webank.ai.fate.serving.admin.bean.Dict;
import com.webank.ai.fate.serving.admin.bean.ReturnResult;
import com.webank.ai.fate.serving.admin.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.admin.rpc.core.FateService;
import com.webank.ai.fate.serving.admin.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.admin.rpc.core.OutboundPackage;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@FateService(name = Dict.SERVICE_NAME_MODEL_QUERY,
        preChain = {/*"paramValidator"*/}
)
public class ModelQueryService extends AbstractServiceAdaptor<Map, ReturnResult> {

    @Override
    public ReturnResult doService(Context context, InboundPackage<Map> data, OutboundPackage<ReturnResult> outboundPackage) {
        if (true) {
            throw new IllegalArgumentException();
        }
        return null;
    }

    @Override
    protected ReturnResult transformErrorMap(Context context, Map data) {
        return ReturnResult.failure((Integer) data.get(Dict.CODE), (String) data.get(Dict.MESSAGE));
    }
}
