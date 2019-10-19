package com.webank.ai.fate.serving;


import com.webank.ai.fate.register.loadbalance.LoadBalancer;
import com.webank.ai.fate.register.loadbalance.RandomLoadBalance;
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

@Configuration
@ComponentScan(basePackages = {"com.webank.ai.fate.serving.*"})
@ConfigurationProperties
@PropertySource(value = "file:${user.dir}/conf/serving-server.properties", ignoreResourceNotFound = false)
//@PropertySource(value ={"file:${user.dir}/config/custom.properties","file:${user.dir}/config/custom_prison.properties"}, ignoreResourceNotFound = true)
@SpringBootApplication
@Service
public class SpringConfig {

    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;
    @Autowired
    LoadBalancer loadBalancer;
    @Autowired
    RouterService routerService;

    @Bean
    ZookeeperRegistry getServiceRegistry() {
        // String project, String environment, int port
        String useRegisterString = com.webank.ai.fate.core.utils.Configuration.getProperty("useRegister");
        if (Boolean.valueOf(useRegisterString))
            return ZookeeperRegistry.getRegistery(com.webank.ai.fate.core.utils.Configuration.getProperty("zk.url"), "serving",
                    "online", com.webank.ai.fate.core.utils.Configuration.getPropertyInt(Dict.PORT));
        else
            return null;

    }


    @Bean
    LoadBalancer getLoadBalancer() {
        LoadBalancer loadBalancer = new RandomLoadBalance();
        return loadBalancer;
    }


    @Bean
    RouterService getRouterService() {

        DefaultRouterService routerService = new DefaultRouterService();

        routerService.setRegistry(zookeeperRegistry);

        routerService.setLoadBalancer(this.loadBalancer);

        return routerService;

    }


//    @Override
//    public void afterPropertiesSet() throws Exception {
//        //List<URL> lists = zookeeperRegistry.lookup(URL.valueOf("proxy/online/unaryCall"));
//        System.err.println( routerService.router(URL.valueOf("proxy/online/unaryCall"), LoadBalanceModel.random_with_weight));
//
//    }


}
