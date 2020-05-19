package com.webank.ai.fate.serving.core.flow;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.webank.ai.fate.serving.core.bean.MetricEntity;
import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class FlowCounterManager {

    Logger logger = LoggerFactory.getLogger(FlowCounterManager.class);

    String appName;

    MetricSearcher metricSearcher;

    public MetricSearcher getMetricSearcher() {
        return metricSearcher;
    }

    public void setMetricSearcher(MetricSearcher metricSearcher) {
        this.metricSearcher = metricSearcher;
    }

    public FlowCounterManager() {

    }

    public FlowCounterManager(String appName) {
        this.appName = appName;
        metricSearcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR, appName + "-metrics.log.pid" + GetSystemInfo.getPid());
        metricReport = new FileMetricReport(appName);
    }

    public  List<MetricNode>  queryMetrics(long beginTimeMs, long endTimeMs, String identity){
        try {
            return  metricSearcher.findByTimeAndResource(beginTimeMs, endTimeMs, identity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    public  List<MetricNode>  queryAllMetrics(long beginTimeMs, int size){
        try {
            return  metricSearcher.find(beginTimeMs, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  null;
    }

    MetricReport  metricReport;

    public MetricReport getMetricReport() {
        return metricReport;
    }

    public void setMetricReport(MetricReport metricReport) {
        this.metricReport = metricReport;
    }

    private ConcurrentHashMap<String,FlowCounter>   passMap =   new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,FlowCounter>   successMap =   new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,FlowCounter>   blockMap =   new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,FlowCounter>   exceptionMap =   new ConcurrentHashMap<>();

    public  boolean  pass(String sourceName){
        FlowCounter  flowCounter =passMap.get(sourceName);
        if(flowCounter==null){
            flowCounter= passMap.putIfAbsent(sourceName,new  FlowCounter(getAllowedQps(sourceName)));
            if(flowCounter==null) {
                flowCounter = passMap.get(sourceName);
            }
        }
        return  flowCounter.tryPass();
    }

    public  boolean  success(String sourceName){
        FlowCounter  flowCounter =successMap.get(sourceName);
        if(flowCounter==null){
            flowCounter= successMap.putIfAbsent(sourceName,new  FlowCounter(Integer.MAX_VALUE));
            if(flowCounter==null) {
                flowCounter = successMap.get(sourceName);
            }
        }
        return  flowCounter.tryPass();
    }

    public  boolean  block(String sourceName){
        FlowCounter  flowCounter =blockMap.get(sourceName);
        if(flowCounter==null){
            flowCounter= blockMap.putIfAbsent(sourceName,new  FlowCounter(Integer.MAX_VALUE));
            if(flowCounter==null) {
                flowCounter = blockMap.get(sourceName);
            }
        }
        return  flowCounter.tryPass();
    }

    public  boolean  exception(String sourceName){
        FlowCounter  flowCounter =exceptionMap.get(sourceName);
        if(flowCounter==null){
            flowCounter= exceptionMap.putIfAbsent(sourceName,new  FlowCounter(Integer.MAX_VALUE));
            if(flowCounter==null) {
                flowCounter = exceptionMap.get(sourceName);
            }
        }
        return  flowCounter.tryPass();
    }


    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    public  void  startReport(){

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                long current = TimeUtil.currentTimeMillis();
                List<MetricNode> reportList =   Lists.newArrayList();
                passMap.forEach((sourceName,flowCounter)->{

                    long  passCount =flowCounter.getSum();
                    FlowCounter successCounter = successMap.get(sourceName);
                    FlowCounter blockCounter =  blockMap.get(sourceName);
                    FlowCounter exceptionCounter = exceptionMap.get(sourceName);
                    MetricNode metricNode = new  MetricNode();
                    metricNode.setTimestamp(current);
                    metricNode.setResource(sourceName);
                    metricNode.setPassQps(passCount);
                    metricNode.setBlockQps(blockCounter!=null?new Double(blockCounter.getQps()).longValue():0);
                    metricNode.setExceptionQps(exceptionCounter!=null?new Double(exceptionCounter.getQps()).longValue():0);
                    metricNode.setSuccessQps(successCounter!=null?new Double(successCounter.getQps()).longValue():0);
                    reportList.add(metricNode);
                });
                metricReport.report(reportList);

            }
        }, 0,
        1,
        TimeUnit.SECONDS);
    }

    public  static  void main(String[] args){
        FlowCounterManager  flowCounterManager = new  FlowCounterManager();
        flowCounterManager.setMetricReport(new FileMetricReport("Test"));
        flowCounterManager.setMetricSearcher(new MetricSearcher(MetricWriter.METRIC_BASE_DIR, "Test" + "-metrics.log.pid" + GetSystemInfo.getPid()));
        flowCounterManager.startReport();

        while(true) {
            flowCounterManager.pass("test");
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static final String DEFAULT_CONFIG_FILE = "conf" + File.separator + "FlowRule.json";
    Map<String, Double> sourceQpsAllowMap = new HashMap<>();

    /**
     * init rules
     */
    public void init() {
        File file = new File(DEFAULT_CONFIG_FILE);
        logger.info("try to load flow counter rules, {}", file.getAbsolutePath());

        if (!file.exists()) {
            logger.error("flow counter rule config not found, {}", file.getAbsolutePath());
            return;
        }

        String result = "";
        try (
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader reader = new BufferedReader(inputStreamReader)
        ) {
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                result += tempString;
            }
        } catch (IOException e) {
            logger.error("load flow counter rules failed, use default setting, cause by: {}", e.getMessage());
        }

        JSONArray dataArray = JSONArray.parseArray(result);

        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject jsonObject = dataArray.getJSONObject(i);
            sourceQpsAllowMap.put(jsonObject.getString("source"), jsonObject.getDoubleValue("allow_qps"));
        }

        logger.info("load flow counter rules success");
    }

    private double getAllowedQps(String sourceName) {
        try {
            return sourceQpsAllowMap.get(sourceName);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    public void updateAllowQps(String sourceName, double allowQps) {
        sourceQpsAllowMap.put(sourceName, allowQps);
    }
}
