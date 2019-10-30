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

package com.webank.ai.fate.serving.service;

import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.monitor.WatchDog;
import io.grpc.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServiceOverloadProtectionHandle implements ServerInterceptor {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        String fullMethodName = serverCall.getMethodDescriptor().getFullMethodName();
        String serviceName = fullMethodName.split("/")[1];
        if (StringUtils.isBlank(serviceName)) {
            serverCall.close(Status.DATA_LOSS, metadata);
        }

        ServerCall.Listener<ReqT> delegate = serverCallHandler.startCall(serverCall, metadata);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {

            @Override
            public void onHalfClose() {
                try {
                    WatchDog.enter(serviceName);
                    super.onHalfClose();
                } catch (Exception e) {
                    LOGGER.info("ServiceException:", e);
                    serverCall.close(Status.CANCELLED.withCause(e).withDescription(e.getMessage()), metadata);
                }
            }

            @Override
            public void onCancel() {
                WatchDog.quit(serviceName);
                super.onCancel();
            }

            @Override
            public void onComplete() {
                WatchDog.complete(serviceName);
                super.onComplete();
//                WatchDog.getCount();
            }
        };
    }
}
