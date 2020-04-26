package com.webank.ai.fate.serving.admin.rpc.core;

/**
 * @Description TODO
 * @Author
 **/
public interface ServiceRegister {

    ServiceAdaptor getServiceAdaptor(String name);

}
