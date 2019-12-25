package com.webank.ai.fate.serving.proxy.rpc.core;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Description TODO
 * @Author
 **/
public class DefaultInterceptorChain<req,resp> implements InterceptorChain<req,resp> {

    Logger logger = LoggerFactory.getLogger(DefaultInterceptorChain.class);

    List<Interceptor<req,resp>> chain = Lists.newArrayList();

    @Override
    public void addInterceptor(Interceptor<req,resp> interceptor) {

        chain.add(interceptor);
    }

    /**
     * 前处理因为多数是校验逻辑 ， 在这里抛出异常，将中断流程
     * @param context
     * @param inboundPackage
     * @param outboundPackage
     * @throws Exception
     */
    @Override
    public void doPreProcess(Context context, InboundPackage<req> inboundPackage ,OutboundPackage<resp> outboundPackage) throws Exception {

        for(Interceptor<req,resp>  interceptor:chain){
            interceptor.doPreProcess(context,inboundPackage,outboundPackage);
        }

    }

    /**
     * 后处理即使抛出异常，也将执行完所有
     * @param context
     * @param inboundPackage
     * @param outboundPackage
     * @throws Exception
     */
    @Override
    public void doPostProcess(Context context, InboundPackage<req> inboundPackage,OutboundPackage<resp> outboundPackage) throws Exception {

        for(Interceptor<req,resp>  interceptor:chain){
            try {
                interceptor.doPostProcess(context, inboundPackage, outboundPackage);
            }catch(Throwable  e){
                logger.error("doPostProcess error",e);
            }
        }
    }
}
