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

import com.google.common.util.concurrent.ListenableFuture;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.common.async.AbstractAsyncMessageProcessor;
import com.webank.ai.fate.serving.common.async.AsyncMessageEvent;
import com.webank.ai.fate.serving.common.async.Subscribe;
import com.webank.ai.fate.serving.common.utils.GetSystemInfo;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FetchModleEventHandler extends AbstractAsyncMessageProcessor {

    Logger logger = LoggerFactory.getLogger(FetchModleEventHandler.class);

    @Subscribe(value = Dict.EVENT_FETCH_MODEL)
    public void handleFetchModelEvent(AsyncMessageEvent event) {
        logger.info("trigger fetch model event");
        try {
            FetchModelEventData data = (FetchModelEventData) event.getData();

            ManagedChannel channel = GrpcConnectionPool.getPool().getManagedChannel(GetSystemInfo.localIp, MetaInfo.PROPERTY_PORT);
            ModelServiceGrpc.ModelServiceBlockingStub blockingStub = ModelServiceGrpc.newBlockingStub(channel);

            ModelServiceProto.FetchModelRequest.Builder builder = ModelServiceProto.FetchModelRequest.newBuilder();
            builder.setSourceIp(data.getSourceIp()).setSourcePort(data.getSourcePort());
            if (data.getServiceId() != null) {
                builder.setServiceId(data.getServiceId());
            } else {
                builder.setNamespace(data.getNamespace());
                builder.setTableName(data.getTableName());
            }

            ModelServiceProto.FetchModelResponse response = blockingStub.fetchModel(builder.build());
            if (response.getStatusCode() == StatusCode.SUCCESS) {
                logger.info("fetch model success");
            } else {
                logger.info("fetch model failure, code: {}, msg: {}", response.getStatusCode(), response.getMessage());
            }
        } catch (Exception e) {
            logger.error("fetch model error, cause by : {}", e.getMessage());
        }
    }

}
