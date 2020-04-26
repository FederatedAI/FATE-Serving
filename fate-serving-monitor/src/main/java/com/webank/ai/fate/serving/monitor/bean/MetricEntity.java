package com.webank.ai.fate.serving.monitor.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MetricEntity implements Comparable<MetricEntity> {
    // 用户认证参数
    @JsonIgnore
    private String userAuthKey;
    // 组件ID
    private String componentName;
    // 接口名称
    private String interfaceName;
    // 指标名称
//    private String attrName;
    // 上报值
//    private double metricValue;
    private Long passQps;

    private Long timestamp;

    public synchronized void addPassQps(Long passQps) {
        this.passQps += passQps;
    }

    public static MetricEntity copyOf(MetricEntity newEntity) {
        return MetricEntity.builder()
                .componentName(newEntity.getComponentName())
                .interfaceName(newEntity.getInterfaceName())
//                .attrName(newEntity.getAttrName())
//                .metricValue(newEntity.getMetricValue())
                .passQps(newEntity.getPassQps())
                .timestamp(newEntity.getTimestamp())
                .build();
    }

    @Override
    public int compareTo(MetricEntity o) {
        if (this.timestamp < o.getTimestamp()) {
            return -1;
        } else if (this.timestamp > o.getTimestamp()) {
            return 1;
        }
        return 0;
    }
}
