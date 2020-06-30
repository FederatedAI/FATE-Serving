package com.webank.ai.fate.serving.core.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {

    public static ThreadPoolExecutor newThreadPoolExecutor() {
        int processors = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(processors,processors * 2,
                0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(128));
        return executor;
    }
}
