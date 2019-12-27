package com.webank.ai.fate.metrics;

import java.util.concurrent.Callable;
import java.util.function.ToDoubleFunction;

public class MmGuage extends MmMeter {
    public static final ToDoubleFunction<Callable<Double>> metricFunc = doubleCallable -> {
        try {
            return doubleCallable.call();
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    };

    private Callable<Double> callable;

    public MmGuage(String name, String desc, Callable<Double> callable, String... tags) {
        super(name, desc, tags);
        this.callable = callable;
    }

    public Callable<Double> getCallable() {
        return callable;
    }

    public void setCallable(Callable<Double> callable) {
        this.callable = callable;
    }

    @Override
    public String getTagsString() {
        return "MmGuage{" + super.toString() + '}';
    }
}
