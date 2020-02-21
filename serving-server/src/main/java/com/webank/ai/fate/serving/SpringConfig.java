package com.webank.ai.fate.serving;


import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.webank.ai.fate.register.router.DefaultRouterService;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.Dict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Configuration
@ComponentScan(basePackages = {"com.webank.ai.fate.serving.*"})
@ConfigurationProperties
@PropertySource(value = "file:${user.dir}/conf/serving-server.properties", ignoreResourceNotFound = false)
@SpringBootApplication
@Service
public class SpringConfig {

    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;

    @Autowired
    RouterService routerService;

    @Bean
    ZookeeperRegistry getServiceRegistry() {
        String useRegisterString = com.webank.ai.fate.serving
                .core.bean.Configuration.getProperty(Dict.USE_REGISTER,"true");
        if (Boolean.valueOf(useRegisterString)) {
            return ZookeeperRegistry.getRegistery(com.webank.ai.fate.serving.core.bean.Configuration.getProperty("zk.url"), "serving",
                    "online", com.webank.ai.fate.serving.core.bean.Configuration.getPropertyInt(Dict.PORT));
        } else {
            return null;
        }
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
    RouterService getRouterService() {
        DefaultRouterService routerService = new DefaultRouterService();
        routerService.setRegistry(zookeeperRegistry);
        return routerService;
    }

}
