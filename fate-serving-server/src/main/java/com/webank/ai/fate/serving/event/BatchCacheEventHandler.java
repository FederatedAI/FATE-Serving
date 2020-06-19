/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
