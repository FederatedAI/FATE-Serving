package com.webank.ai.fate.serving.proxy.rpc.services;

import com.google.common.collect.Maps;
import com.webank.ai.fate.api.core.BasicMeta;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.register.utils.StringUtils;
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

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HealthCheckEndPointService implements HealthCheckAware {
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckEndPointService.class);

    private static final String IP = "IP";

    private static final String PORT = "PORT";

    private static final Pattern PATTERN = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+(:\\d{0,5})?");

    private static final String HTTP_LOCALHOST = "http://localhost";

    private static final String HTTPS_LOCALHOST = "https://localhost";

    private static final String HTTP_LOCALHOST_IP = "http://127.0.0.1";

    private static final String HTTPS_LOCALHOST_IP = "https://127.0.0.1";

    @Autowired
    ConfigFileBasedServingRouter configFileBasedServingRouter;

    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;

    private void checkSshCertConfig(HealthCheckResult healthCheckResult) {
        Map<Proxy.Topic, List<RouterInfo>> routerInfoMap = configFileBasedServingRouter.getAllRouterInfoMap();
        if(routerInfoMap != null && routerInfoMap.size() > 0) {
            routerInfoMap.forEach((k,v)-> {
                if(v != null) {
                    String topicName = k.getName().concat("_").concat(k.getRole()).concat("_").concat(k.getPartyId());
                    v.forEach(routerInfo -> {
                        if (routerInfo.isUseSSL()) {
                            String caFilePath = routerInfo.getCaFile();
                            if (StringUtils.isNotEmpty(caFilePath)) {
                                File caFile = new File(caFilePath);
                                if (caFile.exists()) {
                                    healthCheckResult.getRecords().add(
                                            new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(), topicName +
                                                            " check ca file :" + caFilePath + " is found", HealthCheckStatus.ok));
                                } else {
                                    healthCheckResult.getRecords().add(
                                            new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(), topicName +
                                                    " check ca file :" + caFilePath + " is not found", HealthCheckStatus.error));
                                }
                            } else {
                                healthCheckResult.getRecords().add(
                                        new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(), topicName +
                                                " check ca file :" + caFilePath + " is not found", HealthCheckStatus.warn));
                            }

                            String certChainFilePath = routerInfo.getCertChainFile();
                            if (StringUtils.isNotEmpty(certChainFilePath)) {
                                File certChainFile = new File(certChainFilePath);
                                if (certChainFile.exists()) {
                                    healthCheckResult.getRecords().add(
                                            new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), topicName +
                                                    " check cert file :" + certChainFilePath + " is found", HealthCheckStatus.ok));
                                } else {
                                    healthCheckResult.getRecords().add(
                                            new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), topicName +
                                                    " check cert file :" + certChainFilePath + " is not found", HealthCheckStatus.error));
                                }
                            } else {
                                healthCheckResult.getRecords().add(
                                        new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), topicName +
                                                " check cert file :" + certChainFilePath + " is not found", HealthCheckStatus.warn));
                            }

                            String privateKeyFilePath = routerInfo.getPrivateKeyFile();
                            if (StringUtils.isNotEmpty(privateKeyFilePath)) {
                                File privateKeyFile = new File(privateKeyFilePath);
                                if (privateKeyFile.exists()) {
                                    healthCheckResult.getRecords().add(
                                            new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), topicName +
                                                    " check privateKey file :" + privateKeyFilePath + " is found", HealthCheckStatus.ok));
                                } else {
                                    healthCheckResult.getRecords().add(
                                            new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), topicName +
                                                    " check privateKey file :" + privateKeyFilePath + " is found", HealthCheckStatus.error));
                                }
                            } else {
                                healthCheckResult.getRecords().add(
                                        new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), topicName +
                                                " check privateKey file :" + privateKeyFilePath + " is found", HealthCheckStatus.warn));
                            }
                        } else {
                            healthCheckResult.getRecords().add(
                                    new HealthCheckRecord(HealthCheckItemEnum.CHECK_CERT_FILE.getItemName(), topicName +
                                            " No CA authentication configured", HealthCheckStatus.ok));
                        }
                    });
                }
            });
        }
    }


    private void checkZkConfig(HealthCheckResult  healthCheckResult){
        if(zookeeperRegistry == null){
            healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ZOOKEEPER_CONFIG.getItemName(),"zookeeper is not used or config is invalid", HealthCheckStatus.warn));
        } else {
            if(zookeeperRegistry.getZkClient().isConnected()) {
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ZOOKEEPER_CONFIG.getItemName(),"zookeeper can not touched", HealthCheckStatus.error));
            }
        }
    }

    private void checkRouterInfo(HealthCheckResult healthCheckResult){
        if(configFileBasedServingRouter.getRouteTable() == null || configFileBasedServingRouter.getRouteTable().size() == 0) {
            healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_FILE.getItemName(),"check router file : router info is not found", HealthCheckStatus.error));
        } else {
            healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_FILE.getItemName(),"check router file : router info is found", HealthCheckStatus.ok));
        }

        routerInfoCheck(healthCheckResult);
    }

    private void routerInfoCheck(HealthCheckResult healthCheckResult) {
        Map<String, Map<String, List<BasicMeta.Endpoint>>> routerInfoMap = configFileBasedServingRouter.getRouteTable();
        if(routerInfoMap != null && routerInfoMap.size() > 0) {
            routerInfoMap.values().forEach(value-> value.forEach((k, v) -> {
                if(v != null) {
                    v.forEach(endpoint -> {
                        try {
                            if (StringUtils.isNotEmpty(endpoint.getUrl())) {
                                Map<String,String> resultMap = getIpPortFromUrl(endpoint.getUrl());
                                if (!TelnetUtil.tryTelnet(resultMap.get(IP), Integer.parseInt(resultMap.get(PORT)))) {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(),  resultMap.get(IP) + ":" + Integer.valueOf(resultMap.get(PORT)) + ": can not be telneted", HealthCheckStatus.error));
                                } else {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(),  resultMap.get(IP) + ":" + Integer.valueOf(resultMap.get(PORT)) + ": telneted ok", HealthCheckStatus.ok));
                                }
                            } else {
                                if (!TelnetUtil.tryTelnet(endpoint.getIp(), endpoint.getPort())) {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(),  endpoint.getIp() + ":" + endpoint.getPort() + ": can not be telneted", HealthCheckStatus.error));
                                } else {
                                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ROUTER_NET.getItemName(),  endpoint.getIp() + ":" + endpoint.getPort() + ": telneted ok", HealthCheckStatus.ok));
                                }
                            }
                        } catch (Exception e) {
                            logger.error("routerInfoCheck happen exception: {}", e.getMessage());
                            throw e;
                        }
                    });
                } else {
                    logger.warn("routerInfoCheck find {} have no value", k);
                }
            }));
        } else {
            logger.warn("routerInfoCheck find routeTable is null or routeTable content size is 0");
        }
    }

    private static Map<String,String> getIpPortFromUrl(String url){
        Map<String,String> resultMap = Maps.newHashMap();
        if (url.startsWith(HTTP_LOCALHOST)) {
            url = url.replace(HTTP_LOCALHOST, HTTP_LOCALHOST_IP);
        }

        if (url.startsWith(HTTPS_LOCALHOST)) {
            url = url.replace(HTTPS_LOCALHOST, HTTPS_LOCALHOST_IP);
        }

        String host = "";
        Matcher matcher = PATTERN.matcher(url);

        if(matcher.find()) {
            host = matcher.group();
        }

        if(!host.contains(":")) {
            resultMap.put("IP",host);
            resultMap.put("PORT","80");
            return resultMap;
        }

        String[] ipPortArr = host.split(":");
        resultMap.put("IP",ipPortArr[0]);
        resultMap.put("PORT",ipPortArr[1]);
        return resultMap;
    }

    @Override
    public HealthCheckResult check(Context context) {

        if(MetaInfo.PROPERTY_ALLOW_HEALTH_CHECK) {
            HealthCheckItemEnum[] items = HealthCheckItemEnum.values();
            HealthCheckResult healthCheckResult = new HealthCheckResult();
            Arrays.stream(items).filter((item) -> {
                HealthCheckComponent healthCheckComponent = item.getComponent();
                return healthCheckComponent == HealthCheckComponent.ALL || healthCheckComponent == HealthCheckComponent.SERVINGPROXY;
            }).forEach((item) -> {
                        switch (item) {
                            case CHECK_MEMORY_USAGE:
                                HealthCheckUtil.memoryCheck(healthCheckResult);
                                break;
                            case CHECK_CERT_FILE:
                                this.checkSshCertConfig(healthCheckResult);
                                break;
                            case CHECK_ZOOKEEPER_CONFIG:
                                logger.info("check zk config");
                                break;
                            case CHECK_ROUTER_NET:
                                logger.info("check route net");
                                break;
                            case CHECK_ROUTER_FILE:
                                this.checkRouterInfo(healthCheckResult);
                                break;
                            default:
                                logger.warn("Illegal verification items: {}", item.getItemName());
                                break;
                        }
                    }
            );

            return healthCheckResult;
        } else {
            logger.warn("serving-proxy is not allowed health check");
            return  null;
        }
    }
}
