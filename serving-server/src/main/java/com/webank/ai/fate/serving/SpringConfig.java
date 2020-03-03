package com.webank.ai.fate.serving;


import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.google.common.base.Preconditions;
import com.webank.ai.fate.register.common.NamedThreadFactory;
import com.webank.ai.fate.register.provider.FateServerBuilder;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.service.InferenceService;
import com.webank.ai.fate.serving.service.ModelService;
import com.webank.ai.fate.serving.service.ProxyService;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration

public class SpringConfig {

//    @Autowired(required = false)
//    ZookeeperRegistry zookeeperRegistry;
//
//    @Autowired
//    RouterService routerService;
    @Autowired
    Environment environment;

    @Value("${port:3000}")
    int port;

    @Bean
    @Conditional({UseZkCondition.class})
    ZookeeperRegistry getServiceRegistry( @Value("${useRegister:false}")  boolean useZk  ) {
        String  zkUrl = environment.getProperty("zk.url");
        Preconditions.checkArgument(StringUtils.isNotEmpty(zkUrl));
        return ZookeeperRegistry.getRegistery(zkUrl, "serving",
                    "online", com.webank.ai.fate.serving.core.bean.Configuration.getPropertyInt(Dict.PORT));
    }

    @Bean
    public MetricRegistry metrics() {
        return new MetricRegistry();
    }

    @Bean
    public Meter requestMeter(MetricRegistry metrics) {
        return metrics.meter("request");
    }

    @Bean
    public Counter pendingJobs(MetricRegistry metrics) {
        return metrics.counter("requestCount");
    }

    @Bean
    public ConsoleReporter consoleReporter(MetricRegistry metrics) {
        return ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public JmxReporter jmxReporter(MetricRegistry metrics) {
        return JmxReporter.forRegistry(metrics).build();
    }

    @Bean
    @Conditional({UseZkCondition.class})
    RouterService getRouterService(  ZookeeperRegistry zookeeperRegistry) {
        DefaultRouterService routerService = new DefaultRouterService();
        routerService.setRegistry(zookeeperRegistry);
        return routerService;
    }



//    public
//
//    int processors = Runtime.getRuntime().availableProcessors();
//
//    Integer corePoolSize = com.webank.ai.fate.serving.core.bean.Configuration.getPropertyInt("serving.core.pool.size", processors);
//    Integer maxPoolSize = com.webank.ai.fate.serving.core.bean.Configuration.getPropertyInt("serving.max.pool.size", processors * 2);
//    Integer aliveTime = com.webank.ai.fate.serving.core.bean.Configuration.getPropertyInt("serving.pool.alive.time", 1000);
//    Integer queueSize = com.webank.ai.fate.serving.core.bean.Configuration.getPropertyInt("serving.pool.queue.size", 10);
//    Executor executor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, aliveTime.longValue(), TimeUnit.MILLISECONDS,
//            new SynchronousQueue(), new NamedThreadFactory("ServingServer", true));
//
//    FateServerBuilder serverBuilder = (FateServerBuilder) ServerBuilder.forPort(port);
//        serverBuilder.keepAliveTime(100,TimeUnit.MILLISECONDS);
//        serverBuilder.executor(executor);
//    //new ServiceOverloadProtectionHandle()
//        serverBuilder.addService(ServerInterceptors.intercept(applicationContext.getBean(InferenceService.class), new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), InferenceService.class);
//        serverBuilder.addService(ServerInterceptors.intercept(applicationContext.getBean(ModelService.class), new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), ModelService.class);
//        serverBuilder.addService(ServerInterceptors.intercept(applicationContext.getBean(ProxyService.class), new ServiceExceptionHandler(), new ServiceOverloadProtectionHandle()), ProxyService.class);
//    server = serverBuilder.build();

}
