package com.webank.ai.fate.metrics;

import io.micrometer.core.instrument.Counter;


public class MmCounter extends MmMeter implements ICounter {
    private Counter counter;

    public MmCounter(String name, String desc, Counter counter, String... tags) {
        super(name, desc, tags);
        this.counter = counter;
    }

    @Override
    public void increment() {
        this.counter.increment();
    }

    @Override
    public void increment(double delta) {
        this.counter.increment(delta);
    }

    public Counter getCounter() {
        return counter;
    }

    public void setCounter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public String toString() {
        return "MmCounter{" + super.toString() + ", counter=" + counter.count() + '}';
    }
}
