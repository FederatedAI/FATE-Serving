package com.webank.ai.fate.serving.core.bean;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MetricEntity implements Comparable<MetricEntity> {

//    // 用户认证参数
//    @JsonIgnore
//    private String userAuthKey;
//    // 组件ID
//    private String componentName;
//    // 接口名称
//    private String interfaceName;

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    private String sourceName;
    // 指标名称
//    private String attrName;
    // 上报值
//    private double metricValue;
    private double passQps;
    private double successQps;

    public double getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(double successQps) {
        this.successQps = successQps;
    }

    public double getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(double blockQps) {
        this.blockQps = blockQps;
    }

    public double getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(double exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    private double blockQps;
    private double exceptionQps;

    private long timestamp;

    public MetricEntity() {
    }

    public static MetricEntity copyOf(MetricEntity newEntity) {
        MetricEntity metricEntity = new MetricEntity();

        metricEntity.setPassQps(newEntity.getPassQps());
        metricEntity.setTimestamp(newEntity.getTimestamp());
        return metricEntity;
    }

    @Override
    public  String toString(){
       return  JSON.toJSONString(this);
    }


    public double getPassQps() {
        return passQps;
    }

    public void setPassQps(double passQps) {
        this.passQps = passQps;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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
