package com.webank.ai.fate.serving.common.provider;


import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.common.RouterMode;
import com.webank.ai.fate.register.common.ServiceWrapper;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
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
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@FateService(name = "commonService", preChain = {
        "requestOverloadBreaker",
}, postChain = {

})
@Service
public class CommonServiceProvider extends AbstractServingServiceProvider {

    @Autowired
    FlowCounterManager flowCounterManager;
    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;

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
        CommonServiceProto.MetricType type = queryMetricRequest.getType();
        List<MetricNode> metricNodes = null;
        if (type.equals(CommonServiceProto.MetricType.INTERFACE)) {
            metricNodes = flowCounterManager.queryMetrics(beginMs, endMs, sourceName);
            if (StringUtils.isBlank(sourceName)) {
                metricNodes = flowCounterManager.queryAllMetrics(beginMs, 300);
            }
        } else {
            metricNodes = flowCounterManager.queryModelMetrics(beginMs, endMs, sourceName);
        }
        CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
        String response = metricNodes != null ? JsonUtil.object2Json(metricNodes) : "";
        builder.setStatusCode(StatusCode.SUCCESS);
        builder.setData(ByteString.copyFrom(response.getBytes()));
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
        Map metaInfoMap = MetaInfo.toMap();
        Map map;
        if (StringUtils.isNotBlank(keyword)) {
            Map resultMap = Maps.newHashMap();
            metaInfoMap.forEach((k, v) -> {
                if (String.valueOf(k).toLowerCase().indexOf(keyword.toLowerCase()) > -1) {
                    resultMap.put(k, v);
                }
            });
            map = resultMap;
        } else {
            map = metaInfoMap;
        }
        builder.setData(ByteString.copyFrom(JsonUtil.object2Json(map).getBytes()));
        return builder.build();
    }

    @FateServiceMethod(name = "QUERY_JVM")
    public CommonServiceProto.CommonResponse listJvmMem(Context context, InboundPackage inboundPackage) {
        try {
//            CommonServiceProto.QueryJvmInfoRequest queryPropsRequest = (CommonServiceProto.QueryJvmInfoRequest) inboundPackage.getBody();
            CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
            builder.setStatusCode(StatusCode.SUCCESS);
//            Map map = Maps.newHashMap();
            List<JvmInfo> jvmInfos = JvmInfoCounter.getMemInfos();
            builder.setData(ByteString.copyFrom(JsonUtil.object2Json(jvmInfos).getBytes()));
            return builder.build();
        } catch (Exception e) {
            throw new SysException(e.getMessage());
        }
    }

    @FateServiceMethod(name = "UPDATE_SERVICE")
    public CommonServiceProto.CommonResponse updateService(Context context, InboundPackage inboundPackage) {
        try {
            Preconditions.checkArgument(zookeeperRegistry!=null);
            CommonServiceProto.UpdateServiceRequest request = (CommonServiceProto.UpdateServiceRequest) inboundPackage.getBody();
            String url = request.getUrl();
            String routerMode = request.getRouterMode();
            int weight = request.getWeight();
            long version = request.getVersion();
            URL originUrl = URL.valueOf(url);
            boolean hasChange = false;
            ServiceWrapper serviceWrapper = new ServiceWrapper();
            HashMap<String, String> parameters = Maps.newHashMap(originUrl.getParameters());
            if (RouterMode.contains(routerMode) && !routerMode.equalsIgnoreCase(originUrl.getParameter(Constants.ROUTER_MODE))) {
                parameters.put(Constants.ROUTER_MODE, routerMode);
                serviceWrapper.setRouterMode(routerMode);
                hasChange = true;
            }
            String originWeight = originUrl.getParameter(Constants.WEIGHT_KEY);
            if (weight != -1 && (originWeight == null || weight != Integer.parseInt(originWeight))) {
                parameters.put(Constants.WEIGHT_KEY, String.valueOf(weight));
                serviceWrapper.setWeight(weight);
                hasChange = true;
            }
            String originVersion = originUrl.getParameter(Constants.VERSION_KEY);
            if (version != -1 && (originVersion == null || version != Long.parseLong(originVersion))) {
                parameters.put(Constants.VERSION_KEY, String.valueOf(version));
                serviceWrapper.setVersion(version);
                hasChange = true;
            }
            CommonServiceProto.CommonResponse.Builder builder = CommonServiceProto.CommonResponse.newBuilder();
            builder.setStatusCode(StatusCode.SUCCESS);
            if (hasChange) {
                // update service cache map
                ConcurrentMap<String, ServiceWrapper> serviceCacheMap = zookeeperRegistry.getServiceCacheMap();
                ServiceWrapper cacheServiceWrapper = serviceCacheMap.get(originUrl.getEnvironment() + "/" + originUrl.getPath());
                if (cacheServiceWrapper == null) {
                    cacheServiceWrapper = new ServiceWrapper();
                }
                cacheServiceWrapper.update(serviceWrapper);
                serviceCacheMap.put(originUrl.getEnvironment() + "/" + originUrl.getPath(), cacheServiceWrapper);

                boolean success = zookeeperRegistry.tryUnregister(originUrl);
                if (success) {
                    URL newUrl = new URL(originUrl.getProtocol(), originUrl.getProject(), originUrl.getEnvironment(),
                            originUrl.getHost(), originUrl.getPort(), originUrl.getPath(), parameters);
                    zookeeperRegistry.register(newUrl);
                    builder.setMessage(Dict.SUCCESS);
                } else {
                    builder.setStatusCode(StatusCode.UNREGISTER_ERROR);
                    builder.setMessage("no node");
                }
            } else {
                builder.setMessage("no change");
            }
            return builder.build();
        } catch (Exception e) {
            throw new SysException(e.getMessage());
        }
    }

}
