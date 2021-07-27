package com.webank.ai.fate.serving.common.health;

import com.google.common.collect.Lists;

import java.util.List;






public  class HealthCheckResult {
    public List<HealthCheckRecord> getRecords() {
        return records;
    }

    public void setRecords(List<HealthCheckRecord> records) {
        this.records = records;
    }

    List<HealthCheckRecord>  records  = Lists.newArrayList();
}


