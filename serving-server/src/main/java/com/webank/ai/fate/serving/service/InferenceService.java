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

import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto.InferenceMessage;
import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.core.utils.ObjectTransform;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.guest.GuestInferenceProvider;
import com.webank.ai.fate.serving.utils.InferenceUtils;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InferenceService extends InferenceServiceGrpc.InferenceServiceImplBase {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Logger accessLOGGER = LogManager.getLogger(Dict.ACCESS);
    @Autowired
    GuestInferenceProvider guestInferenceProvider;


    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = "inference")
    public void inference(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {
        inferenceServiceAction(req, responseObserver, InferenceActionType.SYNC_RUN);
    }

    @Override
    @RegisterService(serviceName = "getInferenceResult")
    public void getInferenceResult(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {
        inferenceServiceAction(req, responseObserver, InferenceActionType.GET_RESULT);
    }

    @Override
    @RegisterService(useDynamicEnvironment = true, serviceName = "startInferenceJob")
    public void startInferenceJob(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver) {
        inferenceServiceAction(req, responseObserver, InferenceActionType.ASYNC_RUN);

    }

    private void inferenceServiceAction(InferenceMessage req, StreamObserver<InferenceMessage> responseObserver, InferenceActionType actionType) {

        InferenceMessage.Builder response = InferenceMessage.newBuilder();
        ReturnResult returnResult = new ReturnResult();

        InferenceRequest inferenceRequest = null;
        Context context = new BaseContext(new GuestInferenceLoggerPrinter());
        context.preProcess();

        try {
            try {
                context.putData(Dict.ORIGIN_REQUEST, req.getBody().toStringUtf8());
                inferenceRequest = (InferenceRequest) ObjectTransform.json2Bean(req.getBody().toStringUtf8(), InferenceRequest.class);

                if (inferenceRequest != null) {
                    if (inferenceRequest.getCaseid().length() == 0) {
                        inferenceRequest.setCaseId(InferenceUtils.generateCaseid());
                    }
                    context.setCaseId(inferenceRequest.getCaseid());
                    context.setActionType(actionType.name());

                    switch (actionType.name()) {
                        case "SYNC_RUN":
                            returnResult = guestInferenceProvider.syncInference(context, inferenceRequest);
                            break;
                        case "GET_RESULT":
                            returnResult = guestInferenceProvider.getResult(context, inferenceRequest);
                            break;
                        case "ASYNC_RUN":
                            returnResult = guestInferenceProvider.asynInference(context, inferenceRequest);
                            break;
                        default:

                            throw new Exception();


                    }

                    //  returnResult = inferenceProvider.inference(context,inferenceRequest, actionType);
                    if (returnResult.getRetcode() != InferenceRetCode.OK) {
                        LOGGER.error("caseid {} inference {} failed: {}  result {}", context.getCaseId(), actionType, req.getBody().toStringUtf8(), returnResult);
                    }
                } else {

                    returnResult.setRetcode(InferenceRetCode.EMPTY_DATA);
                }
            } catch (Throwable e) {

                returnResult.setRetcode(InferenceRetCode.SYSTEM_ERROR);
                LOGGER.error(String.format("inference system error:\n%s", req.getBody().toStringUtf8()), e);
            }

            response.setBody(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult).getBytes()));
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } finally {

            context.postProcess(inferenceRequest, returnResult);
        }
    }
}
