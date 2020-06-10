package com.webank.ai.fate.serving.core.flow;


import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.timer.HashedWheelTimer;
import com.webank.ai.fate.serving.core.timer.Timeout;
import com.webank.ai.fate.serving.core.timer.TimerTask;
import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FlowCounterManager {

    Logger logger = LoggerFactory.getLogger(FlowCounterManager.class);

    String appName;
    MetricSearcher metricSearcher;
    MetricSearcher modelMetricSearcher;
    boolean  countModelRequest;

    LimitQueue<MetricNode>  modelLimitQueue = new  LimitQueue<MetricNode>(10);

    public static final boolean USE_PID = true;

    HashedWheelTimer   hashedWheelTimer =  new  HashedWheelTimer();

    public MetricSearcher getMetricSearcher() {
        return metricSearcher;
    }

    public void setMetricSearcher(MetricSearcher metricSearcher) {
        this.metricSearcher = metricSearcher;
    }


    public FlowCounterManager(String appName) {
        this(appName,false);
    }



    public FlowCounterManager(String appName,Boolean countModelRequest) {
        this.appName = appName;
        String baseFileName = appName + "-metrics.log";
        String modelFileName = "model-metrics.log";
        if (USE_PID) {
            baseFileName += ".pid" + GetSystemInfo.getPid();
            modelFileName += ".pid" + GetSystemInfo.getPid();
        }
        metricSearcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR, baseFileName);

        metricReport = new FileMetricReport(appName);
        if(countModelRequest){
            modelMetricReport = new FileMetricReport("model");
            modelMetricSearcher = new MetricSearcher(MetricWriter.METRIC_BASE_DIR,   modelFileName);
        }

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

    public  List<MetricNode>  queryModelMetrics(long beginTimeMs, long endTimeMs, String identity){

        try {
            return  modelMetricSearcher.findByTimeAndResource(beginTimeMs, endTimeMs, identity);
        } catch (Exception e) {
            logger.error("find model metric error",e);
            throw  new SysException("find model metric error");
        }


    }

    public  List<MetricNode>  queryAllModelMetrics(long beginTimeMs, int size){
        try {
            return  modelMetricSearcher.find(beginTimeMs, size);
        } catch (Exception e) {
            logger.error("find mode metric error",e);
            throw  new SysException("find mode metric error");

        }
    }


    MetricReport  metricReport;

    MetricReport  modelMetricReport;

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

        init();
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                long current = TimeUtil.currentTimeMillis();
                List<MetricNode> reportList =   Lists.newArrayList();
                List<MetricNode> modelReportList =   Lists.newArrayList();
                passMap.forEach((sourceName,flowCounter)->{
                        FlowCounter successCounter = successMap.get(sourceName);
                        FlowCounter blockCounter =  blockMap.get(sourceName);
                        FlowCounter exceptionCounter = exceptionMap.get(sourceName);
                        MetricNode metricNode = new  MetricNode();
                        metricNode.setTimestamp(current);
                        metricNode.setResource(sourceName);
                        metricNode.setPassQps(flowCounter.getSum());
                        metricNode.setBlockQps(blockCounter!=null?new Double(blockCounter.getQps()).longValue():0);
                        metricNode.setExceptionQps(exceptionCounter!=null?new Double(exceptionCounter.getQps()).longValue():0);
                        metricNode.setSuccessQps(successCounter!=null?new Double(successCounter.getQps()).longValue():0);
                        if(sourceName.startsWith("I_")){
                            reportList.add(metricNode);
                        }
                        if(sourceName.startsWith("M_")){
                            modelReportList.add(metricNode);
                        }
                });
            //    logger.info("try to report {}",reportList);
                metricReport.report(reportList);
                modelMetricReport.report(modelReportList);
            }
        }, 0,
        1,
        TimeUnit.SECONDS);
    }


    public  void  rmAllFiles(){
        try {
            if (modelMetricReport instanceof FileMetricReport) {
                FileMetricReport fileMetricReport = (FileMetricReport) modelMetricReport;
                fileMetricReport.rmAllFile();
            }
            if (metricReport instanceof FileMetricReport) {

                FileMetricReport fileMetricReport = (FileMetricReport) metricReport;
                fileMetricReport.rmAllFile();

            }
        }catch (Exception e){
            logger.error("remove metric file error");
        }
    }

    public  static  void main(String[] args){
        FlowCounterManager  flowCounterManager = new  FlowCounterManager("test");
        flowCounterManager.setMetricReport(new FileMetricReport("Test"));
        flowCounterManager.setMetricSearcher(new MetricSearcher(MetricWriter.METRIC_BASE_DIR, "Test" + "-metrics.log.pid" + GetSystemInfo.getPid()));
        flowCounterManager.startReport();

        while(true) {
            flowCounterManager.pass("M_test");
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
    private void init() {
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

      //  JSONArray dataArray = JSONArray.parseArray(result);
        Gson  gson = new Gson();
        List configs =gson.fromJson(result,List.class);
        for (int i = 0; i < configs.size(); i++) {
            Map jsonObject = (Map)configs.get(i);
            sourceQpsAllowMap.put(jsonObject.get("source").toString(),Double.valueOf(jsonObject.get("allow_qps").toString()));
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
        logger.info("update {} allowed qps to {}", sourceName, allowQps);
        sourceQpsAllowMap.put(sourceName, allowQps);
    }
}
