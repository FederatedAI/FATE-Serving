package com.webank.ai.fate.serving.proxy.bootstrap;

import com.webank.ai.fate.serving.proxy.rpc.core.AbstractServiceAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Description TODO
 * @Author
 **/
@SpringBootApplication
@ComponentScan(basePackages = {"com.webank.ai.fate.serving.proxy.*"})
@PropertySource("classpath:application.properties")
@EnableScheduling
public class Bootstrap {


   static  Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) {

        SpringApplication.run(Bootstrap.class, args);


                Runtime.getRuntime().addShutdownHook(new  Thread(new Runnable() {



                    @Override
                    public void run() {

                        logger.info("try to shutdown server ==============!!!!!!!!!!!!!!!!!!!!!");
                        AbstractServiceAdaptor.isOpen=false;
                        int  tryNum= 0;
                        /**
                         * 3秒
                         */
                       while(AbstractServiceAdaptor.requestInHandle.get()>0&&tryNum<30){

                           logger.info("try to shundown,try count {}, remain {}",tryNum,AbstractServiceAdaptor.requestInHandle.get());
                           try {
                               Thread.sleep(100);
                           } catch (InterruptedException e) {
                               e.printStackTrace();
                           }
                       }
                    }
                })


        );
    }

}