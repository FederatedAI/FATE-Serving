package com.webank.ai.fate.serving.event;


import com.alibaba.fastjson.JSON;
import com.webank.ai.fate.serving.core.annotation.Subscribe;
import com.webank.ai.fate.serving.core.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.core.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BatchCacheEventHandler extends AbstractAsyncMessageProcessor {
    Logger logger  = LoggerFactory.getLogger(BatchCacheEventHandler.class);
    @Autowired
    Cache cache;
    @Subscribe(value=Dict.EVENT_SET_BATCH_INFERENCE_CACHE)
    public void handleMetricsEvent(AsyncMessageEvent event) {
        logger.info("pppppppppppppppppppppppppppppppppppppppppp {}",  event.getData());
        List<Cache.DataWrapper> lists = (List<Cache.DataWrapper>)event.getData();
        if(lists!=null) {
            cache.put(lists);
        }

    }

}
