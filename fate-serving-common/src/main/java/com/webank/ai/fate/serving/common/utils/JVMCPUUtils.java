package com.webank.ai.fate.serving.common.utils;

import com.webank.ai.fate.serving.common.bean.ThreadVO;

import java.util.*;

/**
 * @author hcy
 */
public class JVMCPUUtils {

    private static Set<String> states = null;

    static {
        states = new HashSet<>(Thread.State.values().length);
        for (Thread.State state : Thread.State.values()) {
            states.add(state.name());
        }
    }

    public static List<ThreadVO> getThreadsState() {

        List<ThreadVO> threads = ThreadUtils.getThreads();

        Collection<ThreadVO> resultThreads = new ArrayList<>();
        for (ThreadVO thread : threads) {
            if (thread.getState() != null && states.contains(thread.getState().name())) {
                resultThreads.add(thread);
            }
        }


        ThreadSampler threadSampler = new ThreadSampler();
        threadSampler.setIncludeInternalThreads(true);
        threadSampler.sample(resultThreads);
        threadSampler.pause(1000);
        return threadSampler.sample(resultThreads);
    }
}
