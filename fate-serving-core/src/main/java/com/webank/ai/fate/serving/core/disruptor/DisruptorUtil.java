package com.webank.ai.fate.serving.core.disruptor;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

        log.info("disruptor initialized");
    }

    public void shutdown() {
        if (disruptor != null) {
            disruptor.shutdown();
        }
    }

    /**
     * event producer
     * args[0] event name, e.g. interface name, use to @Subscribe value
     * args[1] event action
     * args[2] data params
     * @param args
     */
    public static void producer(Object... args){
        RingBuffer<AsyncMessageEvent> ringBuffer = disruptor.getRingBuffer();
        AsyncMessageEventProducer producer = new AsyncMessageEventProducer(ringBuffer);
        producer.publishEvent(args);
    }
}
