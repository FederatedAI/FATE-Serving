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

import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.common.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.common.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.common.async.Subscribe;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.provider.ModelServiceProvider;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ModelActionType;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class FetchModelEventHandler extends AbstractAsyncMessageProcessor implements InitializingBean {

    Logger logger = LoggerFactory.getLogger(FetchModelEventHandler.class);

    @Autowired
    ModelServiceProvider modelServiceProvider;

    ConcurrentHashMap<String, FetchModelEventData> waitingFetchMap =  new ConcurrentHashMap<>();
    ConcurrentMap<String,Long> alreadySendMap = new ConcurrentHashMap<>();

    ExecutorService fetchModelThreadPool = Executors.newFixedThreadPool(5);

    @Subscribe(value = Dict.EVENT_FETCH_MODEL)
    public void handleFetchModelEvent(AsyncMessageEvent event) {
        logger.info("trigger fetch model event");
        try {
            FetchModelEventData data = (FetchModelEventData) event.getData();
            StringBuilder keyBuilder = new StringBuilder();
            if (data.getServiceId() != null) {
                keyBuilder.append(data.getServiceId());
            } else {
                keyBuilder.append(data.getTableName()).append("#").append(data.getNamespace());
            }
            logger.info("model {} waiting to fetch", keyBuilder);
            waitingFetchMap.put(keyBuilder.toString(), data);
        } catch (Exception e) {
            logger.error("fetch model error, cause by : {}", e.getMessage());
        }
    }

    private void doFetchModel(FetchModelEventData data) {
        logger.info("fetch model by {}", data);
        ServingServerContext context = new ServingServerContext();
        context.setActionType(ModelActionType.FETCH_MODEL.name());
        context.setCaseId(UUID.randomUUID().toString().replaceAll("-", ""));

        ModelServiceProto.FetchModelRequest.Builder builder = ModelServiceProto.FetchModelRequest.newBuilder();
        builder.setSourceIp(data.getSourceIp()).setSourcePort(data.getSourcePort());
        if (data.getServiceId() != null) {
            builder.setServiceId(data.getServiceId());
        } else {
            builder.setNamespace(data.getNamespace());
            builder.setTableName(data.getTableName());
        }

        InboundPackage<ModelServiceProto.FetchModelRequest> inboundPackage = new InboundPackage();
        inboundPackage.setBody(builder.build());
        OutboundPackage outboundPackage = modelServiceProvider.service(context, inboundPackage);
        ModelServiceProto.FetchModelResponse response = (ModelServiceProto.FetchModelResponse) outboundPackage.getData();
        if (response.getStatusCode() == StatusCode.SUCCESS) {
            logger.info("fetch model success");
        } else {
            logger.info("fetch model failure, code: {}, msg: {}", response.getStatusCode(), response.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        new Thread(() -> {
            while (true) {
                try {
                    if (waitingFetchMap.size() > 0) {
                        logger.info("waitingFetchMap {}", waitingFetchMap);
                    }
                    waitingFetchMap.forEach((k, v) -> {
                        try {
                            long now = System.currentTimeMillis();
                            if (alreadySendMap.putIfAbsent(k, now) == null) {
                                fetchModelThreadPool.execute(() -> {
                                    try {
                                        this.doFetchModel(v);
                                    } finally {
                                        alreadySendMap.remove(k);
                                    }
                                    waitingFetchMap.remove(k);
                                });
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    });
                    Thread.sleep(200);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
