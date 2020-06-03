package com.webank.ai.fate.serving.common.provider;


import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import com.webank.ai.fate.serving.core.flow.JvmInfo;
import com.webank.ai.fate.serving.core.flow.JvmInfoCounter;
import com.webank.ai.fate.serving.core.flow.MetricNode;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import org.apache.commons.lang3.StringUtils;
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

    @Override
    protected Object transformExceptionInfo(Context context, ExceptionInfo data) {
        CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
        builder.setStatusCode(data.getCode());
        builder.setMessage(data.getMessage());
        return builder.build();
    }

    @FateServiceMethod(name = "QUERY_METRICS")
    public CommonServiceProto.CommonResponse queryMetrics(Context context, InboundPackage inboundPackage) {
        CommonServiceProto.QueryMetricRequest queryMetricRequest = (CommonServiceProto.QueryMetricRequest) inboundPackage.getBody();
        long beginMs = queryMetricRequest.getBeginMs();
        long endMs = queryMetricRequest.getEndMs();
        String sourceName = queryMetricRequest.getSource();
        List<MetricNode> metricNodes;
        if (StringUtils.isNotEmpty(sourceName)) {
            metricNodes = flowCounterManager.queryMetrics(beginMs, endMs, sourceName);
        } else {
            metricNodes = flowCounterManager.queryAllMetrics(beginMs, 100);
        }
        CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
        builder.setStatusCode(StatusCode.SUCCESS);
        builder.setData(ByteString.copyFrom(JSON.toJSONString(metricNodes).getBytes()));
        return builder.build();
    }

    @FateServiceMethod(name = "UPDATE_FLOW_RULE")
    public CommonServiceProto.CommonResponse updateFlowRule(Context context, InboundPackage inboundPackage) {
        CommonServiceProto.UpdateFlowRuleRequest updateFlowRuleRequest = (CommonServiceProto.UpdateFlowRuleRequest) inboundPackage.getBody();
        flowCounterManager.updateAllowQps(updateFlowRuleRequest.getSource(), updateFlowRuleRequest.getAllowQps());
        CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
        builder.setStatusCode(StatusCode.SUCCESS);
        builder.setMessage(Dict.SUCCESS);
        return builder.build();
    }

    @FateServiceMethod(name = "LIST_PROPS")
    public CommonServiceProto.CommonResponse listProps(Context context, InboundPackage inboundPackage) {
        CommonServiceProto.QueryPropsRequest queryPropsRequest = (CommonServiceProto.QueryPropsRequest) inboundPackage.getBody();
        String keyword = queryPropsRequest.getKeyword();
        CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
        builder.setStatusCode(StatusCode.SUCCESS);
        Map map = Maps.newHashMap();
        builder.setData(ByteString.copyFrom(JSON.toJSONString(MetaInfo.toMap()).getBytes()));
        return builder.build();
    }

    @FateServiceMethod(name = "QUERY_JVM")
    public CommonServiceProto.CommonResponse listJvmMem(Context context, InboundPackage inboundPackage) {
        try {
            CommonServiceProto.QueryJvmInfoRequest queryPropsRequest = (CommonServiceProto.QueryJvmInfoRequest) inboundPackage.getBody();
            CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
            builder.setStatusCode(StatusCode.SUCCESS);
            Map map = Maps.newHashMap();
            List<JvmInfo> jvmInfos = JvmInfoCounter.getMemInfos();
            builder.setData(ByteString.copyFrom(JSON.toJSONString(jvmInfos).getBytes()));
            return builder.build();
        } catch (Exception e) {
            throw new SysException(e.getMessage());
        }

    }

}
