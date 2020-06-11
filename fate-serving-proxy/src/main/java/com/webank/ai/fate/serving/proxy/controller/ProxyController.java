package com.webank.ai.fate.serving.proxy.controller;


import com.webank.ai.fate.serving.core.bean.BaseContext;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.ServiceAdaptor;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.proxy.rpc.core.ProxyServiceRegister;
import com.webank.ai.fate.serving.proxy.utils.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${coordinator:9999}")
    private String selfCoordinator;

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
        context.setGuestAppId(selfCoordinator);
        Map jsonObject = JsonUtil.json2Object(data,Map.class);

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


}