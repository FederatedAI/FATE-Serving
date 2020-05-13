package com.webank.ai.fate.serving.admin.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.common.RouterMode;
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

    private static final Logger logger = LoggerFactory.getLogger(ServiceController.class);
    @Autowired
    private ZookeeperRegistry zookeeperRegistry;

    // 列出集群中所注册的所有接口
    @GetMapping("/service/registered")
    public ReturnResult allRegistered() {
        if (logger.isDebugEnabled()) {
            logger.debug("try to query all registered service");
        }
        Properties properties = zookeeperRegistry.getCacheProperties();

        Map<String, List<Object>> registered = new HashMap<>();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            // serving/9999/batchInference
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtils.isNotBlank(value)) {
                String[] arr = value.trim().split("\\s+");
                List<Object> urls = new ArrayList<>();
                for (String u : arr) {
                    URL url = URL.valueOf(u);
                    if (!Constants.EMPTY_PROTOCOL.equals(url.getProtocol())) {
                        String[] split = key.split("/");
                        Map data = Maps.newHashMap();
                        data.put("url", url.toFullString());
                        data.put("project", split[0]);
                        data.put("environment", split[1]);
                        data.put("name", key);
                        data.put("host", url.getHost());
                        data.put("port", url.getPort());
                        data.put("routerMode", url.getParameter("router_mode"));
                        data.put("version", url.getParameter("version", 100));
                        data.put("weight", url.getParameter("weight", 100));

//                        urls.add(url);
                        urls.add(data);
                    }
                }

                if (urls.size() > 0) {
                    registered.put(key, urls);
                }
            }
        }


        logger.info("registered services: {}", registered);

        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.SUCCESS);

        Map data = Maps.newHashMap();
        data.put("total", registered.size());
        data.put("rows", registered);
        result.setData(data);
        return result;
    }

    // 修改每个接口中的路由信息，权重信息
    @PostMapping("/service/update")
    public ReturnResult updateWeight(String project, String url, String routerMode, Integer weight, Long version) {
        if (logger.isDebugEnabled()) {
            logger.debug("try to update service weight");
        }

        Preconditions.checkArgument(StringUtils.isNotBlank(project), "parameter project is blank");
        Preconditions.checkArgument(StringUtils.isNotBlank(url), "parameter url is blank");

        logger.info("update {} url: {}, routerMode: {}, weight: {}, version: {}", project, url, routerMode, weight, version);

        ReturnResult result = new ReturnResult();
        result.setRetcode(StatusCode.SUCCESS);

        // grpc://10.58.8.74:8000/inference?router_mode=VERSION_BIGER&timestamp=1589266159585&version=100&weight=50
        URL originUrl = URL.valueOf(url);
        originUrl = originUrl.setProject(project);

        Map<String, String> originParameters = originUrl.getParameters();

        boolean hasChange = false;
        HashMap<String, String> parameters = Maps.newHashMap(originUrl.getParameters());
        if (RouterMode.contains(routerMode) && !routerMode.equalsIgnoreCase(originUrl.getParameter(Constants.ROUTER_MODE))) {
            parameters.put(Constants.ROUTER_MODE, routerMode);
            hasChange = true;
        }

        String originWeight = originUrl.getParameter(Constants.WEIGHT_KEY);
        if (weight != null && (originWeight == null || weight != Integer.parseInt(originWeight))) {
            parameters.put(Constants.WEIGHT_KEY, String.valueOf(weight));
            hasChange = true;
        }

        String originVersion = originUrl.getParameter(Constants.VERSION_KEY);
        if (version != null && (originVersion == null || version != Long.parseLong(originVersion))) {
            parameters.put(Constants.VERSION_KEY, String.valueOf(version));
            hasChange = true;
        }

        if (hasChange) {
            // unregistered
            zookeeperRegistry.unregister(originUrl);

            // register
            URL newUrl = new URL(originUrl.getProtocol(), originUrl.getProject(), originUrl.getEnvironment(), originUrl.getHost(), originUrl.getPort(), originUrl.getPath(), parameters);
            zookeeperRegistry.register(newUrl);
        } else {
            result.setRetmsg("no change");
        }
        return result;
    }

}
