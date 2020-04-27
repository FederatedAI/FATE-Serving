package com.webank.ai.fate.serving.admin.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.common.RouterModel;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


/**
 * @Description Service management
 * @Date: 2020/3/25 11:13
 * @Author: v_dylanxu
 */
@RequestMapping("/api")
@RestController
public class ServiceController {

    @Autowired
    private ZookeeperRegistry zookeeperRegistry;

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);

    // 列出集群中所注册的所有接口
    @GetMapping("/service/registered")
    public ReturnResult allRegistered() {
        if (logger.isDebugEnabled()) {
            logger.debug("try to query all registered service");
        }
        Properties properties = zookeeperRegistry.getCacheProperties();

        Map<String, List<String>> registered = new HashMap<>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtils.isNotBlank(value)) {
                String[] arr = value.trim().split("\\s+");
                List<String> urls = new ArrayList<>();
                for (String u : arr) {
                    URL url = URL.valueOf(u);
                    if (!Constants.EMPTY_PROTOCOL.equals(url.getProtocol())) {
                        urls.add(url.toFullString());
                    }
                }

                if (urls.size() > 0) {
                    registered.put(key, urls);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("registered services: {}", registered);
        }

        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.SUCCESS);

        Map data = Maps.newHashMap();
        data.put("registered", registered);
        result.setData(data);
        return result;
    }

    // 修改每个接口中的路由信息，权重信息
    @PostMapping("/service/update")
    public ReturnResult updateWeight(String project, String url, String routerMode, Integer weight, Double version) {
        if (logger.isDebugEnabled()) {
            logger.debug("try to update service weight");
        }

        Preconditions.checkArgument(StringUtils.isNotBlank(project), "parameter project is blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(url), "parameter url is blank");

        logger.info("update {} url: {}, routerMode: {}, weight: {}, version: {}", project, url, routerMode, weight, version);

//        grpc://192.168.0.5:8000/publishBind?ROUTER_MODEL=ALL_ALLOWED&timestamp=1585131517611
        URL originUrl = URL.valueOf(url);
        originUrl = originUrl.setProject(project);

        // unregistered
        zookeeperRegistry.unregister(originUrl);

        Map<String, String> parameters = originUrl.getParameters();
        if (RouterModel.contains(routerMode) && !routerMode.equalsIgnoreCase(originUrl.getParameter(Constants.ROUTER_MODEL))) {
            parameters.put(Constants.ROUTER_MODEL, routerMode);
        }

        String originWeight = originUrl.getParameter(Constants.WEIGHT_KEY);
        if (weight != null && (originWeight == null || weight != Integer.valueOf(originWeight))) {
            parameters.put(Constants.WEIGHT_KEY, String.valueOf(weight));
        }

        String originVersion = originUrl.getParameter(Constants.VERSION_KEY);
        if (version != null && (originVersion == null || version != Double.valueOf(originVersion))) {
            parameters.put(Constants.VERSION_KEY, String.valueOf(version));
        }

        // register
        URL newUrl = new URL(originUrl.getProtocol(), originUrl.getProject(), originUrl.getEnvironment(), originUrl.getHost(), originUrl.getPort(), originUrl.getPath(), parameters);
        zookeeperRegistry.register(newUrl);

        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.SUCCESS);
        return result;
    }

}
