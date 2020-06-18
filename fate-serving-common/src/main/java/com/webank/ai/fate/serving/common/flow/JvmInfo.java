package com.webank.ai.fate.serving.common.flow;

import com.webank.ai.fate.serving.core.utils.JVMMemoryUtils;

public class JvmInfo {
    @Override
    public String toString() {
        return Long.toString(this.timestamp);
    }

    long  timestamp ;

    public JvmInfo() {
    }

    public  JvmInfo(long  timestamp){
        this.timestamp = timestamp;
    }
    JVMMemoryUtils.JVMMemoryUsage    heap;
    JVMMemoryUtils.JVMMemoryUsage    eden;
    JVMMemoryUtils.JVMMemoryUsage    old;
    long  yongGcCount;
    long  yongGcTime;
    long  fullGcCount;

    public long getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(long threadCount) {
        this.threadCount = threadCount;
    }

    long  threadCount;

    public long getYongGcCount() {
        return yongGcCount;
    }

    public void setYongGcCount(long yongGcCount) {
        this.yongGcCount = yongGcCount;
    }

    public long getYongGcTime() {
        return yongGcTime;
    }

    public void setYongGcTime(long yongGcTime) {
        this.yongGcTime = yongGcTime;
    }

    public long getFullGcCount() {
        return fullGcCount;
    }

    public void setFullGcCount(long fullGcCount) {
        this.fullGcCount = fullGcCount;
    }

    public long getFullGcTime() {
        return fullGcTime;
    }

    public void setFullGcTime(long fullGcTime) {
        this.fullGcTime = fullGcTime;
    }

    long  fullGcTime;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public JVMMemoryUtils.JVMMemoryUsage getHeap() {
        return heap;
    }

    public void setHeap(JVMMemoryUtils.JVMMemoryUsage heap) {
        this.heap = heap;
    }

    public JVMMemoryUtils.JVMMemoryUsage getEden() {
        return eden;
    }

    public void setEden(JVMMemoryUtils.JVMMemoryUsage eden) {
        this.eden = eden;
    }

    public JVMMemoryUtils.JVMMemoryUsage getOld() {
        return old;
    }

    public void setOld(JVMMemoryUtils.JVMMemoryUsage old) {
        this.old = old;
    }

    public JVMMemoryUtils.JVMMemoryUsage getNonHeap() {
        return nonHeap;
    }

    public void setNonHeap(JVMMemoryUtils.JVMMemoryUsage nonHeap) {
        this.nonHeap = nonHeap;
    }

    public JVMMemoryUtils.JVMMemoryUsage getSurvivor() {
        return survivor;
    }

    public void setSurvivor(JVMMemoryUtils.JVMMemoryUsage survivor) {
        this.survivor = survivor;
    }

    JVMMemoryUtils.JVMMemoryUsage  nonHeap;
    JVMMemoryUtils.JVMMemoryUsage survivor;

    public  void  reset(){

    }
}
