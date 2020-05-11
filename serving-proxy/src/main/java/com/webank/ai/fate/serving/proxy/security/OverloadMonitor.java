package com.webank.ai.fate.serving.proxy.security;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Author
 **/
@Service
public class OverloadMonitor implements Interceptor {
    Logger logger = LoggerFactory.getLogger(OverloadMonitor.class);

    @Override
    public void doPreProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {
        Entry entry = null;
        try {
            entry = SphU.entry(context.getServiceName());
        } catch (BlockException ex) {
            logger.warn("request was block by overload monitor, serviceName:{}.", context.getServiceName());
            throw ex;
        } finally {
            if (entry != null) {
                entry.exit();
            }
        }
    }

    @Override
    public void doPostProcess(Context context, InboundPackage inboundPackage, OutboundPackage outboundPackage) throws Exception {

    }
}
