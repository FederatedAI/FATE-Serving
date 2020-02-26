package com.webank.ai.fate.serving.core.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;

public class DisruptorUtil {

    private static Disruptor<AsyncMessageEvent> disruptor = null;

    static {
        // Construct the Disruptor, use single event publisher
        disruptor = new Disruptor(AsyncMessageEvent.FACTORY, 2048, DaemonThreadFactory.INSTANCE,
                ProducerType.SINGLE, new BlockingWaitStrategy());

        // Connect the handler
        disruptor.handleEventsWith(new AsyncMessageEventHandler()).then(new ClearingEventHandler());

//        disruptor.handleEventsWithWorkerPool()

        // exception handler
        disruptor.setDefaultExceptionHandler(new DisruptorExceptionHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();
    }

    public void shutdown() {
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }

    public static void producer(Object... args){
        RingBuffer<AsyncMessageEvent> ringBuffer = disruptor.getRingBuffer();
        AsyncMessageEventProducer producer = new AsyncMessageEventProducer(ringBuffer);
        producer.publishEvent(args);
    }
}
