package com.webank.ai.fate.serving.proxy.rpc.provider;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import com.webank.ai.fate.serving.core.flow.JvmInfo;
import com.webank.ai.fate.serving.core.flow.JvmInfoCounter;
import com.webank.ai.fate.serving.core.flow.MetricNode;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@FateService(name = "commonService", preChain = {
        "requestOverloadBreaker"
}, postChain = {

})
@Service
public class CommonServiceProvider extends AbstractProxyServiceProvider {

    @Autowired
    FlowCounterManager flowCounterManager;

    @Autowired
    Environment environment;

    @Override
    protected Object transformExceptionInfo(Context context, ExceptionInfo exceptionInfo) {
        CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
        builder.setStatusCode(exceptionInfo.getCode());
        builder.setMessage(exceptionInfo.getMessage());
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

        MutablePropertySources propertySources = ((StandardEnvironment) environment).getPropertySources();
        propertySources.forEach(ps -> {
            if (ps instanceof ResourcePropertySource) {
                ResourcePropertySource propertySource = (ResourcePropertySource) ps;
                if (StringUtils.isNotBlank(keyword) && propertySource.containsProperty(keyword)) {
                    Map map = Maps.newHashMap();
                    map.put(keyword, propertySource.getProperty(keyword));
                    builder.setData(ByteString.copyFrom(JSONObject.toJSONString(map).getBytes()));
                } else {
                    builder.setData(ByteString.copyFrom(JSONObject.toJSONString(propertySource.getSource()).getBytes()));
                }
            }
        });
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
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

    }



}
