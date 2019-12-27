package com.webank.ai.fate.metrics;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MmMetricFactory implements IMetricFactory{
    private static Logger logger = LoggerFactory.getLogger(MmMetricFactory.class);
    @Autowired
    private MmMetricsRegistry register;
    private Set<MmGuage> gauges = ConcurrentHashMap.newKeySet();
    private ConcurrentMap<String, ICounter> counters = new ConcurrentHashMap<>();
    private ConcurrentMap<String, ITimer> timers = new ConcurrentHashMap<>();

    @PreDestroy
    public void destroy() {
        gauges.clear();
        counters.clear();
        timers.clear();
    }

    @Override
    public ICounter counter(String name, String desc, String... tags) {
        ICounter c;
        String key = getKey(name, tags);
        if ((c = counters.get(key)) == null) {
            c = counters.computeIfAbsent(key, k -> new MmCounter(name, desc, register.registerCounter(name, desc, tags), tags));
        }
        return c;
    }

    @Override
    public ITimer timer(String name, String desc, String... tags) {
        ITimer t;
        String key = getKey(name, tags);
        if ((t = timers.get(key)) == null) {
            t = timers.computeIfAbsent(key, k -> new MmTimer(name, desc, register.registerTimer(name, desc, tags), tags));
        }
        return t;
    }

    @Override
    public void gauge(String name, String desc, Callable<Double> callable, String... tags) {
        MmGuage g = new MmGuage(name, desc, callable, tags);
        if (gauges.add(g)) {
            register.registerGauge(g, tags);
            logger.info("gauge metric added for: {}", g);

        } else {
            logger.warn("duplicated gauge: {}", g);
        }
    }

    private String getKey(String name, String... tags) {
        return name + StringUtils.join(tags);
    }

    public Set<MmGuage> getGauges() {
        return gauges;
    }

    public Map<String, ICounter> getCounters() {
        return counters;
    }

    public Map<String, ITimer> getTimers() {
        return timers;
    }

}
