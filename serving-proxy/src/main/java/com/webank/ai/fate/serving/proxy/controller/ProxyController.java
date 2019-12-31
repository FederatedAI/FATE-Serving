package com.webank.ai.fate.serving.proxy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.ai.fate.serving.core.bean.BaseContext;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.ServiceAdaptor;
import com.webank.ai.fate.serving.metrics.api.IMetricFactory;
import com.webank.ai.fate.serving.proxy.common.Dict;
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

    @Autowired
    IMetricFactory metricFactory;


    @Value("${coordinator:9999}")
    private String selfCoordinator;

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
        metricFactory.counter("http.inference", "inference request", "request", "all").increment();

        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                logger.info("receive : {} headers {}", data, headers.toSingleValueMap());

                final ServiceAdaptor serviceAdaptor = proxyServiceRegister.getServiceAdaptor("inference");

                Context context = new BaseContext();
                context.setVersion(version);

                InboundPackage<Map> inboundPackage = buildInboundPackageFederation(context, data, httpServletRequest);

                OutboundPackage<Map> result  =   serviceAdaptor.service(context,inboundPackage );
                if(result!=null&&result.getData()!=null) {
                    result.getData().remove("log");
                    result.getData().remove("warn");
                    result.getData().remove("caseid");
                }

                metricFactory.counter("http.inference", "inference response", "response", "all").increment();

                return  JSON.toJSONString(result.getData());

            }
        };

    }


    private InboundPackage<Map> buildInboundPackageFederation(Context  context , String data,
                                                              HttpServletRequest  httpServletRequest) {
        String sourceIp = WebUtil.getIpAddr(httpServletRequest);
        context.setSourceIp(sourceIp);
        context.setCaseId(UUID.randomUUID().toString());
        context.setGuestAppId(selfCoordinator);

        JSONObject jsonObject =JSON.parseObject(data);
        Map head = JSON.parseObject(jsonObject.getString(Dict.HEAD), Map.class);
        Map body = JSON.parseObject(jsonObject.getString(Dict.BODY), Map.class);

        if(null != head){
            context.setHostAppid((String) head.get(Dict.APP_ID));
        }

        InboundPackage<Map> inboundPackage = new InboundPackage<Map>();
        inboundPackage.setBody(body);
        inboundPackage.setHead(head);
//        inboundPackage.setHttpServletRequest(httpServletRequest);
        return inboundPackage;
    }



}