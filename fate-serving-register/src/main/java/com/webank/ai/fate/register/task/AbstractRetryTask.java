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


import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.core.timer.Timeout;
import com.webank.ai.fate.serving.core.timer.Timer;
import com.webank.ai.fate.serving.core.timer.TimerTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;


public abstract class AbstractRetryTask implements TimerTask {

    public static final Logger logger = LoggerFactory.getLogger(AbstractRetryTask.class);
    private static int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;
    private static String REGISTRY_RETRY_TIMES_KEY = "retry.times";
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
        this.retryTimes = url.getParameter(REGISTRY_RETRY_TIMES_KEY, Integer.MAX_VALUE);
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
        if (logger.isDebugEnabled()) {
            logger.debug("retry task begin");
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug(taskName + " : " + url.getProject());
        }
        try {
            doRetry(url, registry, timeout);
        } catch (Throwable t) { // Ignore all the exceptions and wait for the next retry
            logger.error("Failed to execute task " + taskName + ", url: " + url + ", waiting for again, cause:" + t.getMessage(), t);
            // reput this task when catch exception.
            reput(timeout, retryPeriod);
        } finally {
            long end = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("retry task cost " + (end - begin));
            }
        }


    }

    /**
     * doRetry
     *
     * @param url
     * @param registry
     * @param timeout
     */
    protected abstract void doRetry(URL url, FailbackRegistry registry, Timeout timeout);
}
