package com.webank.ai.fate.serving.monitor.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.serving.monitor.bean.MetricEntity;
import com.webank.ai.fate.serving.monitor.bean.ReturnResult;
import com.webank.ai.fate.serving.monitor.exceptions.AuthorizedException;
import com.webank.ai.fate.serving.monitor.repository.MetricsRepository;
import com.webank.ai.fate.serving.monitor.utils.AllowKeysUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
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

            return ReturnResult.success(metricEntity);
        };
    }

    // TODO: 2020/4/1 提供查询接口，统计
    @GetMapping("/fsm/queryMetric")
    public ReturnResult queryMetric(String componentName, String interfaceName, Long startTime, Long endTime) {
        Preconditions.checkArgument(StringUtils.isNotBlank(componentName));
        Preconditions.checkArgument(StringUtils.isNotBlank(interfaceName));

        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - 1000 * 60;
        }
        if (endTime - startTime > MAX_QUERY_INTERVAL_MS) {
            return ReturnResult.failure(-1, "time intervalMs is too big, must <= 1h");
        }

//        List<String> interfaceList = repository.listInterfaceOfComponent(componentName);
        List<MetricEntity> entities = repository.queryByComponentAndInterfaceBetween(componentName, interfaceName, startTime, endTime);

        Collections.sort(entities);

        return ReturnResult.success(entities);
    }
}
