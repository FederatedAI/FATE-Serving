package com.webank.ai.fate.serving.grpc.service;

import com.google.common.collect.Lists;
import com.webank.ai.fate.api.networking.proxy.Proxy;

import com.webank.ai.fate.serving.common.bean.HealthCheckResult;
import com.webank.ai.fate.serving.common.utils.TelnetUtil;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HealthCheckEndPointService {


    public HealthCheckResult check(){
        HealthCheckResult  healthCheckResult = new HealthCheckResult();
        this.configCheck(healthCheckResult);
        this.machineCheck(healthCheckResult);
        return  healthCheckResult;
    }

    private  void  configCheck(HealthCheckResult  healthCheckResult){

        String fullUrl = MetaInfo.PROPERTY_MODEL_TRANSFER_URL;
        if(StringUtils.isNotBlank(fullUrl)) {
            String host = fullUrl.substring(fullUrl.indexOf('/') + 2, fullUrl.lastIndexOf(':'));
            int port = Integer.parseInt(fullUrl.substring(fullUrl.lastIndexOf(':') + 1,
                    fullUrl.indexOf('/', fullUrl.lastIndexOf(':'))));
            boolean isConnected = TelnetUtil.tryTelnet(host, port);
            if (!isConnected) {
                String result1 = String.format("check default fateflow url: %s can not connected", fullUrl);
                healthCheckResult.getWarnList().add(result1);
            }
        if(!MetaInfo.PROPERTY_USE_REGISTER.booleanValue()){
                healthCheckResult.getWarnList().add("check use zookeeper"+":"+MetaInfo.PROPERTY_USE_REGISTER);
        }else{
            healthCheckResult.getOkList().add("check use zookeeper"+":"+MetaInfo.PROPERTY_USE_REGISTER);
            if(StringUtils.isNotEmpty(MetaInfo.PROPERTY_ZK_URL)){
                healthCheckResult.getOkList().add("check zookeeper url"+":"+MetaInfo.PROPERTY_ZK_URL);
            }else {
                healthCheckResult.getErrorList().add("check zookeeper url"+":"+MetaInfo.PROPERTY_ZK_URL);
            }
        }
        }
    }


    private  void  machineCheck(HealthCheckResult  healthCheckResult){
        try {
            SystemInfo systemInfo = new SystemInfo();
//            CentralProcessor processor = systemInfo.getHardware().getProcessor();
//            long[] prevTicks = processor.getSystemCpuLoadTicks();
//            long[] ticks = processor.getSystemCpuLoadTicks();
//            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
//            long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
//            long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
//            long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
//            long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
//            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
//            long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
//            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
//            long totalCpu = user + nice + cSys + idle + ioWait + irq + softirq + steal;
//
//            Map<String,String> CPUInfo = new HashMap<>();
//            CPUInfo.put("Total CPU Processors", String.valueOf(processor.getLogicalProcessorCount()));
//            CPUInfo.put("CPU Usage", new DecimalFormat("#.##%").format(1.0-(idle * 1.0 / totalCpu)));


            GlobalMemory memory = systemInfo.getHardware().getMemory();
            long totalByte = memory.getTotal();
            long callableByte = memory.getAvailable();
            Map<String,String> memoryInfo= new HashMap<>();
//            memoryInfo.put("Total Memory",new DecimalFormat("#.##GB").format(totalByte/1024.0/1024.0/1024.0));
//            memoryInfo.put("Memory Usage", new DecimalFormat("#.##%").format((totalByte-callableByte)*1.0/totalByte));
            double  useRate = (totalByte-callableByte)*1.0/totalByte;

            if(useRate>0.8) {
                healthCheckResult.getWarnList().add("check memory usage:"+useRate);
            }else{
                healthCheckResult.getOkList().add("check memory usage:"+useRate);
            }


        } catch (Exception e) {

        }
    }

    private  void  metircCheck(HealthCheckResult  healthCheckResult){


    }

}
