package com.webank.ai.fate.serving.proxy.rpc.router;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.proxy.common.Dict;
import com.webank.ai.fate.serving.proxy.rpc.core.Context;
import com.webank.ai.fate.serving.proxy.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.proxy.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.proxy.utils.FederatedModelUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public  class ZkServingRouter extends BaseServingRouter implements InitializingBean{

    @Value("${useZkRouter:false}")
    private  String  useZkRouter;

    @Value("${routeType:random}")
    private String routeTypeString;

    private RouteType routeType;

    @Value("${coordinator:9999}")
    private String selfCoordinator;

    public static void setZkRouterService(RouterService zkRouterService) {
        ZkServingRouter.zkRouterService = zkRouterService;
    }

    private static RouterService zkRouterService;


    private static final Logger logger = LogManager.getLogger();

    @Override
    public RouteType getRouteType(){
        return routeType;
    }

    @Override
    public List<RouterInfo> getRouterInfoList(Context context, InboundPackage inboundPackage){
        if(!"true".equals(useZkRouter)){
            return null;
        }
        String environment = getEnvironment(context, inboundPackage);
        List<URL>   list =this.zkRouterService.router("serving", environment, context.getServiceName());
        logger.info("try to find zk ,{}:{}:{}, result {}", "serving", environment, context.getServiceName(), list);
        if(null == list || list.isEmpty()){
            return null;
        }
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
        routeType = RouteTypeConvertor.string2RouteType(routeTypeString);
    }
}
