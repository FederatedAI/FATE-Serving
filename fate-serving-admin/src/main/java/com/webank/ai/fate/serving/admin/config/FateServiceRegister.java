//package com.webank.ai.fate.serving.admin.config;
//
//import com.webank.ai.fate.serving.core.rpc.core.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.context.ApplicationListener;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//public class FateServiceRegister implements ServiceRegister, ApplicationContextAware, ApplicationListener<ApplicationReadyEvent> {
//
//    Logger logger = LoggerFactory.getLogger(FateServiceRegister.class);
//    Map<String, ServiceAdaptor> serviceAdaptorMap = new HashMap<String, ServiceAdaptor>();
//    ApplicationContext applicationContext;
//
//    @Override
//    public ServiceAdaptor getServiceAdaptor(String name) {
//        return this.serviceAdaptorMap.get(name);
//    }
//
//    @Override
//    public void setApplicationContext(ApplicationContext context) throws BeansException {
//        this.applicationContext = context;
//    }
//
////    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
//
//
//    @Override
//    public void onApplicationEvent(ApplicationReadyEvent applicationEvent) {
//        String[] beans = applicationContext.getBeanNamesForType(AbstractServiceAdaptor.class);
//        for (String beanName : beans) {
//            AbstractServiceAdaptor serviceAdaptor = applicationContext.getBean(beanName, AbstractServiceAdaptor.class);
//
//            FateService fateService = serviceAdaptor.getClass().getAnnotation(FateService.class);
//
//            if (fateService != null) {
//                serviceAdaptor.setServiceName(fateService.name());
//
//                String[] preChain = fateService.preChain();
//                for (String pre : preChain) {
//                    Interceptor preInterceptor = applicationContext.getBean(pre, Interceptor.class);
//                    serviceAdaptor.addPreProcessor(preInterceptor);
//                }
//
//                String[] postChain = fateService.postChain();
//                for (String post : postChain) {
//                    Interceptor postInterceptor = applicationContext.getBean(post, Interceptor.class);
//                    serviceAdaptor.addPostProcessor(postInterceptor);
//                }
//
//                this.serviceAdaptorMap.put(fateService.name(), serviceAdaptor);
//            }
//        }
//        logger.info("service register info {}", this.serviceAdaptorMap);
//
//    }
//}
