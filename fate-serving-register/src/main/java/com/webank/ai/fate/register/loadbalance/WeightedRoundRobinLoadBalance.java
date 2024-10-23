package com.webank.ai.fate.register.loadbalance;

import com.webank.ai.fate.register.url.URL;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class WeightedRoundRobinLoadBalance extends AbstractLoadBalancer {

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    protected List<URL> doSelect(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            return Lists.newArrayList();
        }

        int totalWeight = 0;
        boolean sameWeight = true;
        int lastWeight = 0;

        // 计算总权重并检查权重是否相同
        for (int i = 0; i < urls.size(); i++) {
            int weight = getWeight(urls.get(i));
            totalWeight += weight;
            if (sameWeight && i > 0 && weight != lastWeight) {
                sameWeight = false;
            }
            lastWeight = weight;
        }

        // 如果权重相同，直接使用轮询算法
        if (sameWeight && totalWeight > 0) {
            int length = urls.size();
            int currentIndex = index.getAndIncrement();
            int selectedIndex = currentIndex % length;
            return Lists.newArrayList(urls.get(selectedIndex));
        }

        // 否则，使用加权轮询算法
        int currentIndex = index.getAndIncrement();
        int pos = currentIndex % totalWeight;
        for (URL url : urls) {
            pos -= getWeight(url);
            if (pos < 0) {
                return Lists.newArrayList(url);
            }
        }

        // 如果所有服务器权重都为0，则采用随机算法
        return Lists.newArrayList(urls.get(ThreadLocalRandom.current().nextInt(urls.size())));
    }
}