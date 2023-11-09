package com.webank.ai.fate.serving.common.health;

import com.google.common.collect.Lists;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import java.util.List;
public  class HealthCheckResult {

    public List<HealthCheckRecord> getRecords() {
        return records;
    }

    public void setRecords(List<HealthCheckRecord> records) {
        this.records = records;
    }

    List<HealthCheckRecord>  records  = Lists.newArrayList();

    @Override
    public  String toString(){
        return JsonUtil.object2Json(this);
    }
}


