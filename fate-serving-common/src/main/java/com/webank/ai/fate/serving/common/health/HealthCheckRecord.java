package com.webank.ai.fate.serving.common.health;

import com.webank.ai.fate.serving.core.utils.JsonUtil;

public class HealthCheckRecord{
    String  checkItemName;
    String  msg;

    public String getCheckItemName() {
        return checkItemName;
    }

    public void setCheckItemName(String checkItemName) {
        this.checkItemName = checkItemName;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public HealthCheckStatus getHealthCheckStatus() {
        return healthCheckStatus;
    }

    public void setHealthCheckStatus(HealthCheckStatus healthCheckStatus) {
        this.healthCheckStatus = healthCheckStatus;
    }

    HealthCheckStatus  healthCheckStatus;
    public HealthCheckRecord(){

    }
    public HealthCheckRecord(String checkItemName, String msg, HealthCheckStatus healthCheckStatus) {
        this.checkItemName = checkItemName;
        this.msg = msg;
        this.healthCheckStatus = healthCheckStatus;
    }

    @Override
    public String  toString(){
        return JsonUtil.object2Json(this);
    }
}