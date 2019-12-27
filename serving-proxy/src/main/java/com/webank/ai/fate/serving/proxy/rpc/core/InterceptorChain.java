package com.webank.ai.fate.serving.proxy.rpc.core;

/**
 * @Description
 * @Author
 **/
public interface InterceptorChain<req,resp> extends Interceptor<req,resp> {

    public void addInterceptor(Interceptor<req,resp> interceptor);


}
