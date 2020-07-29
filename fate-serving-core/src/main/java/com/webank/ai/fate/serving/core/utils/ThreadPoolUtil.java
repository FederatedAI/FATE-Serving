package com.webank.ai.fate.serving.core.utils;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    public static ThreadPoolExecutor newThreadPoolExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processors, Integer.MAX_VALUE,
                0, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
        return executor;
    }
}
