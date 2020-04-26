package com.webank.ai.fate.serving.admin.rpc.core;

/**
 * @Description
 * @Author
 **/
public interface InterceptorChain<req, resp> extends Interceptor<req, resp> {

    void addInterceptor(Interceptor<req, resp> interceptor);

}
