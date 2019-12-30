package com.webank.ai.fate.serving.metrics.api;

public interface ICounter {

    void increment(double delta);

    void increment();
}

