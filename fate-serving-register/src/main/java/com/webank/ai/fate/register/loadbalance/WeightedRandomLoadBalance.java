package com.webank.ai.fate.register.loadbalance;

import com.webank.ai.fate.register.url.URL;
import org.apache.curator.shaded.com.google.common.collect.Lists;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedRandomLoadBalance extends AbstractLoadBalancer {

    @Override
    protected List<URL> doSelect(List<URL> urls) {
        int length = urls.size();

        boolean sameWeight = true;

        int[] weights = new int[length];

        int firstWeight = getWeight(urls.get(0));
        weights[0] = firstWeight;

        int totalWeight = firstWeight;
        for (int i = 1; i < length; i++) {
            int weight = getWeight(urls.get(i));

            weights[i] = weight;

            totalWeight += weight;
            if (sameWeight && weight != firstWeight) {
                sameWeight = false;
            }
        }

        if (totalWeight > 0 && !sameWeight) {

            int offset = ThreadLocalRandom.current().nextInt(totalWeight);

            for (int i = 0; i < length; i++) {
                offset -= weights[i];
                if (offset < 0) {
                    return Lists.newArrayList(urls.get(i));
                }
            }
        }

        return Lists.newArrayList(urls.get(ThreadLocalRandom.current().nextInt(length)));
    }
}
