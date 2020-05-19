package com.webank.ai.fate.serving.proxy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.ai.fate.serving.core.bean.BaseContext;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.ServiceAdaptor;
import com.webank.ai.fate.serving.proxy.rpc.core.ProxyServiceRegister;
import com.webank.ai.fate.serving.proxy.utils.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

//    @Autowired
//    IMetricFactory metricFactory;
    Logger logger = LoggerFactory.getLogger(ProxyController.class);
    @Value("${coordinator:9999}")
    private String selfCoordinator;

    /*String binaryReader(HttpServletRequest request) throws IOException {
        int len = request.getContentLength();
        ServletInputStream iii = request.getInputStream();
        byte[] buffer = new byte[len];
        iii.read(buffer, 0, len);
        return new String(buffer);
    }*/


    @RequestMapping(value = "/federation/{version}/{callName}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Callable<String> federation(@PathVariable String version,
                                       @PathVariable String callName,
                                       @RequestBody String data,
                                       HttpServletRequest httpServletRequest,
                                       @RequestHeader HttpHeaders headers
    ) throws Exception {
//        metricFactory.counter("http.inference.request", "http inference request", "callName", callName).increment();

        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (logger.isDebugEnabled()) {
                    logger.debug("receive : {} headers {}", data, headers.toSingleValueMap());
                }

                final ServiceAdaptor serviceAdaptor = proxyServiceRegister.getServiceAdaptor(Dict.SERVICENAME_INFERENCE);

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

//                metricFactory.counter("http.inference.response", "http inference response", "callName", callName).increment();

                return JSON.toJSONString(result.getData());

            }
        };

    }


    private InboundPackage<Map> buildInboundPackageFederation(Context context, String data,
                                                              HttpServletRequest httpServletRequest) {
        String sourceIp = WebUtil.getIpAddr(httpServletRequest);
        context.setSourceIp(sourceIp);
        context.setGuestAppId(selfCoordinator);

        JSONObject jsonObject = JSON.parseObject(data);
        Map head = JSON.parseObject(jsonObject.getString(Dict.HEAD) != null ? jsonObject.getString(Dict.HEAD) : "{}", Map.class);
        Map body = JSON.parseObject(jsonObject.getString(Dict.BODY) != null ? jsonObject.getString(Dict.BODY) : "{}", Map.class);

        context.setHostAppid(head.get(Dict.APP_ID) != null ? head.getOrDefault(Dict.APP_ID, "").toString() : "");
        context.setCaseId(head.get(Dict.CASE_ID) != null ? head.getOrDefault(Dict.CASE_ID, "").toString() : "");
        if (null == context.getCaseId() || context.getCaseId().isEmpty()) {
            context.setCaseId(UUID.randomUUID().toString());
        }

        InboundPackage<Map> inboundPackage = new InboundPackage<Map>();
        inboundPackage.setBody(body);
        inboundPackage.setHead(head);
        return inboundPackage;
    }


}