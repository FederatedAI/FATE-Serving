package com.webank.ai.fate.serving.proxy.rpc.router;

import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.common.rpc.core.Interceptor;
import com.webank.ai.fate.serving.common.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
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

    @Autowired
    private ZkServingRouter zkServingRouter;

    @Autowired
    private ConfigFileBasedServingRouter configFileBasedServingRouter;

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        RouterInfo routerInfo = zkServingRouter.route(context, inboundPackage);
        if (null == routerInfo) {
            routerInfo = configFileBasedServingRouter.route(context, inboundPackage);
        }
        if (null == routerInfo) {
            throw new NoRouteInfoException();
        }
        inboundPackage.setRouterInfo(routerInfo);
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
    }
}
