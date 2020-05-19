//package com.webank.ai.fate.serving.proxy.rpc.core;
//
//import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
//import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
//import org.springframework.stereotype.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicLong;
//
///**
// * @Description 服务过载监控
// * @Author
// **/
//@Endpoint(id = "overload")
//@Component
//public class OverLoadEndPoint {
//
//
//    Map<String, AtomicLong> overLoadInfo = new HashMap();
//
//    public void addServiceOverLoad(String serviceName) {
//
//    }
//
//    @ReadOperation
//    public Map recordOverLoad() {
//        Map map = new HashMap();
//        map.put("name", "ppppp");
//        System.err.println("00000ppppppppppppppppp");
//        return map;
//
//    }
//
//
//}
