package com.webank.ai.fate.serving.proxy.rpc.core;

/**
 * @Description TODO
 * @Author
 **/

public interface ServiceRegister {

    public ServiceAdaptor getServiceAdaptor(String name);

}
