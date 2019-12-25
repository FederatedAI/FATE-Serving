/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.register.task;


import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.common.FailbackRegistry;
import com.webank.ai.fate.register.common.TimerTask;
import com.webank.ai.fate.register.interfaces.Timeout;
import com.webank.ai.fate.register.interfaces.Timer;
import com.webank.ai.fate.register.url.URL;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;


public abstract class AbstractRetryTask implements TimerTask {

    public static final Logger logger = LogManager.getLogger();
    /**
     * url for retry task
     */
    protected final URL url;
    /**
     * registry for this task
     */
    protected final FailbackRegistry registry;
    /**
     * retry period
     */
    final long retryPeriod;
    /**
     * define the most retry times
     */
    private final int retryTimes;
    /**
     * task name for this task
     */
    private final String taskName;
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;
    String REGISTRY_RETRY_TIMES_KEY = "retry.times";
    /**
     * times of retry.
     * retry task is execute in single thread so that the times is not need volatile.
     */
    private int times = 1;

    private volatile boolean cancel;

    AbstractRetryTask(URL url, FailbackRegistry registry, String taskName) {
        if (url == null || StringUtils.isBlank(taskName)) {
            throw new IllegalArgumentException();
        }
        this.url = url;
        this.registry = registry;
        this.taskName = taskName;
        cancel = false;
        this.retryPeriod = url.getParameter(Constants.REGISTRY_FILESAVE_SYNC_KEY, DEFAULT_REGISTRY_RETRY_PERIOD);
        this.retryTimes = url.getParameter(REGISTRY_RETRY_TIMES_KEY, 5000);
    }

    public void cancel() {
        cancel = true;
    }

    public boolean isCancel() {
        return cancel;
    }

    protected void reput(Timeout timeout, long tick) {
        if (timeout == null) {
            throw new IllegalArgumentException();
        }

        Timer timer = timeout.timer();
        if (timer.isStop() || timeout.isCancelled() || isCancel()) {
            return;
        }
        times++;
        timer.newTimeout(timeout.task(), tick, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run(Timeout timeout) throws Exception {

        logger.info("retry task begin");
        long begin = System.currentTimeMillis();
        if (timeout.isCancelled() || timeout.timer().isStop() || isCancel()) {
            // other thread cancel this timeout or stop the timer.
            return;
        }
        if (times > retryTimes) {
            // reach the most times of retry.
            logger.error("Final failed to execute task " + taskName + ", url: " + url + ", retry " + retryTimes + " times.");
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info(taskName + " : " + url);
        }
        try {
            doRetry(url, registry, timeout);
        } catch (Throwable t) { // Ignore all the exceptions and wait for the next retry
            logger.error("Failed to execute task " + taskName + ", url: " + url + ", waiting for again, cause:" + t.getMessage(), t);
            // reput this task when catch exception.
            reput(timeout, retryPeriod);
        } finally {
            long end = System.currentTimeMillis();
            logger.error("retry task cost " + (end - begin));
        }


    }

    protected abstract void doRetry(URL url, FailbackRegistry registry, Timeout timeout);
}
