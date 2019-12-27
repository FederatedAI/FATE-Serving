package com.webank.ai.fate.metrics;

import java.util.concurrent.TimeUnit;

public interface ITimer {

    void record(long duration, TimeUnit unit);

    void record(long milliSeconds);
}
