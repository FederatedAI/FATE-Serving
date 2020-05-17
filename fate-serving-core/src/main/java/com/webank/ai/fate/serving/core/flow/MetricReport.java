package com.webank.ai.fate.serving.core.flow;

import com.webank.ai.fate.serving.core.bean.MetricEntity;

import java.util.List;

public interface MetricReport {

    public  void  report(List<MetricNode> metricEntity);
}
