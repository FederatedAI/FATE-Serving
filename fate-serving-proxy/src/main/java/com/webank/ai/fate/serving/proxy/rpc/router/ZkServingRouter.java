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

package com.webank.ai.fate.serving.proxy.rpc.router;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.core.rpc.router.RouteType;
import com.webank.ai.fate.serving.core.rpc.router.RouteTypeConvertor;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.proxy.utils.FederatedModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ZkServingRouter extends BaseServingRouter implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ZkServingRouter.class);
    private RouteType routeType;
    @Autowired(required = false)
    private RouterService zkRouterService;

    @Override
    public RouteType getRouteType() {
        return routeType;
    }

    @Override
    public List<RouterInfo> getRouterInfoList(Context context, InboundPackage inboundPackage) {
        if (!MetaInfo.PROPERTY_USE_ZK_ROUTER) {
            return null;
        }
        String environment = getEnvironment(context, inboundPackage);
        if (environment == null) {
            return null;
        }
        List<URL> list = zkRouterService.router(Dict.SERVICE_SERVING, environment, context.getServiceName());

        logger.info("try to find zk ,{}:{}:{}, result {}", "serving", environment, context.getServiceName(), list);

        if (null == list || list.isEmpty()) {
            return null;
        }
        List<RouterInfo> routeList = new ArrayList<>();
        for (URL url : list) {
            String urlip = url.getHost();
            int port = url.getPort();
            RouterInfo router = new RouterInfo();
            router.setHost(urlip);
            router.setPort(port);
            routeList.add(router);
        }
        return routeList;
    }

    // TODO utu: sucks! have to reconstruct the entire protocol of online serving
    private String getEnvironment(Context context, InboundPackage inboundPackage) {
        if (Dict.SERVICENAME_INFERENCE.equals(context.getServiceName()) || Dict.SERVICENAME_BATCH_INFERENCE.equals(context.getServiceName())) {
            // guest, proxy -> serving
            return (String) inboundPackage.getHead().get(Dict.SERVICE_ID);
        }

        if (Dict.UNARYCALL.equals(context.getServiceName()) && context.getGrpcType() == GrpcType.INTER_GRPC) {
            // host, proxy -> serving
            Proxy.Packet sourcePacket = (Proxy.Packet) inboundPackage.getBody();
            return FederatedModelUtils.getModelRouteKey(sourcePacket);
        }

        // default unaryCall  proxy -> proxy
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        routeType = RouteTypeConvertor.string2RouteType(MetaInfo.PROPERTY_ROUTE_TYPE);
    }
}
