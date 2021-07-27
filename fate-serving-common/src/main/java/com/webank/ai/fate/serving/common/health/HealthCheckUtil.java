package com.webank.ai.fate.serving.common.health;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class HealthCheckUtil {


    public  static void   memoryCheck(HealthCheckResult  healthCheckResult){
        try {
            SystemInfo systemInfo = new SystemInfo();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            long[] ticks = processor.getSystemCpuLoadTicks();
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
            long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
            long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
            long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long ioWait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long totalCpu = user + nice + cSys + idle + ioWait + irq + softirq + steal;

            Map<String,String> CPUInfo = new HashMap<>();
            CPUInfo.put("Total CPU Processors", String.valueOf(processor.getLogicalProcessorCount()));
            CPUInfo.put("CPU Usage", new DecimalFormat("#.##%").format(1.0-(idle * 1.0 / totalCpu)));


            GlobalMemory memory = systemInfo.getHardware().getMemory();
            long totalByte = memory.getTotal();
            long callableByte = memory.getAvailable();
            Map<String,String> memoryInfo= new HashMap<>();
            memoryInfo.put("Total Memory",new DecimalFormat("#.##GB").format(totalByte/1024.0/1024.0/1024.0));
            memoryInfo.put("Memory Usage", new DecimalFormat("#.##%").format((totalByte-callableByte)*1.0/totalByte));
            double  useRate = (totalByte-callableByte)*1.0/totalByte;

            if(useRate>0.8) {
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_MEMORY_USAGE.getItemName(),"check memory usage:"+useRate ,HealthCheckStatus.error));
            }else{
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_MEMORY_USAGE.getItemName(),"check memory usage:"+useRate ,HealthCheckStatus.ok));

            }
        } catch (Exception e) {

        }
    }

}
