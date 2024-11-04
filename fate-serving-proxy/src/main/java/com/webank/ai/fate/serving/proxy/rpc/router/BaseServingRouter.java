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

import com.google.common.hash.Hashing;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.RouterInterface;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.NoRouterInfoException;
import com.webank.ai.fate.serving.core.rpc.router.RouteType;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseServingRouter implements RouterInterface {
    private static final Logger logger = LoggerFactory.getLogger(BaseServingRouter.class);

    private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

    public abstract List<RouterInfo> getRouterInfoList(Context context, InboundPackage inboundPackage);

    public abstract RouteType getRouteType();

    @Override
    public RouterInfo route(Context context, InboundPackage inboundPackage) {
        List<RouterInfo> routeList = getRouterInfoList(context, inboundPackage);

        if (routeList == null || routeList.isEmpty()) {
            return null;
        }

        int idx = 0;
        RouteType routeType = getRouteType();
        switch (routeType) {
            case RANDOM_ROUTE: {
                idx = ThreadLocalRandom.current().nextInt(routeList.size());
                break;
            }
            case CONSISTENT_HASH_ROUTE: {
                idx = Hashing.consistentHash(context.getRouteBasis(), routeList.size());
                break;
            }
            case ROUND_ROBIN: {
                idx = roundRobinCounter.getAndIncrement() % routeList.size();
                break;
            }
            default: {
                // to use the first one.
                break;
            }
        }

        RouterInfo routerInfo = routeList.get(idx);

        context.setRouterInfo(routerInfo);

        logger.info("caseid {} get route info {}:{}", context.getCaseId(), routerInfo.getHost(), routerInfo.getPort());

        return routerInfo;
    }

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {
        RouterInfo routerInfo = this.route(context, inboundPackage);
        if (routerInfo == null) {
            throw new NoRouterInfoException(StatusCode.PROXY_ROUTER_ERROR, "PROXY_ROUTER_ERROR");
        }
        inboundPackage.setRouterInfo(routerInfo);
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) {

    }
}
