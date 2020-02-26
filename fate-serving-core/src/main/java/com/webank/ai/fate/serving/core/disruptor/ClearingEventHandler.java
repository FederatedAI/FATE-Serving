package com.webank.ai.fate.serving.core.disruptor;

import com.lmax.disruptor.EventHandler;
import com.webank.ai.fate.serving.core.disruptor.AsyncMessageEvent;

public class ClearingEventHandler<T> implements EventHandler<AsyncMessageEvent<T>> {
    @Override
    public void onEvent(AsyncMessageEvent<T> event, long sequence, boolean endOfBatch) throws Exception {
        // Failing to call clear here will result in the
        // object associated with the event to live until
        // it is overwritten once the ring buffer has wrapped
        // around to the beginning.
        event.clear();
    }
}
