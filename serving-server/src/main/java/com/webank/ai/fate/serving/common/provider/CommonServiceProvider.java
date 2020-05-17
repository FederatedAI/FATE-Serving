package com.webank.ai.fate.serving.common.provider;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceOuterClass;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import com.webank.ai.fate.serving.model.ModelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@FateService(name = "commonService", preChain = {
        "requestOverloadBreaker",
}, postChain = {
})
@Service
public class CommonServiceProvider extends AbstractServingServiceProvider {
    @Autowired
    FlowCounterManager flowCounterManager;


    @FateServiceMethod(name = "queryMetrics")
    public Object qeuryMetrics(Context context, InboundPackage data) {
        CommonServiceOuterClass.QueryMetricRequest queryMetricRequest = (CommonServiceOuterClass.QueryMetricRequest) data.getBody();

        return null;
    }




}
