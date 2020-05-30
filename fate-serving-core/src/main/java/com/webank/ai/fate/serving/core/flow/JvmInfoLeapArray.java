package com.webank.ai.fate.serving.core.flow;


public class JvmInfoLeapArray extends  LeapArray<JvmInfo>{


    public JvmInfoLeapArray(int sampleCount, int intervalInMs) {
        super(sampleCount, intervalInMs);
    }

    @Override
    public JvmInfo newEmptyBucket(long timeMillis) {
        return new  JvmInfo(timeMillis);
    }

    @Override
    protected WindowWrap<JvmInfo> resetWindowTo(WindowWrap<JvmInfo> windowWrap, long startTime) {
        windowWrap.resetTo(startTime);
        windowWrap.value().reset();
        return windowWrap;
    }
}
