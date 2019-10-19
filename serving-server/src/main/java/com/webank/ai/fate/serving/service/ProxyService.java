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

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.api.networking.proxy.Proxy.Packet;
import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.core.constant.StatusCode;
import com.webank.ai.fate.core.utils.ObjectTransform;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.host.HostInferenceProvider;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProxyService extends DataTransferServiceGrpc.DataTransferServiceImplBase {
    private static final Logger logger = LogManager.getLogger();
    @Autowired
    HostInferenceProvider hostInferenceProvider;


    @Override
    @RegisterService(serviceName = Dict.UNARYCALL, useDynamicEnvironment = true)
    public void unaryCall(Proxy.Packet req, StreamObserver<Proxy.Packet> responseObserver) {
        ReturnResult responseResult = null;
        Context context = new BaseContext(new HostInferenceLoggerPrinter());
        context.setActionType(req.getHeader().getCommand().getName());
        context.preProcess();
        //Map<String, Object> requestData=null;
        HostFederatedParams requestData = null;

        try {
            //requestData = (Map<String, Object>) ObjectTransform.json2Bean(req.getBody().getValue().toStringUtf8(), HashMap.class);
            //{"caseId":"73aca9d0dec811e9a0af5254005e961b","featureIdMap":{"device_id":"xxxxxxxxxx","phone_num":""},"local":{"partyId":"10000","role":"host"},
            // "partnerLocal":{"partyId":"9999","role":"guest"},"partnerModelInfo":{"name":"201909241953242070093","namespace":"guest#9999#guest-9999#host-10000#model"},
            // "role":{"allRole":{"host":["10000"],"guest":["9999"]}},"seqNo":"1b91bb3621704dc3bf4e63a1ed22e81d"}


            String data = req.getBody().getValue().toStringUtf8();
            logger.info("unaryCall {}", data);

            requestData = JSON.parseObject(data, HostFederatedParams.class);

            context.setCaseId(requestData.getCaseId() != null ? requestData.getCaseId() : Dict.NONE);

            switch (req.getHeader().getCommand().getName()) {
                case Dict.FEDERATED_INFERENCE:
                    responseResult = hostInferenceProvider.federatedInference(context, requestData);
                    break;
                case Dict.FEDERATED_INFERENCE_FOR_TREE:
                    responseResult = hostInferenceProvider.federatedInferenceForTree(context, requestData);
                    break;

                default:
                    responseResult = new ReturnResult();
                    responseResult.setRetcode(StatusCode.PARAMERROR);
                    break;
            }

            Packet.Builder packetBuilder = Packet.newBuilder();
            packetBuilder.setBody(Proxy.Data.newBuilder()
                    .setValue(ByteString.copyFrom(ObjectTransform.bean2Json(responseResult).getBytes()))
                    .build());

            Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();

            FederatedParty partnerParty = requestData.getPartnerLocal();
            FederatedParty party = requestData.getLocal();


//            FederatedParty partnerParty = (FederatedParty) ObjectTransform.json2Bean(requestData.get("partner_local").toString(), FederatedParty.class);
//            FederatedParty party = (FederatedParty) ObjectTransform.json2Bean(requestData.get("local").toString(), FederatedParty.class);

            metaDataBuilder.setSrc(
                    topicBuilder.setPartyId(String.valueOf(party.getPartyId()))
                            .setRole(Dict.HOST)
                            .setName(Dict.MY_PARTY_NAME)
                            .build());
            metaDataBuilder.setDst(
                    topicBuilder.setPartyId(String.valueOf(partnerParty.getPartyId()))
                            .setRole(Dict.GUEST)
                            .setName(Dict.PARTNER_PARTY_NAME)
                            .build());
            packetBuilder.setHeader(metaDataBuilder.build());
            responseObserver.onNext(packetBuilder.build());
            responseObserver.onCompleted();
        } finally {
            context.postProcess(requestData, responseResult);

        }
    }
}