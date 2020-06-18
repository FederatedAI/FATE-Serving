package com.webank.ai.fate.serving.event;
import com.webank.ai.fate.serving.common.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.common.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.common.async.Subscribe;
import com.webank.ai.fate.serving.common.cache.Cache;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SingleCacheEventHandler extends AbstractAsyncMessageProcessor {
    Logger logger = LoggerFactory.getLogger(SingleCacheEventHandler.class);
    @Autowired
    Cache cache;

    @Subscribe(value = Dict.EVENT_SET_INFERENCE_CACHE)
    public void handleMetricsEvent(AsyncMessageEvent event) {
        CacheEventData cacheEventData = (CacheEventData) event.getData();
        Map map = (Map) cacheEventData.getData();
        cache.put(cacheEventData.getKey(), JsonUtil.object2Json(map));
    }

}
