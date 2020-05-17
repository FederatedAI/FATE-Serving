package com.webank.ai.fate.serving.core.flow;
import com.google.common.collect.Lists;
import com.webank.ai.fate.serving.core.bean.MetricEntity;
import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class FlowCounterManager {

    String  appName;

    public   FlowCounterManager(String appName){
        this.appName =  appName;
        metricSearcher =  new  MetricSearcher(MetricWriter.METRIC_BASE_DIR,appName+"-metrics.log.pid"+ GetSystemInfo.getPid());

    }

    Logger logger = LoggerFactory.getLogger(FlowCounterManager.class);

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



    MetricSearcher   metricSearcher ;


    public MetricReport getMetricReport() {
        return metricReport;
    }

    public void setMetricReport(MetricReport metricReport) {
        this.metricReport = metricReport;
    }

    MetricReport  metricReport =  new FileMetricReport();

    private ConcurrentHashMap<String,FlowCounter>   passMap =   new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,FlowCounter>   sucessMap =   new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,FlowCounter>   blockMap =   new ConcurrentHashMap<>();
    private ConcurrentHashMap<String,FlowCounter>   exceptionMap =   new ConcurrentHashMap<>();

    public  FlowCounterManager(){

    }


    public  boolean  pass(String sourceName){
        logger.info("================try to pass {}",sourceName);
        FlowCounter  flowCounter =passMap.get(sourceName);
        if(flowCounter==null){
            flowCounter= passMap.putIfAbsent(sourceName,new  FlowCounter(Integer.MAX_VALUE));
            if(flowCounter==null) {
                flowCounter = passMap.get(sourceName);
            }
        }
        return  flowCounter.tryPass();
    }
    public  boolean  success(String sourceName){
        FlowCounter  flowCounter =sucessMap.get(sourceName);
        if(flowCounter==null){
            flowCounter= sucessMap.putIfAbsent(sourceName,new  FlowCounter(Integer.MAX_VALUE));
            if(flowCounter==null) {
                flowCounter = sucessMap.get(sourceName);
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
                    FlowCounter sucessCounter = sucessMap.get(sourceName);
                    FlowCounter blockCounter =  blockMap.get(sourceName);
                    FlowCounter exceptionCounter = exceptionMap.get(sourceName);
                    MetricNode metricNode = new  MetricNode();
                    metricNode.setTimestamp(current);
                    metricNode.setResource(sourceName);
                    metricNode.setPassQps(passCount);
                    metricNode.setBlockQps(blockCounter!=null?new Double(blockCounter.getQps()).longValue():0);
                    metricNode.setExceptionQps(exceptionCounter!=null?new Double(exceptionCounter.getQps()).longValue():0);
                    metricNode.setSuccessQps(sucessCounter!=null?new Double(sucessCounter.getQps()).longValue():0);
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











}
