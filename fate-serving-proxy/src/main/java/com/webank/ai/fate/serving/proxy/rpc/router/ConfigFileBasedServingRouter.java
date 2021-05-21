/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.proxy.rpc.router;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.webank.ai.fate.api.core.BasicMeta;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.common.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.rpc.router.RouteType;
import com.webank.ai.fate.serving.core.rpc.router.RouteTypeConvertor;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.proxy.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConfigFileBasedServingRouter extends BaseServingRouter implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(ConfigFileBasedServingRouter.class);
    private static final String IP = "ip";
    private static final String PORT = "port";
    private static final String USE_SSL = "useSSL";
    private static final String HOSTNAME = "hostname";
    private static final String negotiationType = "negotiationType";
    private static final String certChainFile = "certChainFile";
    private static final String privateKeyFile = "privateKeyFile";
    private static final String caFile = "caFile";
    private static final String DEFAULT = "default";
    private final String DEFAULT_ROUTER_FILE = "conf" + System.getProperty(Dict.PROPERTY_FILE_SEPARATOR) + "route_table.json";
    private final String fileSeparator = System.getProperty(Dict.PROPERTY_FILE_SEPARATOR);
    private RouteType routeType;
    private String userDir = System.getProperty(Dict.PROPERTY_USER_DIR);
    private String lastFileMd5;
    private Map<Proxy.Topic, Set<Proxy.Topic>> allow;
    private Map<Proxy.Topic, Set<Proxy.Topic>> deny;
    private boolean defaultAllow;
    private Map<String, Map<String, List<BasicMeta.Endpoint>>> routeTable;
    private Map<Proxy.Topic, List<RouterInfo>> topicEndpointMapping;
    private BasicMeta.Endpoint.Builder endpointBuilder;

    @Override
    public RouteType getRouteType() {
        return routeType;
    }

    @Override
    public List<RouterInfo> getRouterInfoList(Context context, InboundPackage inboundPackage) {
        Proxy.Topic dstTopic;
        Proxy.Topic srcTopic;
        if (Dict.SERVICENAME_INFERENCE.equals(context.getServiceName()) || Dict.SERVICENAME_BATCH_INFERENCE.equals(context.getServiceName())) {
            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();
            dstTopic = topicBuilder.setPartyId(String.valueOf(MetaInfo.PROPERTY_COORDINATOR)).
                    setRole(MetaInfo.PROPERTY_INFERENCE_SERVICE_NAME)
                    .setName(Dict.PARTNER_PARTY_NAME)
                    .build();
            srcTopic = topicBuilder.setPartyId(String.valueOf(MetaInfo.PROPERTY_COORDINATOR)).
                    setRole(Dict.SELF_PROJECT_NAME)
                    .setName(Dict.PARTNER_PARTY_NAME)
                    .build();
        } else {   // default unaryCall
            Proxy.Packet sourcePacket = (Proxy.Packet) inboundPackage.getBody();
            dstTopic = sourcePacket.getHeader().getDst();
            srcTopic = sourcePacket.getHeader().getSrc();
        }

        Preconditions.checkNotNull(dstTopic, "dstTopic cannot be null");

        if (!isAllowed(srcTopic, dstTopic)) {
            logger.warn("from {} to {} is not allowed!", srcTopic, dstTopic);
            return null;
        }

        List<RouterInfo> routeList = topicEndpointMapping.getOrDefault(dstTopic, null);
        if (routeList != null) {
            return routeList;
        }

        // to get route list from routeTable
        String topicName = dstTopic.getName();
        String coordinator = dstTopic.getPartyId();
        String serviceName = dstTopic.getRole();
        if (StringUtils.isAnyBlank(topicName, coordinator, serviceName)) {
            throw new IllegalArgumentException("one of dstTopic name, coordinator, role is null. dstTopic: " + dstTopic);
        }
        Map<String, List<BasicMeta.Endpoint>> serviceTable =
                routeTable.getOrDefault(coordinator, routeTable.getOrDefault(DEFAULT, null));
        if (serviceTable == null) {
            throw new IllegalStateException("No available endpoint for the coordinator: " + coordinator +
                    ". Considering adding a default endpoint?");
        }
        List<BasicMeta.Endpoint> endpoints =
                serviceTable.getOrDefault(serviceName, serviceTable.getOrDefault(DEFAULT, null));
        if (endpoints == null || endpoints.isEmpty()) {
            throw new IllegalStateException("No available endpoint for this service: " + serviceName +
                    ". Considering adding a default endpoint, or check if the list is empty?");
        }

        routeList = new ArrayList<>();
        for (BasicMeta.Endpoint epoint : endpoints) {
            RouterInfo router = new RouterInfo();
            // ip is first priority
            if (!epoint.getIp().isEmpty()) {
                router.setHost(epoint.getIp());
            } else {
                router.setHost(epoint.getHostname());
            }
            router.setUseSSL(epoint.getUseSSL());
            router.setPort(epoint.getPort());
            router.setNegotiationType(epoint.getNegotiationType());
            router.setCertChainFile(epoint.getCertChainFile());
            router.setPrivateKeyFile(epoint.getPrivateKeyFile());
            router.setCaFile(epoint.getCaFile());
            routeList.add(router);
        }

        topicEndpointMapping.put(dstTopic, routeList);

        return routeList;
    }

    private boolean isAllowed(Proxy.Topic from, Proxy.Topic to) {
        if (hasRule(deny, from, to)) {
            return false;
        } else if (hasRule(allow, from, to)) {
            return true;
        } else {
            return defaultAllow;
        }
    }

    // TODO utu: sucks here, need to be optimized on efficiency
    private boolean hasRule(Map<Proxy.Topic, Set<Proxy.Topic>> target, Proxy.Topic from, Proxy.Topic to) {
        boolean result = false;

        if (target == null || target.isEmpty()) {
            return result;
        }

        Proxy.Topic.Builder fromBuilder = Proxy.Topic.newBuilder();
        Proxy.Topic.Builder toBuilder = Proxy.Topic.newBuilder();

        Proxy.Topic fromValidator = fromBuilder.setPartyId(from.getPartyId()).setRole(from.getRole()).build();
        Proxy.Topic toValidator = toBuilder.setPartyId(to.getPartyId()).setRole(to.getRole()).build();

        Set<Proxy.Topic> rules = null;
        if (target.containsKey(fromValidator)) {
            rules = target.get(fromValidator);
        }

        int stage = 0;
        while (stage < 3 && rules == null) {
            switch (stage) {
                case 0:
                    break;
                case 1:
                    fromValidator = fromBuilder.setRole("*").build();
                    break;
                case 2:
                    fromValidator = fromBuilder.setPartyId("*").build();
                    break;
                default:
                    throw new IllegalStateException("Illegal state when checking from rule");
            }

            if (target.containsKey(fromValidator)) {
                rules = target.get(fromValidator);
            }

            ++stage;
        }

        if (rules == null) {
            return result;
        }

        stage = 0;
        while (stage < 3 && !result) {
            switch (stage) {
                case 0:
                    break;
                case 1:
                    toValidator = toBuilder.setRole("*").build();
                    break;
                case 2:
                    toValidator = toBuilder.setPartyId("*").build();
                    break;
                default:
                    throw new IllegalStateException("Illegal state when checking to rule");
            }

            if (rules.contains(toValidator)) {
                result = true;
            }

            ++stage;
        }

        return result;
    }

    @Scheduled(fixedRate = 10000)
    public void loadRouteTable() {
        String filePath = "";
        if (StringUtils.isNotEmpty(MetaInfo.PROPERTY_ROUTE_TABLE)) {
            filePath = MetaInfo.PROPERTY_ROUTE_TABLE;
        } else {
            filePath = userDir + this.fileSeparator + DEFAULT_ROUTER_FILE;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("start refreshed route table...,try to load {}", filePath);
        }

        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("router table {} is not exist", filePath);
            return;
        }
        String fileMd5 = FileUtils.fileMd5(filePath);
        if (null != fileMd5 && fileMd5.equals(lastFileMd5)) {
            return;
        }
        JsonParser jsonParser = new JsonParser();
        JsonReader jsonReader = null;
        JsonObject confJson = null;
        try {
            jsonReader = new JsonReader(new FileReader(filePath));
            confJson = jsonParser.parse(jsonReader).getAsJsonObject();
            MetaInfo.PROXY_ROUTER_TABLE = confJson.toString();
            logger.info("load router table {}", confJson);

        } catch (Exception e) {
            logger.error("parse router table error: {}", filePath);
            throw new RuntimeException(e);
        } finally {
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (IOException ignore) {

                }
            }
        }
        initRouteTable(confJson.getAsJsonObject("route_table"));
        initPermission(confJson.getAsJsonObject("permission"));
        logger.info("refreshed route table at: {}", filePath);
        lastFileMd5 = fileMd5;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("in ConfigFileBasedServingRouter:afterPropertiesSet");
        }
        routeType = RouteTypeConvertor.string2RouteType(MetaInfo.PROPERTY_ROUTE_TYPE);
        routeTable = new ConcurrentHashMap<>();
        topicEndpointMapping = new WeakHashMap<>();
        endpointBuilder = BasicMeta.Endpoint.newBuilder();

        allow = new ConcurrentHashMap<>();
        deny = new ConcurrentHashMap<>();
        defaultAllow = false;

        lastFileMd5 = "";

        try {
            loadRouteTable();
        } catch (Throwable e) {
            logger.error("load route table fail. ", e);
        }
    }

    private void initRouteTable(JsonObject confJson) {
        Map<String, Map<String, List<BasicMeta.Endpoint>>> newRouteTable = new ConcurrentHashMap<>();

        // loop through coordinator
        for (Map.Entry<String, JsonElement> coordinatorEntry : confJson.entrySet()) {
            String coordinatorKey = coordinatorEntry.getKey();
            JsonObject coordinatorValue = coordinatorEntry.getValue().getAsJsonObject();

            Map<String, List<BasicMeta.Endpoint>> serviceTable = newRouteTable.get(coordinatorKey);
            if (serviceTable == null) {
                serviceTable = new ConcurrentHashMap<>(4);
                newRouteTable.put(coordinatorKey, serviceTable);
            }

            // loop through role in coordinator
            for (Map.Entry<String, JsonElement> roleEntry : coordinatorValue.entrySet()) {
                String roleKey = roleEntry.getKey();
                JsonArray roleValue = roleEntry.getValue().getAsJsonArray();

                List<BasicMeta.Endpoint> endpoints = serviceTable.get(roleKey);
                if (endpoints == null) {
                    endpoints = new ArrayList<>();
                    serviceTable.put(roleKey, endpoints);
                }

                // loop through endpoints
                for (JsonElement endpointElement : roleValue) {
                    endpointBuilder.clear();
                    JsonObject endpointJson = endpointElement.getAsJsonObject();

                    if (endpointJson.has(IP)) {
                        String targetIp = endpointJson.get(IP).getAsString();
                        endpointBuilder.setIp(targetIp);
                    }

                    if (endpointJson.has(PORT)) {
                        int targetPort = endpointJson.get(PORT).getAsInt();
                        endpointBuilder.setPort(targetPort);
                    }

                    if (endpointJson.has(USE_SSL)) {
                        boolean targetUseSSL = endpointJson.get(USE_SSL).getAsBoolean();
                        endpointBuilder.setUseSSL(targetUseSSL);
                    }

                    if (endpointJson.has(HOSTNAME)) {
                        String targetHostname = endpointJson.get(HOSTNAME).getAsString();
                        endpointBuilder.setHostname(targetHostname);
                    }

                    if (endpointJson.has(negotiationType)) {
                        String targetNegotiationType = endpointJson.get(negotiationType).getAsString();
                        endpointBuilder.setNegotiationType(targetNegotiationType);
                    }

                    if (endpointJson.has(certChainFile)) {
                        String targetCertChainFile = endpointJson.get(certChainFile).getAsString();
                        endpointBuilder.setCertChainFile(targetCertChainFile);
                    }

                    if (endpointJson.has(privateKeyFile)) {
                        String targetPrivateKeyFile = endpointJson.get(privateKeyFile).getAsString();
                        endpointBuilder.setPrivateKeyFile(targetPrivateKeyFile);
                    }

                    if (endpointJson.has(caFile)) {
                        String targetCaFile = endpointJson.get(caFile).getAsString();
                        endpointBuilder.setCaFile(targetCaFile);
                    }

                    BasicMeta.Endpoint endpoint = endpointBuilder.build();
                    endpoints.add(endpoint);
                }
            }
        }

        routeTable = newRouteTable;
        topicEndpointMapping.clear();
    }

    private void initPermission(JsonObject confJson) {
        boolean newDefaultAllow = false;
        Map<Proxy.Topic, Set<Proxy.Topic>> newAllow = new ConcurrentHashMap<>();
        Map<Proxy.Topic, Set<Proxy.Topic>> newDeny = new ConcurrentHashMap<>();

        if (confJson.has("default_allow")) {
            newDefaultAllow = confJson.getAsJsonPrimitive("default_allow").getAsBoolean();
        }

        if (confJson.has("allow")) {
            initPermissionType(newAllow, confJson.getAsJsonArray("allow"));
        }

        if (confJson.has("deny")) {
            initPermissionType(newDeny, confJson.getAsJsonArray("deny"));
        }

        defaultAllow = newDefaultAllow;
        allow = newAllow;
        deny = newDeny;
    }

    private void initPermissionType(Map<Proxy.Topic, Set<Proxy.Topic>> target, JsonArray conf) {
        for (JsonElement pairElement : conf) {
            JsonObject pair = pairElement.getAsJsonObject();

            JsonObject from = pair.getAsJsonObject("from");
            Proxy.Topic fromTopic = createTopicFromJson(from);

            JsonObject to = pair.getAsJsonObject("to");
            Proxy.Topic toTopic = createTopicFromJson(to);

            if (!target.containsKey(fromTopic)) {
                target.put(fromTopic, new HashSet<>());
            }
            Set<Proxy.Topic> toTopics = target.get(fromTopic);
            toTopics.add(toTopic);
        }
    }

    private Proxy.Topic createTopicFromJson(JsonObject json) {
        Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();
        if (json.has("coordinator")) {
            topicBuilder.setPartyId(json.get("coordinator").getAsString());
        }

        if (json.has("role")) {
            topicBuilder.setRole(json.get("role").getAsString());
        }

        return topicBuilder.build();
    }
}
