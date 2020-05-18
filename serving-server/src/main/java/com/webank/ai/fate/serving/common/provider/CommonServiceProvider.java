package com.webank.ai.fate.serving.common.provider;


import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import com.webank.ai.fate.serving.core.flow.MetricNode;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@FateService(name = "commonService", preChain = {
        "requestOverloadBreaker",
}, postChain = {
})
@Service
public class CommonServiceProvider extends AbstractServingServiceProvider {

    @Autowired
    FlowCounterManager flowCounterManager;

    /*@Override
    protected OutboundPackage serviceFailInner(Context context, InboundPackage data, Throwable e) {
        return super.serviceFailInner(context, data, e);
    }*/

    @Override
    protected Object transformErrorMap(Context context, Map data) {
        CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
        builder.setStatusCode(data.get(Dict.CODE).toString());
        builder.setMessage(data.get(Dict.MESSAGE).toString());
        return builder.build();
    }

    @FateServiceMethod(name = "QUERY_METRICS")
    public CommonServiceProto.CommonResponse queryMetrics(Context context, InboundPackage inboundPackage) {
        CommonServiceProto.QueryMetricRequest queryMetricRequest = (CommonServiceProto.QueryMetricRequest) inboundPackage.getBody();
        long beginMs = queryMetricRequest.getBeginMs();
        long endMs = queryMetricRequest.getEndMs();
        String sourceName = queryMetricRequest.getSource();
        List<MetricNode> metricNodes = flowCounterManager.queryMetrics(beginMs, endMs, sourceName);

        CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
        builder.setStatusCode(StatusCode.SUCCESS);
        builder.setData(ByteString.copyFrom(JSONObject.toJSONString(metricNodes).getBytes()));
        return builder.build();
    }




}
