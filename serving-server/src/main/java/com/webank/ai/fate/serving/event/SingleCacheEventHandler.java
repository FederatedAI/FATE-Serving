package com.webank.ai.fate.serving.event;


import com.alibaba.fastjson.JSON;
import com.webank.ai.fate.serving.core.annotation.Subscribe;
import com.webank.ai.fate.serving.core.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.cache.Cache;
import com.webank.ai.fate.serving.core.upload.AlertInfoUploader;
import com.webank.ai.fate.serving.core.utils.InferenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SingleCacheEventHandler      extends AbstractAsyncMessageProcessor {

    Logger logger = LoggerFactory.getLogger(SingleCacheEventHandler.class);

    @Autowired
    Cache cache;
    @Subscribe(value = Dict.EVENT_SET_INFERENCE_CACHE)
    public void handleMetricsEvent(AsyncMessageEvent event) {

       CacheEventData cacheEventData= (CacheEventData) event.getData();

       Map map =(Map) cacheEventData.getData();

       cache.put(cacheEventData.getKey(), JSON.toJSONString(map));
    }

}
