package com.webank.ai.fate.serving.event;
import com.webank.ai.fate.serving.common.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.common.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.common.async.Subscribe;
import com.webank.ai.fate.serving.common.cache.Cache;
import com.webank.ai.fate.serving.core.bean.Dict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchCacheEventHandler extends AbstractAsyncMessageProcessor {
    Logger logger = LoggerFactory.getLogger(BatchCacheEventHandler.class);
    @Autowired
    Cache cache;

    @Subscribe(value = Dict.EVENT_SET_BATCH_INFERENCE_CACHE)
    public void handleMetricsEvent(AsyncMessageEvent event) {
        List<Cache.DataWrapper> lists = (List<Cache.DataWrapper>) event.getData();
        if (lists != null) {
            cache.put(lists);
        }
    }

}
