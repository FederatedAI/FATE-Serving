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

package com.webank.ai.fate.serving.common.interceptors;

import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.NoRouteInfoException;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FederationRouterInterceptor extends AbstractInterceptor {

    @Autowired(required = false)
    RouterService routerService;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        RouterInfo routerInfo = new RouterInfo();
        String address = null;
        if (routerService == null) {
            address = environment.getProperty(Dict.PROPERTY_PROXY_ADDRESS);
            if (!checkAddress(address)) {
                throw new NoRouteInfoException(StatusCode.GUEST_ROUTER_ERROR, "address is error in config file");
            }
            String[] args = address.split(":");
            routerInfo.setHost(args[0]);
            routerInfo.setPort(new Integer(args[1]));
        } else {
            List<URL> urls = routerService.router(Dict.PROPERTY_PROXY_ADDRESS, Dict.ONLINE_ENVIRONMENT, Dict.UNARYCALL);
            if (urls != null && urls.size() > 0) {
                URL url = urls.get(0);
                String ip = url.getHost();
                int port = url.getPort();
                routerInfo.setHost(ip);
                routerInfo.setPort(port);
            } else {
                throw new NoRouteInfoException(StatusCode.GUEST_ROUTER_ERROR, "address is error");
            }
        }
        context.setRouterInfo(routerInfo);
    }
}
