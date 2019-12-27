package com.webank.ai.fate.metrics;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

public class MmMetricsRegistry implements MeterBinder {
    private static Logger logger = LoggerFactory.getLogger(MmMetricsRegistry.class);

    private MeterRegistry registry;

    @PreDestroy
    public void destroy() {
        if (registry != null && !registry.isClosed()) {
            registry.close();
        }
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        if (this.registry == null) {
            this.registry = registry;
            logger.info("MeterRegistry is set...");
        }
    }

    public void registerGauge(MmGuage g, String... tags) {
        checkState();
        Gauge.builder(g.getName(), g.getCallable(), MmGuage.metricFunc).tags(tags)
                .description(g.getDesc()).register(this.registry);
    }

    public Counter registerCounter(String name, String desc, String... tags) {
        checkState();
        return Counter.builder(name).tags(tags).description(desc).register(this.registry);
    }

    public Timer registerTimer(String name, String desc, String... tags) {
        checkState();
        return Timer.builder(name).tags(tags).description(desc).register(this.registry);
    }

    private void checkState() {
        if (this.registry == null) {
            throw new IllegalStateException("Metrics registry is not initialized yet!");
        }
    }
}
