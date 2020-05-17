package com.webank.ai.fate.serving.core.flow;

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetricEntity;
import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import com.webank.ai.fate.serving.core.utils.HttpClientPool;

import java.util.List;
import java.util.Map;

public class FileMetricReport implements MetricReport{

    MetricWriter  metricWriter= new MetricWriter("kaideng",1024*1024);


    @Override
    public void report(List<MetricNode> data) {
        long  currentTime =  TimeUtil.currentTimeMillis();
        try {
            metricWriter.write(TimeUtil.currentTimeMillis(),data);
        } catch (Exception e) {
            e.printStackTrace();
        }




    }
}
