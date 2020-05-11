package com.webank.ai.fate.serving.core.rpc.core;

import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @Description TODO
 * @Author
 **/
public class InboundPackage<T> {


    static Logger logger = LoggerFactory.getLogger(InboundPackage.class);
    ManagedChannel managedChannel;
    RouterInfo routerInfo;
    String source;
    Map head;

    /*public HttpServletRequest getHttpServletRequest() {
        return httpServletRequest;
    }

    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    HttpServletRequest   httpServletRequest;*/
    T body;

    public ManagedChannel getManagedChannel() {
        return managedChannel;
    }

    public void setManagedChannel(ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    public RouterInfo getRouterInfo() {
        return routerInfo;
    }

    public void setRouterInfo(RouterInfo routerInfo) {
        this.routerInfo = routerInfo;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map getHead() {
        return head;
    }

    public void setHead(Map head) {
        this.head = head;
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
    }
}
