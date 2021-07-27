package com.webank.ai.fate.serving.admin.services;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.common.health.HealthCheckRecord;
import com.webank.ai.fate.serving.common.health.HealthCheckResult;
import com.webank.ai.fate.serving.common.health.HealthCheckStatus;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.exceptions.RemoteRpcException;
import com.webank.ai.fate.serving.core.exceptions.SysException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.core.utils.NetUtils;
import com.webank.ai.fate.serving.core.utils.ThreadPoolUtil;
import io.grpc.Grpc;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.*;

@Service
public class HealthCheckService implements InitializingBean {

    Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    @Autowired
    ComponentService  componentService;

    GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();
    Map<String,Object> healthRecord = new ConcurrentHashMap<>();

    private static ThreadPoolExecutor executor = ThreadPoolUtil.newThreadPoolExecutor();

    public    Map  getHealthCheckInfo(){
        return   healthRecord;

    }

    private void  checkRemoteHealth(Map<String,Map> componentMap, String address, String component) {

        Map<String,Map> currentComponentMap = componentMap.get(component);
        String host = address.substring(0,address.indexOf(":"));
        int port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = getMonitorServiceBlockStub(host, port);
        CommonServiceProto.HealthCheckRequest.Builder builder = CommonServiceProto.HealthCheckRequest.newBuilder();
        CommonServiceProto.CommonResponse commonResponse = blockingStub.checkHealthService(builder.build());
        HealthCheckResult  healthCheckResult = JsonUtil.json2Object(commonResponse.getData().toStringUtf8(), HealthCheckResult.class);
        //currentList.add(healthInfo);
        logger.info("componentMap {}",componentMap);
        Map result = new HashMap();
        List<HealthCheckRecord> okList = Lists.newArrayList();
        List<HealthCheckRecord> warnList = Lists.newArrayList();
        List<HealthCheckRecord> errorList = Lists.newArrayList();
        if(healthCheckResult!=null){
            List<HealthCheckRecord> records = healthCheckResult.getRecords();
            for (HealthCheckRecord record : records) {
                if(record.getHealthCheckStatus().equals(HealthCheckStatus.ok)){
                    okList.add(record);
                }else if(record.getHealthCheckStatus().equals(HealthCheckStatus.warn)){
                    warnList.add(record);
                }else{
                    errorList.add(record);
                }

            }

        }
        result.put("okList",okList);
        result.put("warnList",warnList);
        result.put("errorList",errorList);
        currentComponentMap.put(address,result);
    }

    private  void scheduledCheck()   {
        try {
            logger.info("schedule check begin");
//        if (this.healthRecord != null && System.currentTimeMillis() - Long.valueOf(this.healthRecord.get("timeStamp").toString()) < 150000) {
//            return;
//        }
            Map<String, Object> newHealthRecord = new ConcurrentHashMap<>();
            Map<String, Map> componentHearthMap = new ConcurrentHashMap<>();
            Map<String, List<String>> addressMap = componentService.getComponentAddresses();
            //componentService.pullService();
            List<ComponentService.ServiceInfo> serviceInfos = componentService.getServiceInfos();
            componentHearthMap.put("proxy", new ConcurrentHashMap());
            componentHearthMap.put("serving", new ConcurrentHashMap());
            final CountDownLatch countDownLatch = new CountDownLatch(addressMap.size());
            for (String component : addressMap.keySet()) {
                if (component.equals("admin")) {
                    countDownLatch.countDown();
                    continue;
                }
                for (String address : addressMap.get(component)) {
                    executor.submit(() -> {
                        try {
                            checkRemoteHealth(componentHearthMap, address, component);
                        } finally {
                            countDownLatch.countDown();
                        }
                    });
                }
            }
            newHealthRecord.put(Dict.TIMESTAMP, System.currentTimeMillis());
            newHealthRecord.putAll(componentHearthMap);
            healthRecord = newHealthRecord;
        }catch(Exception  e){
            logger.error("schedule health check error ",e );
        }
    }



    private CommonServiceGrpc.CommonServiceBlockingStub getMonitorServiceBlockStub(String host, int port) {
        Preconditions.checkArgument(StringUtils.isNotBlank(host), "parameter host is blank");
        Preconditions.checkArgument(port != 0, "parameter port was wrong");

        if (!NetUtils.isValidAddress(host + ":" + port)) {
            throw new SysException("invalid address");
        }

        if (!componentService.isAllowAccess(host, port)) {
            throw new RemoteRpcException("no allow access, target: " + host + ":" + port);
        }

        ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(host, port);
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        return blockingStub;
    }
    @Override
    public void afterPropertiesSet() throws Exception {

        ScheduledExecutorService  scheduledExecutorService= Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {

                    scheduledCheck();
            }
        },0, MetaInfo.PROPERTY_ADMIN_HEALTH_CHECK_TIME,TimeUnit.SECONDS);
    }
}
