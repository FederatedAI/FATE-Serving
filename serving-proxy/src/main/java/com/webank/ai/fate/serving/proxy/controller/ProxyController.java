package com.webank.ai.fate.serving.proxy.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.proxy.common.Dict;
import com.webank.ai.fate.serving.proxy.rpc.core.*;
import com.webank.ai.fate.serving.proxy.utils.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @Description TODO
 * @Author
 **/
@Controller
public class ProxyController {

    @Autowired
    ProxyServiceRegister proxyServiceRegister;

    Logger logger = LoggerFactory.getLogger(ProxyController.class);

    String binaryReader(HttpServletRequest request) throws IOException {
        int len = request.getContentLength();
        ServletInputStream iii = request.getInputStream();
        byte[] buffer = new byte[len];
        iii.read(buffer, 0, len);
        return new String(buffer);
    }


    @RequestMapping(value = "/federation/{version}/inference", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Callable<String> federation(@PathVariable String version,
                             @RequestBody String data,
                             HttpServletRequest httpServletRequest,
                             @RequestHeader HttpHeaders headers
    ) throws Exception {

        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                logger.info("receive : {} headers {}", data, headers.toSingleValueMap());

                final ServiceAdaptor serviceAdaptor = proxyServiceRegister.getServiceAdaptor("inference");

                Context context = new Context();
                context.setVersion(version);

                InboundPackage<Map> inboundPackage = buildInboundPackageFederation(context, headers, data, httpServletRequest);

                OutboundPackage<Map>  result  =   serviceAdaptor.service(context,inboundPackage );
                if(result!=null&&result.getData()!=null) {
                    result.getData().remove("log");
                    result.getData().remove("warn");
                    result.getData().remove("caseid");
                }
                return  JSON.toJSONString(result.getData());

            }
        };

    }


    private InboundPackage<Map> buildInboundPackageFederation(Context  context ,HttpHeaders headers,
                                                              String data,HttpServletRequest  httpServletRequest) {
        String sourceIp = WebUtil.getIpAddr(httpServletRequest);
        context.setSourceIp(sourceIp);
        context.setCaseId(UUID.randomUUID().toString());

        Map head = Maps.newHashMap();
        // SERVICE_ID == fun(MODEL_ID, MODEL_VERSION)
        head.put(Dict.SERVICE_ID, headers.getFirst(Dict.SERVICE_ID)!=null?headers.getFirst(Dict.SERVICE_ID).trim():"");
        head.put(Dict.MODEL_ID, headers.getFirst(Dict.MODEL_ID)!=null?headers.getFirst(Dict.MODEL_ID).trim():"");
        head.put(Dict.MODEL_VERSION, headers.getFirst(Dict.MODEL_VERSION)!=null?headers.getFirst(Dict.MODEL_VERSION).trim():"");

        Map body = JSON.parseObject(data, Map.class);

        InboundPackage<Map> inboundPackage = new InboundPackage<Map>();
        inboundPackage.setBody(body);
        inboundPackage.setHead(head);
        inboundPackage.setHttpServletRequest(httpServletRequest);
        return inboundPackage;
    }



}