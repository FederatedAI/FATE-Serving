package com.webank.ai.fate.metrics;

import io.micrometer.core.instrument.Timer;

import java.util.concurrent.TimeUnit;

public class MmTimer extends MmMeter implements ITimer {
    private Timer timer;

    public MmTimer(String name, String desc, Timer timer, String... tags) {
        super(name, desc, tags);
        this.timer = timer;
    }

    @Override
    public void record(long millis) {
        this.record(millis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void record(long time, TimeUnit unit) {
        timer.record(time, unit);
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public String toString() {
        return "MmTimer{" + super.toString() + '}';
    }
}
