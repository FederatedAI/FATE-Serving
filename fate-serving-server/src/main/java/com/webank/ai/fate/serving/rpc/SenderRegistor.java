package com.webank.ai.fate.serving.rpc;

import com.webank.ai.fate.serving.common.rpc.core.AbstractServiceAdaptor;
import com.webank.ai.fate.serving.common.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.sink.Protocol;
import com.webank.ai.fate.serving.core.rpc.sink.Sender;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SenderRegistor implements ApplicationContextAware, ApplicationListener<ApplicationReadyEvent> {

    private   Map<String,Sender> sinkMap = new HashMap<String,Sender>();

    public Sender  getSender(String protocol){
        return  sinkMap.get(protocol);
    }

    public  Sender getDefaultSender(){
        return  sinkMap.get("grpc");
    }
    ApplicationContext   applicationContext ;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        String[] beanNames = applicationContext.getBeanNamesForType(Sender.class);
        if(beanNames!=null&&beanNames.length>0){
            for(String beanName :beanNames){
                Object  senderObject = applicationContext.getBean(beanName);
                Protocol  protocol = senderObject.getClass().getAnnotation(Protocol.class);
                sinkMap.put(protocol.name(),(Sender)senderObject);
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext =  applicationContext;
    }
}
