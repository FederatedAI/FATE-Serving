package com.webank.ai.fate.serving.proxy.rpc.router;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.proxy.common.Dict;
import com.webank.ai.fate.serving.proxy.rpc.core.Context;
import com.webank.ai.fate.serving.proxy.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.proxy.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.proxy.utils.FederatedModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public  class ZkServingRouter extends BaseServingRouter implements InitializingBean{
    @Value("${zk.url}")
    private  String  zkUrl ;

    @Value("${useZkRouter:false}")
    private  String  useZkRouter;

    @Value("${acl.username}")
    private String aclUsername;

    @Value("${acl.password}")
    private String aclPassword;

    @Value("${zk.self.port}")
    private String port;

    @Value("${routeType}")
    private String routeTypeString;

    private RouteType routeType;

    @Value("${coordinator}")
    private String selfCoordinator;

    ZookeeperRegistry  zookeeperRegistry;

    com.webank.ai.fate.register.router.RouterService    zkRouterService;


    Logger logger  = LoggerFactory.getLogger(ZkServingRouter.class);

    @Override
    public RouteType getRouteType(){
        return routeType;
    }

    @Override
    public List<RouterInfo> getRouterInfoList(Context context, InboundPackage inboundPackage){
        String environment = getEnvironment(context, inboundPackage);
        List<URL>   list =this.zkRouterService.router("serving", environment, context.getServiceName());
        logger.info("try to find zk ,{}:{}:{}, result {}", "serving", environment, context.getServiceName(), list);
        List<RouterInfo> routeList = new ArrayList<>();
        for(URL url: list){
            String  urlip = url.getHost();
            int  port  = url.getPort();
            RouterInfo router =  new RouterInfo();
            router.setHost(urlip);
            router.setPort(port);
            routeList.add(router);
        }
        return routeList;
    }

    // TODO utu: sucks! have to reconstruct the entire protocol of online serving
    private String getEnvironment(Context context, InboundPackage inboundPackage){
        if("inference".equals(context.getServiceName())){
            // guest, proxy -> serving
            return (String)inboundPackage.getHead().get(Dict.SERVICE_ID);
        }
        // default unaryCall
        if(GrpcType.INTRA_GRPC == context.getGrpcType()){
            // guest, serving -> proxy
            return Dict.SELF_ENVIRONMENT;
        } else {
            Proxy.Packet  sourcePacket = (Proxy.Packet) inboundPackage.getBody();
            if(selfCoordinator.equals(sourcePacket.getHeader().getDst().getPartyId())){
                // host, proxy -> serving
                return FederatedModelUtils.getModelRouteKey(sourcePacket);
            } else {
                // exchange, proxy -> proxy
                return Dict.SELF_ENVIRONMENT;
            }
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        if("true".equals(useZkRouter)&&StringUtils.isNotEmpty(zkUrl)) {

            System.setProperty("acl.username", aclUsername);
            System.setProperty("acl.password", aclPassword);

            zookeeperRegistry = ZookeeperRegistry.getRegistery(zkUrl, Dict.SELF_PROJECT_NAME, Dict.SELF_ENVIRONMENT, Integer.valueOf(port));

            zookeeperRegistry.subProject("serving");

            DefaultRouterService defaultRouterService = new DefaultRouterService();

            defaultRouterService.setRegistry(zookeeperRegistry);

            zkRouterService = defaultRouterService;
        }

        routeType = RouteTypeConvertor.string2RouteType(routeTypeString);

    }
}
