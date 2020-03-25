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

package com.webank.ai.fate.serving.grpc.service;

import com.alibaba.fastjson.JSON;
import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.proxy.DataTransferServiceGrpc;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.api.networking.proxy.Proxy.Packet;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.host.provider.BatchHostInferenceProvider;
import com.webank.ai.fate.serving.host.provider.HostSecureBoostTreeInferenceProvider;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProxyService extends DataTransferServiceGrpc.DataTransferServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);
//    @Autowired
//    HostInferenceProvider hostInferenceProvider;

    @Autowired
    BatchHostInferenceProvider  batchHostInferenceProvider;

    @Autowired
    HostSecureBoostTreeInferenceProvider   hostSecureBoostTreeInferenceProvider;


    @Autowired
    MetricRegistry  metricRegistry;

    @Override
    @RegisterService(serviceName = Dict.UNARYCALL, useDynamicEnvironment = true)
    public void unaryCall(Proxy.Packet req, StreamObserver<Proxy.Packet> responseObserver) {

        String actionType =  req.getHeader().getCommand().getName();

        Context context = new BaseContext();
        context.setActionType(req.getHeader().getCommand().getName());
      //  context.preProcess();
      //  HostFederatedParams requestData = null;

        Object result = null;

        try {

            String data = req.getBody().getValue().toStringUtf8();
            if (logger.isDebugEnabled()) {
                logger.debug("unaryCall {} head {}", data, req.getHeader().getCommand().getName());
            }
            //requestData = JSON.parseObject(data, HostFederatedParams.class);
            //context.setCaseId(requestData.getCaseId() != null ? requestData.getCaseId() : Dict.NONE);
            InboundPackage inboundPackage = new InboundPackage();
            switch (req.getHeader().getCommand().getName()) {

                case Dict.FEDERATED_INFERENCE_FOR_TREE:
                    Map  request = JSON.parseObject(data,Map.class);
                     inboundPackage = new InboundPackage();
                    inboundPackage.setBody(request);
                    OutboundPackage secureBoostTreeOutboundPackage = hostSecureBoostTreeInferenceProvider.service(context, inboundPackage);

                    result = secureBoostTreeOutboundPackage.getData();
                    break;
                case Dict.REMOTE_METHOD_BATCH:
                    BatchInferenceRequest  batchInferenceRequest = JSON.parseObject(data,BatchInferenceRequest.class);

                    inboundPackage.setBody(batchInferenceRequest);
                    OutboundPackage outboundPackage = this.batchHostInferenceProvider.service(context, inboundPackage);

                    ReturnResult responseResult = null;
                    result = (ReturnResult) outboundPackage.getData();
                    break;

                default:
                    responseResult = new ReturnResult();
                    responseResult.setRetcode(StatusCode.HOST_NOT_SUPPORT_ERROR);
                    break;
            }

            Packet.Builder packetBuilder = Packet.newBuilder();
            packetBuilder.setBody(Proxy.Data.newBuilder()
                    .setValue(ByteString.copyFrom(JSON.toJSONString(result).getBytes()))
                    .build());

//            Proxy.Metadata.Builder metaDataBuilder = Proxy.Metadata.newBuilder();
//            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();
//            FederatedParty partnerParty = requestData.getPartnerLocal();
//            FederatedParty party = requestData.getLocal();
//            context.putData(Dict.GUEST_APP_ID, partnerParty.getPartyId());
//            context.putData(Dict.HOST_APP_ID, party.getPartyId());

//            metaDataBuilder.setSrc(
//                    topicBuilder.setPartyId(String.valueOf(party.getPartyId()))
//                            .setRole(Dict.HOST)
//                            .setName(Dict.MY_PARTY_NAME)
//                            .build());
//            metaDataBuilder.setDst(
//                    topicBuilder.setPartyId(String.valueOf(partnerParty.getPartyId()))
//                            .setRole(Dict.GUEST)
//                            .setName(Dict.PARTNER_PARTY_NAME)
//                            .build());
//            packetBuilder.setHeader(metaDataBuilder.build());
            responseObserver.onNext(packetBuilder.build());
            responseObserver.onCompleted();
        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            //context.postProcess(requestData, responseResult);

        }
    }
}