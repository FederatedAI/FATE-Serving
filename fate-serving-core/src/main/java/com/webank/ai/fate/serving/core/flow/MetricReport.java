package com.webank.ai.fate.serving.core.flow;

import java.util.List;

public interface MetricReport {

    public  void  report(List<MetricNode> metricNodes);
}
