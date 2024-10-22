package com.webank.ai.fate.register.loadbalance;

import com.webank.ai.fate.register.url.URL;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance extends AbstractLoadBalancer {

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    protected List<URL> doSelect(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return Lists.newArrayList();
        }
        int length = urls.size();
        int currentIndex = index.getAndIncrement();
        int selectedIndex = currentIndex % length;
        return Lists.newArrayList(urls.get(selectedIndex));
    }
}