package com.webank.ai.fate.serving.metrics.api;

import java.util.concurrent.Callable;

public interface IMetricFactory {

    ICounter counter(String name, String desc, String... tags);

    ITimer timer(String name, String desc, String... tags);

    void gauge(String name, String desc, Callable<Double> callable, String... tags);
}
