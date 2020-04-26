package com.webank.ai.fate.serving.admin.task;

import com.alibaba.fastjson.JSONObject;
import com.webank.ai.fate.serving.admin.cache.MetricCache;
import com.webank.ai.fate.serving.admin.interceptors.RequestInterceptor;
import com.webank.ai.fate.serving.core.bean.MetricEntity;
import com.webank.ai.fate.serving.core.utils.HttpClientPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class SendMetricsTask implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);

    // serving-server__/api/model/publishLoad__1582616571000
    @Autowired
    private MetricCache metricCache;

    @Value("${monitor.send.metric.url}")
    private String url;

    private ScheduledExecutorService scheduleService = Executors.newScheduledThreadPool(1);

    private ExecutorService fetchService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(2048));

    // 最近10秒内的统计
    private static final long MAX_LAST_FETCH_INTERVAL_MS = 1000 * 15;
    private static final long FETCH_INTERVAL_SECOND = 6;

    private Map<String, AtomicLong> appLastFetchTime = new ConcurrentHashMap<>();

    public void run() {
        scheduleService.scheduleAtFixedRate(() -> {
            try {
                fetchAllComponent();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100, 1000, TimeUnit.MILLISECONDS);
    }

    private void fetchAllComponent() {
        for (String resource : metricCache.getResourceSet()) {
            // serving-server__/api/model/publishLoad__
            long now = System.currentTimeMillis();
            // 默认拉取时间
            long lastFetchMs = now - MAX_LAST_FETCH_INTERVAL_MS;

            if (appLastFetchTime.containsKey(resource)) {
                // 如果存在已拉取时间，在上次时间 基础上加1秒
                lastFetchMs = Math.max(lastFetchMs, appLastFetchTime.get(resource).get() + 1000);
            }

            // trim milliseconds
            lastFetchMs = lastFetchMs / 1000 * 1000;

            long endTime = lastFetchMs + FETCH_INTERVAL_SECOND * 1000;
            // 结束时间不能超过当前时间
            if (endTime > now - 1000 * 2) {
                return;
            }

            final long finalLastFetchMs = lastFetchMs;
            final long finalEndTime = endTime;

            if (logger.isDebugEnabled()) {
                logger.debug("fetch metric from {}, to {}", new Date(finalLastFetchMs), new Date(endTime));
            }

            appLastFetchTime.computeIfAbsent(resource, a -> new AtomicLong()).set(endTime);

            fetchService.submit(() -> {
                try {
                    long fetchTime = finalLastFetchMs;
                    while (fetchTime <= finalEndTime) {
                        MetricEntity entity = metricCache.get(resource + fetchTime);
                        if (entity != null) {
                            logger.debug("entity : {}", entity);
                        }
                        if (entity != null) {
                            String resp = HttpClientPool.post(url, JSONObject.parseObject(JSONObject.toJSONString(entity), Map.class));
                            if (logger.isDebugEnabled()) {
                                logger.debug("send metric to monitor, request: {}, response: {}", JSONObject.toJSONString(entity), resp);
                            }
                        }
                        fetchTime += 1000;
                    }
                } catch (Exception e) {
                    logger.error("fetch resource metric error", e);
                }
            });
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        run();
    }
}
