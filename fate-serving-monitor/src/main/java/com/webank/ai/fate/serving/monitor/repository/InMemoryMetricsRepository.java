/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.ai.fate.serving.monitor.repository;

import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.webank.ai.fate.serving.core.bean.MetricEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryMetricsRepository implements MetricsRepository<MetricEntity> {

    private static final long MAX_METRIC_LIVE_TIME_MS = 1000 * 60 * 5;

    /**
     * {@code componentName -> interface -> timestamp -> metric}
     */
    private Map<String, Map<String, ConcurrentLinkedHashMap<Long, MetricEntity>>> allMetrics = new ConcurrentHashMap<>();

    @Override
    public synchronized void save(MetricEntity entity) {
        if (entity == null || StringUtils.isBlank(entity.getComponentName())) {
            return;
        }

        allMetrics.computeIfAbsent(entity.getComponentName(), e -> new ConcurrentHashMap<>(16))
            .computeIfAbsent(entity.getInterfaceName(), e -> new ConcurrentLinkedHashMap.Builder<Long, MetricEntity>()
                .maximumWeightedCapacity(MAX_METRIC_LIVE_TIME_MS).weigher((key, value) -> {
                    // Metric older than {@link #MAX_METRIC_LIVE_TIME_MS} will be removed.
                    int weight = (int)(System.currentTimeMillis() - key);
                    // weight must be a number greater than or equal to one
                    return Math.max(weight, 1);
                }).build()).put(entity.getTimestamp(), entity);
    }

    @Override
    public synchronized void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        metrics.forEach(this::save);
    }

    @Override
    public synchronized List<MetricEntity> queryByComponentAndInterfaceBetween(String componentName, String interfaceName, long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<>();
        if (StringUtils.isBlank(componentName)) {
            return results;
        }
        Map<String, ConcurrentLinkedHashMap<Long, MetricEntity>> interfaceCount = allMetrics.get(componentName);
        if (interfaceCount == null) {
            return results;
        }
        ConcurrentLinkedHashMap<Long, MetricEntity> metricsMap = interfaceCount.get(interfaceName);
        if (metricsMap == null) {
            return results;
        }
        for (Map.Entry<Long, MetricEntity> entry : metricsMap.entrySet()) {
            if (entry.getKey() >= startTime && entry.getKey() <= endTime) {
                results.add(entry.getValue());
            }
        }
        return results;
    }

    @Override
    public List<String> listInterfaceOfComponent(String componentName) {
        List<String> results = new ArrayList<>();
        if (StringUtils.isBlank(componentName)) {
            return results;
        }
        // interface -> timestamp -> metric
        Map<String, ConcurrentLinkedHashMap<Long, MetricEntity>> interfaceMap = allMetrics.get(componentName);
        if (interfaceMap == null) {
            return results;
        }
        final long minTimeMs = System.currentTimeMillis() - 1000 * 60;
        Map<String, MetricEntity> interfaceCount = new ConcurrentHashMap<>(32);

        // key: interface, value: timestamp -> metric
        for (Map.Entry<String, ConcurrentLinkedHashMap<Long, MetricEntity>> interfaceMetrics : interfaceMap.entrySet()) {
            // timestamp -> metric
            for (Map.Entry<Long, MetricEntity> metrics : interfaceMetrics.getValue().entrySet()) {
                // Query only the last minute data
                if (metrics.getKey() < minTimeMs) {
                    continue;
                }
                MetricEntity newEntity = metrics.getValue();
                if (interfaceCount.containsKey(interfaceMetrics.getKey())) {
                    MetricEntity oldEntity = interfaceCount.get(interfaceMetrics.getKey());
                    oldEntity.addPassQps(newEntity.getPassQps());
                } else {
                    interfaceCount.put(interfaceMetrics.getKey(), MetricEntity.copyOf(newEntity));
                }
            }
        }
        // Order by last minute b_qps DESC.
        return interfaceCount.entrySet()
            .stream()
            .sorted((o1, o2) -> {
                MetricEntity e1 = o1.getValue();
                MetricEntity e2 = o2.getValue();
                /*int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                if (t != 0) {
                    return t;
                }*/
                return e2.getPassQps().compareTo(e1.getPassQps());
            })
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
