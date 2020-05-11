package com.webank.ai.fate.serving.metrics.api;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultMetricFactory implements IMetricFactory {
    private Timer timer;
    private int period;
    private boolean mute;

    public DefaultMetricFactory() {
        this(10000);
    }

    public DefaultMetricFactory(int period) {
        this(period, false);
    }

    public DefaultMetricFactory(int period, boolean mute) {
        System.out.println("default metrics factory created");
        timer = new Timer("default_metrics_factory_timer", true);
        this.period = period;
        this.mute = mute;
    }

    @Override
    public ICounter counter(String name, String desc, String... tags) {
        return new ICounter() {
            private AtomicLong counter = new AtomicLong(0);
            private String metricName = name;
            private String description = desc;

            @Override
            public void increment() {
                if (!mute) {
                    System.out.println(
                            "metric name: " + metricName + ", desc: " + description
                                    + ", counter: " + counter.incrementAndGet());
                }
            }

            @Override
            public void increment(double delta) {
                if (!mute) {
                    System.out.println(
                            "metric name: " + metricName + ", desc: " + description
                                    + ", counter: " + counter.addAndGet((long) delta));
                }
            }
        };
    }

    @Override
    public ITimer timer(String name, String desc, String... tags) {
        return new ITimer() {
            private int limit = 1_000_000;
            private SortedMap<Long, Long> storage = buildStorage();

            private SortedMap<Long, Long> buildStorage() {
                return Collections.synchronizedSortedMap(new TreeMap<>());
            }

            @Override
            public void record(long milliSeconds) {
                if (storage.size() >= limit) {
                    storage = buildStorage();
                }
                storage.put(System.currentTimeMillis(), milliSeconds);
                if (!mute) {
                    System.out.println("time consumed: " + milliSeconds + " ms.");
                }
            }

            @Override
            public void record(long duration, TimeUnit unit) {
                this.record(TimeUnit.MILLISECONDS.convert(duration, unit));
            }
        };
    }

    @Override
    public void gauge(String name, String desc, Callable<Double> callable, String... tags) {
        try {
            if (!mute) {
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            System.out.println(
                                    "metric name: " + name + ", desc: " + desc
                                            + ", value: " + callable.call());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 0, period);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}