package com.webank.ai.fate.serving.proxy.rpc.core;

/**
 * @Description 拦截器链
 * @Author
 **/
public interface InterceptorChain<req,resp> extends Interceptor<req,resp> {

    public void addInterceptor(Interceptor<req,resp> interceptor);


}
