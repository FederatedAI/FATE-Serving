package com.webank.ai.fate.serving.admin.services;

import com.google.common.collect.Lists;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperClient;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.webank.ai.fate.register.common.Constants.PATH_SEPARATOR;

@Service
public class ComponentService {
    @Autowired
    ZookeeperRegistry zookeeperRegistry;
    private final static String PATH_SEPARATOR ="/";
    private final static String DEFAULT_COMPONENT_ROOT = "FATE-COMPONENTS";

    Logger logger = LoggerFactory.getLogger(ComponentService.class);

    public  NodeData    getCachedNodeData(){
        return  cachedNodeData;
    }

    public  class  NodeData{
        String name;

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

        List<NodeData>  children= Lists.newArrayList();
    }





    NodeData  cachedNodeData;

    @Scheduled(cron = "0/5 * * * * ?")
    public void schedulePullComponent(){


        ZookeeperClient zkClient = zookeeperRegistry.getZkClient();
        NodeData  root  = new NodeData();
        root.setName("cluster");
        List<String> componentLists =  zkClient.getChildren(PATH_SEPARATOR+DEFAULT_COMPONENT_ROOT);
        if(componentLists!=null){
            componentLists.forEach(name->{
                List<String>  nodes =zkClient.getChildren(PATH_SEPARATOR+DEFAULT_COMPONENT_ROOT+PATH_SEPARATOR+name);
                NodeData  componentData = new NodeData();
                componentData.setName(name);
                root.getChildren().add(componentData);
                if(CollectionUtils.isNotEmpty(nodes)){
                    nodes.forEach(nodeName->{
                        NodeData  nodeData = new NodeData();
                        nodeData.setName(nodeName);
                        componentData.getChildren().add(nodeData);
                    });
                }


            });
        }
        cachedNodeData =  root;
        logger.info("refresh  component info ");
    }





}
