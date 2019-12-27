package com.webank.ai.fate.metrics;

public interface ICounter {

    void increment(double delta);

    void increment();
}

