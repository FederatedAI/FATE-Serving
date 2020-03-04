package com.webank.ai.fate.serving.interceptor;

import com.webank.ai.fate.serving.core.rpc.core.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @Description TODO
 * @Author kaideng
 **/
public class AbstractInterceptor implements Interceptor,EnvironmentAware{

    protected  Environment  environment;

    Logger logger   = LoggerFactory.getLogger(this.getClass());

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


}
