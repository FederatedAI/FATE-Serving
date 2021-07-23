package com.webank.ai.fate.serving.proxy.rpc.services;

import com.google.common.collect.Lists;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.common.bean.HealthCheckResult;
import com.webank.ai.fate.serving.common.utils.TelnetUtil;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.proxy.rpc.router.ConfigFileBasedServingRouter;
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
    @Autowired
    ConfigFileBasedServingRouter configFileBasedServingRouter;

    public HealthCheckResult check(){
        HealthCheckResult  healthCheckResult = new HealthCheckResult();
        this.configCheck(healthCheckResult);
        this.machineCheck(healthCheckResult);
        this.routerInfoCheck(healthCheckResult);
        return  healthCheckResult;

    }

    private  void  configCheck(HealthCheckResult  healthCheckResult){

//        if(!MetaInfo.PROPERTY_USE_REGISTER.booleanValue()){
//                healthCheckResult.getWarnList().add(MetaInfo.PROPERTY_USE_REGISTER+":"+MetaInfo.PROPERTY_USE_REGISTER+"="+MetaInfo.PROPERTY_USE_REGISTER);
//        }else{
//            if(StringUtils.isNotEmpty(MetaInfo.PROPERTY_ZK_URL)){
//                healthCheckResult.getOkList().add(MetaInfo.PROPERTY_ZK_URL+":"+MetaInfo.PROPERTY_ZK_URL);
//            }else {
//                healthCheckResult.getErrorList().add(MetaInfo.PROPERTY_ZK_URL+":"+MetaInfo.PROPERTY_ZK_URL);
//            }
//        }

        if(configFileBasedServingRouter.getAllRouterInfoMap()==null||configFileBasedServingRouter.getAllRouterInfoMap().size()==0){
            healthCheckResult.getErrorList().add("check router_table.json  "+": no router info found");
        }else{
            healthCheckResult.getOkList().add("check router_table.json  "+": router_table.json is found");
        }

        if(!MetaInfo.PROPERTY_USE_ZK_ROUTER.booleanValue()){
            healthCheckResult.getWarnList().add(MetaInfo.PROPERTY_USE_ZK_ROUTER+":"+MetaInfo.PROPERTY_USE_ZK_ROUTER+"="+MetaInfo.PROPERTY_USE_ZK_ROUTER);
        }else{
            if(StringUtils.isNotEmpty(MetaInfo.PROPERTY_ZK_URL)){
                healthCheckResult.getOkList().add("check zk config "+": ok ");
            }else {
                healthCheckResult.getErrorList().add("check zk config "+": no config ");
            }
        }
    }
    private  void  routerInfoCheck(HealthCheckResult  healthCheckResult){
        Map<Proxy.Topic, List<RouterInfo>>    routerInfoMap = configFileBasedServingRouter.getAllRouterInfoMap();
        if(routerInfoMap!=null&&routerInfoMap.size()>0){
            routerInfoMap.forEach((k,v)->{
                if(v!=null){
                    v.forEach(routerInfo -> {
                        boolean  connectAble  = TelnetUtil.tryTelnet(routerInfo.getHost(),routerInfo.getPort());
                        if(!connectAble){
                            healthCheckResult.getErrorList().add("check router " +routerInfo.getHost() + " " + routerInfo.getPort()+": can not be telneted");
                        }else{
                            healthCheckResult.getOkList().add("check router " +routerInfo.getHost() + " " + routerInfo.getPort()+": telnet ok");
                        }
                    });
                }
            });
        }

    }

    private  void  machineCheck(HealthCheckResult  healthCheckResult){
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
