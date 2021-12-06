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

package com.webank.ai.fate.serving.controller;

import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.common.bean.ServingServerContext;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.ObjectTransform;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import com.webank.ai.fate.serving.guest.provider.GuestBatchInferenceProvider;
import com.webank.ai.fate.serving.guest.provider.GuestSingleInferenceProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @Description provide HTTP Interface of inference
 * @Author
 **/
@Controller
public class InferenceController {

    Logger logger = LoggerFactory.getLogger(InferenceController.class);

    @Autowired
    GuestBatchInferenceProvider guestBatchInferenceProvider;
    @Autowired
    GuestSingleInferenceProvider guestSingleInferenceProvider;

    @RequestMapping(value = "/federation/v1/{callName}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Callable<String> federation(@PathVariable String callName,
                                       @RequestBody String data,
                                       HttpServletRequest httpServletRequest,
                                       @RequestHeader HttpHeaders headers
    ) throws Exception {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (logger.isDebugEnabled()) {
                    logger.debug("receive : {} headers {}", data, headers.toSingleValueMap());
                }

                Map inferenceReqMap = buildFederationMap(data);

                InferenceServiceProto.InferenceMessage.Builder reqBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
                reqBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(inferenceReqMap).getBytes()));

                String resultString;

                if (Dict.SERVICENAME_INFERENCE.equals(callName)) {
                    resultString = inference(reqBuilder.build(), guestSingleInferenceProvider);
                } else if (Dict.SERVICENAME_BATCH_INFERENCE.equals(callName)) {
                    resultString = inference(reqBuilder.build(), guestBatchInferenceProvider);
                } else {
                    logger.error("URI: /federation/v1/{} is Error!", callName);
                    return "URI: {/federation/v1/" + callName+ "} is Error!" ;
                }

                if (StringUtils.isNotEmpty(resultString)) {
                    Map resultMap = JsonUtil.json2Object(resultString, Map.class);

                    resultMap.remove("log");
                    resultMap.remove("warn");
                    resultMap.remove("caseid");

                    if (logger.isDebugEnabled()) {
                        logger.debug("{} response : {} ", callName, resultMap);
                    }

                    return JsonUtil.object2Json(resultMap);
                } else {
                    logger.error("Error,no inference result returned");
                    return "Error,no inference result returned";
                }

            }
        };
    }

    private Map buildFederationMap(String data) {
        Map jsonObject = JsonUtil.json2Object(data, Map.class);

        Map head = (Map) jsonObject.getOrDefault(Dict.HEAD, new HashMap<>());
        Map body = (Map) jsonObject.getOrDefault(Dict.BODY, new HashMap<>());

        String caseId = (String) head.getOrDefault(Dict.CASE_ID, "");
        if (null == caseId || caseId.isEmpty()) {
            caseId = UUID.randomUUID().toString().replaceAll("-", "");
        }

        Map inferenceReqMap = Maps.newHashMap();
        inferenceReqMap.put(Dict.CASE_ID, caseId);
        inferenceReqMap.putAll(head);
        inferenceReqMap.putAll(body);

        return inferenceReqMap;
    }


    private String inference(InferenceServiceProto.InferenceMessage req, AbstractServingServiceProvider serviceProvider) {
        Context context = new ServingServerContext();
        InboundPackage inboundPackage = new InboundPackage();
        inboundPackage.setBody(req);
        OutboundPackage outboundPackage = serviceProvider.service(context, inboundPackage);
        return ObjectTransform.bean2Json(outboundPackage.getData());
    }


}