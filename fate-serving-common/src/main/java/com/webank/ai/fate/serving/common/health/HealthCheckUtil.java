package com.webank.ai.fate.serving.common.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class HealthCheckUtil {

    private static final Logger logger = LoggerFactory.getLogger(HealthCheckUtil.class);
    private static final Double MEMORY_LIMIT = 0.8;

    public static void memoryCheck(HealthCheckResult  healthCheckResult){
        try {
            SystemInfo systemInfo = new SystemInfo();
            GlobalMemory memory = systemInfo.getHardware().getMemory();
            long totalByte = memory.getTotal();
            long callableByte = memory.getAvailable();
            double useRate = (totalByte-callableByte) * 1.0 / totalByte;
            String useRateString = getPercentFormat(useRate,1,2);
            if(useRate > MEMORY_LIMIT) {
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_MEMORY_USAGE.getItemName()," usage:"+ useRateString ,HealthCheckStatus.error));
            }else{
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_MEMORY_USAGE.getItemName(),"usage:"+ useRateString ,HealthCheckStatus.ok));
            }
        } catch (Exception e) {
            logger.error("memoryCheck failed", e);
        }
    }

    private static String getPercentFormat(double d,int IntegerDigits,int FractionDigits){
        NumberFormat nf = java.text.NumberFormat.getPercentInstance();
        nf.setMaximumIntegerDigits(IntegerDigits);
        nf.setMinimumFractionDigits(FractionDigits);
        return nf.format(d);
    }

}
