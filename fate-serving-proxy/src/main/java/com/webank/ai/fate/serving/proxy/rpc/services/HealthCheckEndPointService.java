package com.webank.ai.fate.serving.proxy.rpc.services;

import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.register.zookeeper.ZookeeperClient;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.common.health.*;
import com.webank.ai.fate.serving.common.utils.TelnetUtil;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.proxy.rpc.router.ConfigFileBasedServingRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HealthCheckEndPointService implements HealthCheckAware {
    @Autowired
    ConfigFileBasedServingRouter configFileBasedServingRouter;
    @Autowired(required = false)
    ZookeeperRegistry   zookeeperRegistry;

    Logger logger = LoggerFactory.getLogger(HealthCheckEndPointService.class);

    private  void  checkSshCertConfig(HealthCheckResult  healthCheckResult){
        Map<Proxy.Topic, List<RouterInfo>>    routerInfoMap = configFileBasedServingRouter.getAllRouterInfoMap();
        if(routerInfoMap!=null&&routerInfoMap.size()>0){
            routerInfoMap.forEach((k,v)-> {
                if(v!=null){
                    v.forEach(routerInfo -> {
                        if (routerInfo.isUseSSL()) {
                            String caFilePath = routerInfo.getCaFile();
                            if (StringUtils.isNotEmpty(caFilePath)) {
                                File caFile = new File(caFilePath);
                                if (caFile.exists()) {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(), "check cert file :" + caFilePath + " is found", HealthCheckStatus.ok));
                                } else {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(), "check cert file :" + caFilePath + " is not found", HealthCheckStatus.error));
                                }
                            } else {
                                // healthCheckResult.ge();
                            }

                            String certChainFilePath = routerInfo.getCertChainFile();
                            if (StringUtils.isNotEmpty(certChainFilePath)) {
                                File certChainFile = new File(certChainFilePath);
                                if (certChainFile.exists()) {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), "check cert file :" + certChainFilePath + " is found", HealthCheckStatus.ok));
                                } else {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), "check cert file :" + certChainFilePath + " is not found", HealthCheckStatus.ok));
                                }
                            }

                            String privateKeyFilePath = routerInfo.getPrivateKeyFile();
                            if (StringUtils.isNotEmpty(privateKeyFilePath)) {
                                File privateKeyFile = new File(privateKeyFilePath);
                                if (privateKeyFile.exists()) {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), "check cert file :" + privateKeyFilePath + " is found", HealthCheckStatus.ok));

                                    // healthCheckResult.getOkList().add("check cert file :" + privateKeyFilePath + " is found");
                                } else {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), "check cert file :" + privateKeyFilePath + " is found", HealthCheckStatus.ok));

                                    //  healthCheckResult.getErrorList().add("check cert file :" + privateKeyFilePath + " is not found");
                                }
                            }


                        }
                    });
                }
            });
        }

    }


    private  void  checkZkConfig(HealthCheckResult  healthCheckResult){
        if(zookeeperRegistry==null){
            healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ZOOKEEPER_CONFIG.getItemName(),"zookeeper is not used or config is invalid",HealthCheckStatus.warn));
        }else{
            boolean isConnected  = zookeeperRegistry.getZkClient().isConnected();
            if(isConnected){
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ZOOKEEPER_CONFIG.getItemName(),"zookeeper can not touched",HealthCheckStatus.error));
            }
        }
//        if(!MetaInfo.PROPERTY_USE_ZK_ROUTER.booleanValue()){
//            healthCheckResult.getWarnList().add(MetaInfo.PROPERTY_USE_ZK_ROUTER+":"+MetaInfo.PROPERTY_USE_ZK_ROUTER+"="+MetaInfo.PROPERTY_USE_ZK_ROUTER);
//        }else{
//            if(StringUtils.isNotEmpty(MetaInfo.PROPERTY_ZK_URL)){
//                healthCheckResult.getOkList().add("check zk config "+": ok ");
//            }else {
//                healthCheckResult.getErrorList().add("check zk config "+": no config ");
//            }
//        }
    }

    private  void  checkRouterInfo(HealthCheckResult  healthCheckResult){
        logger.info("check router info ================ ");
//        if(!MetaInfo.PROPERTY_USE_REGISTER.booleanValue()){
//                healthCheckResult.getWarnList().add(MetaInfo.PROPERTY_USE_REGISTER+":"+MetaInfo.PROPERTY_USE_REGISTER+"="+MetaInfo.PROPERTY_USE_REGISTER);
//        }else{
//            if(StringUtils.isNotEmpty(MetaInfo.PROPERTY_ZK_URL)){
//                healthCheckResult.getOkList().add(MetaInfo.PROPERTY_ZK_URL+":"+MetaInfo.PROPERTY_ZK_URL);
//            }else {
//                healthCheckResult.getErrorList().add(MetaInfo.PROPERTY_ZK_URL+":"+MetaInfo.PROPERTY_ZK_URL);
//            }
//        }
        HealthCheckRecord  routerConfigCheck = new  HealthCheckRecord();
        if(configFileBasedServingRouter.getAllRouterInfoMap()==null||configFileBasedServingRouter.getAllRouterInfoMap().size()==0){
            routerConfigCheck.setCheckItemName("");
            healthCheckResult.getRecords().add(new  HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_FILE.getItemName(),"check router file : no router info found",HealthCheckStatus.error));

           // healthCheckResult.getErrorList().add("check router_table.json  "+": no router info found");
        }else{
            healthCheckResult.getRecords().add(new  HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_FILE.getItemName(),"check router file : router info is found",HealthCheckStatus.ok));

          //  healthCheckResult.getOkList().add("check router_table.json  "+": router_table.json is found");
        }


    }
    private  void  routerInfoCheck(HealthCheckResult  healthCheckResult){
        Map<Proxy.Topic, List<RouterInfo>>    routerInfoMap = configFileBasedServingRouter.getAllRouterInfoMap();
        if(routerInfoMap!=null&&routerInfoMap.size()>0){
            routerInfoMap.forEach((k,v)->{
                if(v!=null){
                    v.forEach(routerInfo -> {
                        try {
                            boolean connectAble = TelnetUtil.tryTelnet(routerInfo.getHost(), routerInfo.getPort());


                            if (!connectAble) {

                                healthCheckResult.getRecords().add(new  HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(),  routerInfo.getHost() + " " + routerInfo.getPort() + ": can not be telneted",HealthCheckStatus.warn));
                            //("check router " + routerInfo.getHost() + " " + routerInfo.getPort() + ": can not be telneted");
                            } else {
                                healthCheckResult.getRecords().add(new  HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(),  routerInfo.getHost() + " " + routerInfo.getPort() + ": telneted",HealthCheckStatus.ok
                                ));
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    });
                }
            });
        }

    }


    private  void  metircCheck(HealthCheckResult  healthCheckResult){


    }

    @Override
    public HealthCheckResult check(Context context) {

        if(MetaInfo.PROPERTY_ALLOW_HEALTH_CHECK) {
            HealthCheckItemEnum[] items = HealthCheckItemEnum.values();
            HealthCheckResult  healthCheckResult = new  HealthCheckResult();
            Arrays.stream(items).filter((item) -> {
                HealthCheckComponent healthCheckComponent = item.getComponent();
                return healthCheckComponent == HealthCheckComponent.ALL || healthCheckComponent == HealthCheckComponent.SERVINGPROXY;
            }).forEach((item) -> {
                        switch (item) {
//                CHECK_ROUTER_FILE("check router_table.json exist",HealthCheckComponent.SERVINGPROXY),
//                        CHECK_ZOOKEEPER_CONFIG("check zk config",HealthCheckComponent.ALL),
//                        CHECK_ROUTER_NET("check router",HealthCheckComponent.SERVINGPROXY),
//                        CHECK_MEMORY_USAGE("check memory usage",HealthCheckComponent.ALL),
//                        CHECK_CERT_FILE("check cert file",HealthCheckComponent.SERVINGPROXY);
                            case CHECK_MEMORY_USAGE:
                                HealthCheckUtil.memoryCheck(healthCheckResult);
                                break;
                            case CHECK_CERT_FILE:
                                this.checkSshCertConfig(healthCheckResult);break;

                            case CHECK_ZOOKEEPER_CONFIG:
                                break;
                            case CHECK_ROUTER_FILE:
                                this.checkRouterInfo(healthCheckResult);
                                break;
                            //  case  HealthCheckItemEnum.CHECK_CERT_FILE: break;
                        }
                    }
            );
            return healthCheckResult;
        }else{
            return  null;
        }


    }
}
