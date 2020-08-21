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

import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.NoRouteInfoException;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class DefaultServingRouter implements Interceptor {
    Logger logger = LoggerFactory.getLogger(DefaultServingRouter.class);

    @Autowired(required = false)
    private ZkServingRouter zkServingRouter;

    @Autowired
    private ConfigFileBasedServingRouter configFileBasedServingRouter;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        RouterInfo routerInfo = null;
        if (zkServingRouter != null) {
            routerInfo = zkServingRouter.route(context, inboundPackage);
        }
        if (null == routerInfo) {
            routerInfo = configFileBasedServingRouter.route(context, inboundPackage);
        }
        if (null == routerInfo) {
            throw new NoRouteInfoException(StatusCode.PROXY_ROUTER_ERROR,"serving-proxy can not find router info ");
        }
        inboundPackage.setRouterInfo(routerInfo);
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
    }
}
