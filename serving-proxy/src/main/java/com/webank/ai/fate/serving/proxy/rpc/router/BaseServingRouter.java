package com.webank.ai.fate.serving.proxy.rpc.router;

import com.google.common.hash.Hashing;
import com.webank.ai.fate.serving.proxy.exceptions.NoRouteInfoException;
import com.webank.ai.fate.serving.proxy.rpc.core.Context;
import com.webank.ai.fate.serving.proxy.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.proxy.rpc.core.OutboundPackage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public abstract class BaseServingRouter implements RouterInterface{
    private static final Logger logger  = LogManager.getLogger();

    public abstract List<RouterInfo> getRouterInfoList(Context context, InboundPackage inboundPackage);

    public abstract RouteType getRouteType();

    @Override
    public RouterInfo route(Context context, InboundPackage inboundPackage) {
        List<RouterInfo> routeList =  getRouterInfoList(context,inboundPackage);

        if(routeList==null
        || 0 == routeList.size()){
            return null;
        }
        int idx = 0;
        RouteType routeType = getRouteType();
        switch (routeType){
            case RANDOM_ROUTE:{
                idx = ThreadLocalRandom.current().nextInt(routeList.size());
                break;
            }
            case CONSISTENT_HASH_ROUTE:{
                idx = Hashing.consistentHash(context.getRouteBasis(), routeList.size());
                break;
            }
            default:{
                // to use the first one.
                break;
            }
        }
        RouterInfo routerInfo = routeList.get(idx);

        context.setRouterInfo(routerInfo);

        logger.info("caseid {} get route info {}:{}", context.getCaseId(),routerInfo.getHost(),routerInfo.getPort());
        return routerInfo;
    }

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage,OutboundPackage  outboundPackage) throws Exception {
        RouterInfo routerInfo =this.route(context,inboundPackage);
        if(routerInfo ==null){
            throw new  NoRouteInfoException();
        }
        inboundPackage.setRouterInfo(routerInfo);
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage,OutboundPackage outboundPackage) throws Exception {

    }
}
