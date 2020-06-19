package com.webank.ai.fate.serving.common.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
 
/**
 * 类描述：JVM 线程信息工具类
 * 
 **/
public class JVMThreadUtils {
    static private ThreadMXBean threadMXBean;
 
    static {
        threadMXBean = ManagementFactory.getThreadMXBean();
    }
 
    /**
     * Daemon线程总量
     * @return
     */
    static public int getDaemonThreadCount() {
        return threadMXBean.getDaemonThreadCount();
    }
 
    /**
     * 当前线程总量
     * @return
     */
    static public int getThreadCount() {
        return threadMXBean.getThreadCount();
    }
    
    /**
     * 获取线程数量峰值（从启动或resetPeakThreadCount()方法重置开始统计）
     * @return
     */
    static public int getPeakThreadCount() {
        return threadMXBean.getPeakThreadCount();
    }
    
    /**
     * 获取线程数量峰值（从启动或resetPeakThreadCount()方法重置开始统计），并重置
     * @return
     * @Throws java.lang.SecurityException - if a security manager exists and the caller does not have ManagementPermission("control").
     */
    static public int getAndResetPeakThreadCount() {
    	int count = threadMXBean.getPeakThreadCount();
    	resetPeakThreadCount();
        return count;
    }
    
    /**
     * 重置线程数量峰值
     * @Throws java.lang.SecurityException - if a security manager exists and the caller does not have ManagementPermission("control").
     */
    static public void resetPeakThreadCount() {
        threadMXBean.resetPeakThreadCount();
    }
    
    /**
     * 死锁线程总量
     * @return
     * @Throws IllegalStateException 没有权限或JVM不支持的操作
     */
    static public int getDeadLockedThreadCount() {
        try {
            long[] deadLockedThreadIds = threadMXBean.findDeadlockedThreads();
            if (deadLockedThreadIds == null) {
                return 0;
            }
            return deadLockedThreadIds.length;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    public static void main(String[] args) {
    	for(;;) {
    		System.out.println("=======================================================================");
	        System.out.println("getDaemonThreadCount: " + JVMThreadUtils.getDaemonThreadCount());
	        System.out.println("getNonHeapMemoryUsage: " + JVMThreadUtils.getThreadCount());
	        System.out.println("getPeakThreadCountAndReset: " + JVMThreadUtils.getAndResetPeakThreadCount());
	        System.out.println("getDeadLockedThreadCount: " + JVMThreadUtils.getDeadLockedThreadCount());
	        try {	        	
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
}

