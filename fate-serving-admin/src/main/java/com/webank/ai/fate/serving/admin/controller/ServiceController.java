package com.webank.ai.fate.serving.admin.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.common.RouterMode;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.RequestParamWrapper;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.bean.ServiceDataWrapper;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


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
    @GetMapping("/service/list")
    public ReturnResult listRegistered(Integer page, Integer pageSize) {
        if (page == null || page < 0) {
            page = 1;
        }

        if (pageSize == null) {
            pageSize = 10;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("try to query all registered service");
        }
        Properties properties = zookeeperRegistry.getCacheProperties();

        List<ServiceDataWrapper> resultList = new ArrayList<>();
        int totalSize = 0;
        int index = 0;
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            // serving/9999/batchInference
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (StringUtils.isNotBlank(value)) {
                String[] arr = value.trim().split("\\s+");
                for (String u : arr) {
                    URL url = URL.valueOf(u);
                    if (!Constants.EMPTY_PROTOCOL.equals(url.getProtocol())) {
                        String[] split = key.split("/");
                        ServiceDataWrapper wrapper = new ServiceDataWrapper();
                        wrapper.setUrl(url.toFullString());
                        wrapper.setProject(split[0]);
                        wrapper.setEnvironment(split[1]);
                        wrapper.setName(key);
                        wrapper.setHost(url.getHost());
                        wrapper.setPort(url.getPort());
                        wrapper.setRouterMode(String.valueOf(url.getParameter("router_mode")));
                        wrapper.setVersion(Long.parseLong(url.getParameter("version", "100")));
                        wrapper.setWeight(Integer.parseInt(url.getParameter("weight", "100")));
                        wrapper.setIndex(index);
                        resultList.add(wrapper);
                        index++;
                    }
                }
            }
        }

        totalSize = resultList.size();

        resultList = resultList.stream().sorted((Comparator.comparingInt(o -> (o.getProject() + o.getEnvironment()).hashCode()))).collect(Collectors.toList());
        // Pagination
        int totalPage = (resultList.size() + pageSize - 1) / pageSize;
        if (page <= totalPage) {
            resultList = resultList.subList((page - 1) * pageSize, Math.min(page * pageSize, resultList.size()));
        }

        if (logger.isDebugEnabled()) {
            logger.info("registered services: {}", resultList);
        }

        Map data = Maps.newHashMap();
        data.put("total", totalSize);
        data.put("rows", resultList);
        return ReturnResult.build(StatusCode.SUCCESS, Dict.SUCCESS, data);
    }

    // 修改每个接口中的路由信息，权重信息
    @PostMapping("/service/update")
    public ReturnResult updateService(@RequestBody RequestParamWrapper requestParams) {
        String project = requestParams.getProject();
        String url = requestParams.getUrl();
        String routerMode = requestParams.getRouterMode();
        Integer weight = requestParams.getWeight();
        Long version = requestParams.getVersion();

        if (logger.isDebugEnabled()) {
            logger.debug("try to update service");
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
