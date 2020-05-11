package com.webank.ai.fate.serving.admin.controller;

import com.google.common.collect.Lists;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperClient;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.admin.services.ComponentService;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Description Service management
 * @Date: 2020/3/25 11:13
 * @Author: v_dylanxu
 */
@RequestMapping("/api")
@RestController
public class ComponentController {

    private static final Logger logger = LoggerFactory.getLogger(ComponentController.class);

    @Autowired
    ZookeeperRegistry  zookeeperRegistry;


    @Autowired
    ComponentService  componentServices;



    @GetMapping("/component/list")
    public ComponentService.NodeData list() {
      return  componentServices.getCachedNodeData();
    }








    // 列出集群中各个组件，能查询每个组件的配置信息，能够显示每个组件的健康状况

    // 组件上报后缓存起来，给出ID方便调用

    // 组件启停

}
