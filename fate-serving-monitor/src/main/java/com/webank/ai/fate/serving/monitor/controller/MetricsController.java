package com.webank.ai.fate.serving.monitor.controller;


import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.bean.MetricEntity;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.monitor.exceptions.AuthorizedException;
import com.webank.ai.fate.serving.monitor.repository.MetricsRepository;
import com.webank.ai.fate.serving.monitor.utils.AllowKeysUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@RestController
public class MetricsController {

    private static final Logger logger = LoggerFactory.getLogger(MetricsController.class);

    private static final long MAX_QUERY_INTERVAL_MS = 1000 * 60 * 60;

    @Autowired
    private MetricsRepository repository;

    @PostMapping("/fsm/sendMetric")
    public Callable<ReturnResult> sendMetric(@RequestBody String body, @RequestHeader HttpHeaders headers) {
        return () -> {
            if (logger.isDebugEnabled()) {
                logger.debug("receive: {} headers: {}", body, headers.toSingleValueMap());
            }

            MetricEntity metricEntity = JSONObject.parseObject(body, MetricEntity.class);
            Preconditions.checkArgument(metricEntity != null);

            if (!AllowKeysUtil.contains(metricEntity.getUserAuthKey(), metricEntity.getComponentName())) {
                throw new AuthorizedException("user auth key unavailable");
            }
            // TODO: 2020/4/1 持久化
            repository.save(metricEntity);

            ReturnResult result = new ReturnResult();
            result.setRetcode(StatusCode.SUCCESS);
            return result;
        };
    }

    // TODO: 2020/4/1 提供查询接口，统计
    @GetMapping("/fsm/queryMetric")
    public ReturnResult queryMetric(String componentName, String interfaceName, Long startTime, Long endTime) {
        Preconditions.checkArgument(StringUtils.isNotBlank(componentName));
        Preconditions.checkArgument(StringUtils.isNotBlank(interfaceName));

        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.SUCCESS);

        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - 1000 * 60;
        }
        if (endTime - startTime > MAX_QUERY_INTERVAL_MS) {
            result.setRetcode(StatusCode.PARAM_ERROR);
            result.setRetmsg("time intervalMs is too big, must <= 1h");
            return result;
        }

//        List<String> interfaceList = repository.listInterfaceOfComponent(componentName);
        List<MetricEntity> entities = repository.queryByComponentAndInterfaceBetween(componentName, interfaceName, startTime, endTime);

        Collections.sort(entities);

        Map data = Maps.newHashMap();
        data.put("entities", entities);
        result.setData(data);

        return result;
    }
}
