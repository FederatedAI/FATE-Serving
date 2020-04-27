package com.webank.ai.fate.serving.core.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    public MetricEntity() {
    }

    public String getUserAuthKey() {
        return userAuthKey;
    }

    public void setUserAuthKey(String userAuthKey) {
        this.userAuthKey = userAuthKey;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public synchronized void addPassQps(Long passQps) {
        this.passQps += passQps;
    }

    public static MetricEntity copyOf(MetricEntity newEntity) {
        MetricEntity metricEntity = new MetricEntity();
        metricEntity.setComponentName(newEntity.getComponentName());
        metricEntity.setInterfaceName(newEntity.getInterfaceName());
        metricEntity.setPassQps(newEntity.getPassQps());
        metricEntity.setTimestamp(newEntity.getTimestamp());
        return metricEntity;
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
