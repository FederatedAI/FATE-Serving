
package com.webank.ai.fate.serving.core.flow;




import com.google.common.collect.Lists;
import com.webank.ai.fate.serving.core.utils.GetSystemInfo;
import com.webank.ai.fate.serving.core.utils.JVMGCUtils;
import com.webank.ai.fate.serving.core.utils.JVMMemoryUtils;
import com.webank.ai.fate.serving.core.utils.JVMThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

public class JvmInfoCounter {

    static boolean  started =false;

    private static LeapArray<JvmInfo> data= new JvmInfoLeapArray(10, 10000);;

    private  static Logger logger = LoggerFactory.getLogger(JvmInfoCounter.class);

    public static  List<JvmInfo>  getMemInfos(){
       List<JvmInfo>  result = Lists.newArrayList();
       if(data.listAll()!=null){
           data.listAll().forEach(window->{
               result.add(window.value());
           });
       }
       return  result;
    };

    static ScheduledThreadPoolExecutor  executorService =  new ScheduledThreadPoolExecutor(1);

    public static synchronized void  start(){

        if(!started) {
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    long timestamp = TimeUtil.currentTimeMillis();
                    JvmInfo memInfo = data.currentWindow().value();
                    memInfo.heap = JVMMemoryUtils.getHeapMemoryUsage();
                    memInfo.old = JVMMemoryUtils.getOldGenMemoryUsage();
                    memInfo.eden = JVMMemoryUtils.getEdenSpaceMemoryUsage();
                    memInfo.nonHeap = JVMMemoryUtils.getNonHeapMemoryUsage();
                    memInfo.survivor = JVMMemoryUtils.getSurvivorSpaceMemoryUsage();
                    memInfo.yongGcCount = JVMGCUtils.getYoungGCCollectionCount();
                    memInfo.yongGcTime  =  JVMGCUtils.getYoungGCCollectionTime();
                    memInfo.fullGcCount = JVMGCUtils.getFullGCCollectionCount();
                    memInfo.fullGcTime  = JVMGCUtils.getFullGCCollectionTime();
                    memInfo.threadCount = JVMThreadUtils.getThreadCount();
                    memInfo.timestamp = timestamp;
                }
            }, 0, 1000, TimeUnit.MILLISECONDS);
            started =  true;
        }
    }

    public static void main(String[] args){

        JvmInfoCounter.start();
        while(true) {
            System.err.println(JvmInfoCounter.getMemInfos());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }



    }



}
