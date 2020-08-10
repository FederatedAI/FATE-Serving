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

package com.webank.ai.fate.serving.admin.services;

import com.google.common.collect.Lists;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperClient;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ComponentService {

    private final static String PATH_SEPARATOR = "/";
    private final static String DEFAULT_COMPONENT_ROOT = "FATE-COMPONENTS";
    Logger logger = LoggerFactory.getLogger(ComponentService.class);
    @Autowired
    ZookeeperRegistry zookeeperRegistry;
    NodeData cachedNodeData;
    Set<String> whitelist = new HashSet<>();

    public NodeData getCachedNodeData() {
        return cachedNodeData;
    }

    public Set<String> getWhitelist() {
        return whitelist;
    }

    public boolean isAllowAccess(String host, int port) {
        if (whitelist.contains(host + ":" + port)) {
            return true;
        }
        return false;
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void schedulePullComponent() {
        ZookeeperClient zkClient = zookeeperRegistry.getZkClient();
        NodeData root = new NodeData();
        root.setName("cluster");
        root.setLabel(root.getName());
        List<String> componentLists = zkClient.getChildren(PATH_SEPARATOR + DEFAULT_COMPONENT_ROOT);
        if (componentLists != null) {
            Set<String> registeredNodes = new HashSet<>();
            componentLists.forEach(name -> {
                List<String> nodes = zkClient.getChildren(PATH_SEPARATOR + DEFAULT_COMPONENT_ROOT + PATH_SEPARATOR + name);
                if (nodes != null) {
                    registeredNodes.addAll(nodes);
                }

                NodeData componentData = new NodeData();
                componentData.setName(name);
                componentData.setLabel(root.getLabel() + "-" + componentData.getName());
                root.getChildren().add(componentData);
                if (CollectionUtils.isNotEmpty(nodes)) {
                    nodes.forEach(nodeName -> {
                        String content = zkClient.getContent(PATH_SEPARATOR + DEFAULT_COMPONENT_ROOT + PATH_SEPARATOR + name + PATH_SEPARATOR + nodeName);
                        Map contentMap = JsonUtil.json2Object(content, Map.class);
                        NodeData nodeData = new NodeData();
                        nodeData.setName(nodeName);
                        nodeData.setLeaf(true);
                        nodeData.setLabel(componentData.getLabel() + "-" + nodeData.getName());
                        nodeData.setTimestamp((Long) contentMap.get(Constants.TIMESTAMP_KEY));
                        componentData.getChildren().add(nodeData);
                    });
                }
            });

            whitelist.clear();
            whitelist.addAll(registeredNodes);
        }
        cachedNodeData = root;

        if (logger.isDebugEnabled()) {
            logger.debug("refresh  component info ");
        }
    }

    public class NodeData {
        String name;
        boolean leaf;
        String label;
        long timestamp;
        List<NodeData> children = Lists.newArrayList();

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public boolean isLeaf() {
            return leaf;
        }

        public void setLeaf(boolean leaf) {
            this.leaf = leaf;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<NodeData> getChildren() {
            return children;
        }

        public void setChildren(List<NodeData> children) {
            this.children = children;
        }
    }


}
