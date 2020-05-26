package com.webank.ai.fate.serving;

import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.rpc.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@Component
public class FateServiceRegister implements ServiceRegister, ApplicationContextAware, ApplicationListener<ApplicationReadyEvent> {

    Logger logger = LoggerFactory.getLogger(FateServiceRegister.class);
    Map<String, ServiceAdaptor> serviceAdaptorMap = new HashMap<String, ServiceAdaptor>();
    ApplicationContext applicationContext;
    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();

    @Override
    public ServiceAdaptor getServiceAdaptor(String name) {
        if (serviceAdaptorMap.get(name) != null) {
            return serviceAdaptorMap.get(name);
        } else {
            return serviceAdaptorMap.get("NotFound");
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {

        this.applicationContext = context;

    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationEvent) {


        String[] beans = applicationContext.getBeanNamesForType(AbstractServiceAdaptor.class);
        for (String beanName : beans) {
            AbstractServiceAdaptor serviceAdaptor = applicationContext.getBean(beanName, AbstractServiceAdaptor.class);

            FateService proxyService = serviceAdaptor.getClass().getAnnotation(FateService.class);
            Method[] methods = serviceAdaptor.getClass().getMethods();
            for (Method method : methods) {
                FateServiceMethod fateServiceMethod = method.getAnnotation(FateServiceMethod.class);
                if (fateServiceMethod != null) {
                    serviceAdaptor.getMethodMap().put(fateServiceMethod.name(), method);
                }
            }
            if (proxyService != null) {

                serviceAdaptor.setServiceName(proxyService.name());
                // TODO utu: may load from cfg file is a better choice?
                String[] postChain = proxyService.postChain();
                String[] preChain = proxyService.preChain();
                for (String post : postChain) {
                    Interceptor postInterceptor = applicationContext.getBean(post, Interceptor.class);
                    serviceAdaptor.addPostProcessor(postInterceptor);
                }
                for (String pre : preChain) {
                    Interceptor preInterceptor = applicationContext.getBean(pre, Interceptor.class);
                    serviceAdaptor.addPreProcessor(preInterceptor);
                }

                this.serviceAdaptorMap.put(proxyService.name(), serviceAdaptor);
            }


        }
        logger.info("service register info {}", this.serviceAdaptorMap.keySet());
    }


}
