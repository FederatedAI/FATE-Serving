
package com.webank.ai.fate.serving.core.flow;

import java.util.concurrent.TimeUnit;

public final class TimeUtil {
    private static volatile long currentTimeMillis = System.currentTimeMillis();

    public TimeUtil() {
    }

    public static long currentTimeMillis() {
        return currentTimeMillis;
    }

    static {
        Thread daemon = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    TimeUtil.currentTimeMillis = System.currentTimeMillis();
                    try {
                        TimeUnit.MILLISECONDS.sleep(1L);
                    } catch (Throwable var2) {
                        ;
                    }
                }
            }
        });
        daemon.setDaemon(true);
        daemon.setName("time-tick-thread");
        daemon.start();
    }
}
