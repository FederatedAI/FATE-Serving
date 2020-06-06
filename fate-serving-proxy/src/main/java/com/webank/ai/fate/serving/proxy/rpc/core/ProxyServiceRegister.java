package com.webank.ai.fate.serving.proxy.rpc.core;

import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.flow.FlowCounterManager;
import com.webank.ai.fate.serving.core.rpc.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


/**
 * @Description TODO
 * @Author
 **/
@Component
public class ProxyServiceRegister implements ServiceRegister, ApplicationContextAware, ApplicationListener<ApplicationEvent> {

    Logger logger = LoggerFactory.getLogger(ProxyServiceRegister.class);
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
    public void onApplicationEvent(ApplicationEvent applicationEvent) {

        if (applicationEvent instanceof ContextRefreshedEvent) {
            String[] beans = applicationContext.getBeanNamesForType(AbstractServiceAdaptor.class);
            FlowCounterManager flowCounterManager = applicationContext.getBean(FlowCounterManager.class);
            for (String beanName : beans) {
                AbstractServiceAdaptor serviceAdaptor = applicationContext.getBean(beanName, AbstractServiceAdaptor.class);
                serviceAdaptor.setFlowCounterManager(flowCounterManager);
                FateService proxyService = (FateService) serviceAdaptor.getClass().getAnnotation(FateService.class);
                if (proxyService != null) {
                    Method[] methods = serviceAdaptor.getClass().getMethods();
                    for (Method method : methods) {
                        FateServiceMethod fateServiceMethod = method.getAnnotation(FateServiceMethod.class);
                        if (fateServiceMethod != null) {
                            serviceAdaptor.getMethodMap().put(fateServiceMethod.name(), method);
                        }
                    }
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
            logger.info("service register info {}", this.serviceAdaptorMap);
        }


    }
}
