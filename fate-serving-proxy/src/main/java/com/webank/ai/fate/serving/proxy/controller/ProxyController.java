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

package com.webank.ai.fate.serving.proxy.controller;

import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.common.bean.BaseContext;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.ServiceAdaptor;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.ProtobufUtils;
import com.webank.ai.fate.serving.proxy.rpc.core.ProxyServiceRegister;
import com.webank.ai.fate.serving.proxy.utils.WebUtil;
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
 * @Description TODO
 * @Author
 **/
@Controller
public class ProxyController {

    Logger logger = LoggerFactory.getLogger(ProxyController.class);

    @Autowired
    ProxyServiceRegister proxyServiceRegister;


    @RequestMapping(value = "/unary", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Callable<String> uncaryCall(
                                       @RequestBody String data,
                                       HttpServletRequest httpServletRequest,
                                       @RequestHeader HttpHeaders headers
    ) throws Exception {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                    final ServiceAdaptor serviceAdaptor = proxyServiceRegister.getServiceAdaptor("unaryCall");
                    Proxy.Packet.Builder packetBuilder = Proxy.Packet.newBuilder();
                    try {
                        JsonFormat.parser().merge(data, packetBuilder);
                    }catch(Exception e){
                        logger.error("invalid http param {}",data);
                        return JsonFormat.printer().print(returnInvalidParam());
                    }
                    packetBuilder.build();
                    Context context = new BaseContext();
                    context.setCallName("unaryCall");
                    context.setGrpcType(GrpcType.INTER_GRPC);
                    InboundPackage<Proxy.Packet> inboundPackage = buildInboundPackage(context, packetBuilder.build());
                    OutboundPackage<Proxy.Packet> result = serviceAdaptor.service(context, inboundPackage);
                    return JsonFormat.printer().print(result.getData());

            }
        };
    }


    @RequestMapping(value = "/federation/{version}/{callName}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Callable<String> federation(@PathVariable String version,
                                       @PathVariable String callName,
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

                final ServiceAdaptor serviceAdaptor = proxyServiceRegister.getServiceAdaptor(callName);

                Context context = new BaseContext();
                context.setCallName(callName);
                context.setVersion(version);

                InboundPackage<Map> inboundPackage = buildInboundPackageFederation(context, data, httpServletRequest);

                OutboundPackage<Map> result = serviceAdaptor.service(context, inboundPackage);
                if (result != null && result.getData() != null) {
                    result.getData().remove("log");
                    result.getData().remove("warn");
                    result.getData().remove("caseid");
                }

                return JsonUtil.object2Json(result.getData());
            }
        };
    }

    private InboundPackage<Map> buildInboundPackageFederation(Context context, String data,
                                                              HttpServletRequest httpServletRequest) {
        String sourceIp = WebUtil.getIpAddr(httpServletRequest);
        context.setSourceIp(sourceIp);
        context.setGuestAppId(String.valueOf(MetaInfo.PROPERTY_COORDINATOR));
        Map jsonObject = JsonUtil.json2Object(data, Map.class);

        Map head = (Map) jsonObject.getOrDefault(Dict.HEAD, new HashMap<>());
        Map body = (Map) jsonObject.getOrDefault(Dict.BODY, new HashMap<>());
        context.setHostAppid((String) head.getOrDefault(Dict.APP_ID, ""));
        context.setCaseId((String) head.getOrDefault(Dict.CASE_ID, ""));
        if (null == context.getCaseId() || context.getCaseId().isEmpty()) {
            context.setCaseId(UUID.randomUUID().toString().replaceAll("-", ""));
        }

        InboundPackage<Map> inboundPackage = new InboundPackage<Map>();
        inboundPackage.setBody(body);
        inboundPackage.setHead(head);
        return inboundPackage;
    }


    private  InboundPackage<Proxy.Packet> buildInboundPackage(Context context, Proxy.Packet req) {
        context.setCaseId(Long.toString(System.currentTimeMillis()));
        if (StringUtils.isNotBlank(req.getHeader().getOperator())) {
            context.setVersion(req.getHeader().getOperator());
        }
        context.setGuestAppId(req.getHeader().getSrc().getPartyId());
        context.setHostAppid(req.getHeader().getDst().getPartyId());
        InboundPackage<Proxy.Packet> inboundPackage = new InboundPackage<Proxy.Packet>();
        inboundPackage.setBody(req);

        return inboundPackage;
    }


    private  Proxy.Packet   returnInvalidParam(){
        Proxy.Packet.Builder builder = Proxy.Packet.newBuilder();
        Proxy.Data.Builder dataBuilder = Proxy.Data.newBuilder();
        Map fateMap = Maps.newHashMap();
        fateMap.put(Dict.RET_CODE, StatusCode.PROXY_PARAM_ERROR);
        fateMap.put(Dict.RET_MSG, "invalid http param");
        builder.setBody(dataBuilder.setValue(ByteString.copyFromUtf8(JsonUtil.object2Json(fateMap))));
        return builder.build();
    }


}