package com.webank.ai.fate.serving.proxy.rpc.router;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.webank.ai.fate.api.core.BasicMeta;
import com.webank.ai.fate.api.networking.proxy.Proxy;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.router.RouteType;
import com.webank.ai.fate.serving.core.rpc.router.RouteTypeConvertor;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.proxy.common.Dict;
import com.webank.ai.fate.serving.proxy.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public  class ConfigFileBasedServingRouter extends BaseServingRouter implements InitializingBean{
    @Value("${routeType:random}")
    private String routeTypeString;

    private RouteType routeType;

    @Value("${route.table:conf/route_table.json}")
    private String routeTableFile;

    @Value("${coordinator:9999}")
    private String selfCoordinator;

    @Value("${inference.service.name:serving}")
    private String inferenceServiceName;

    private String lastFileMd5;

    private static final Logger logger = LoggerFactory.getLogger(ConfigFileBasedServingRouter.class);

    private Map<Proxy.Topic, Set<Proxy.Topic>> allow;
    private Map<Proxy.Topic, Set<Proxy.Topic>> deny;
    private boolean defaultAllow;
    private Map<String, Map<String, List<BasicMeta.Endpoint>>> routeTable;
    private Map<Proxy.Topic, List<RouterInfo>> topicEndpointMapping;
    private BasicMeta.Endpoint.Builder endpointBuilder;

    private static final String IP = "ip";
    private static final String PORT = "port";
    private static final String HOSTNAME = "hostname";
    private static final String DEFAULT = "default";

    @Override
    public RouteType getRouteType(){
        return routeType;
    }

    @Override
    public List<RouterInfo> getRouterInfoList(Context context, InboundPackage inboundPackage){
        Proxy.Topic dstTopic;
        Proxy.Topic srcTopic;
        if("inference".equals(context.getServiceName())) {
            Proxy.Topic.Builder topicBuilder = Proxy.Topic.newBuilder();
            dstTopic = topicBuilder.setPartyId(selfCoordinator).
                    setRole(inferenceServiceName)
                    .setName(Dict.PARTNER_PARTY_NAME)
                    .build();
            srcTopic = topicBuilder.setPartyId(selfCoordinator).
                    setRole(Dict.SELF_PROJECT_NAME)
                    .setName(Dict.PARTNER_PARTY_NAME)
                    .build();
        } else {   // default unaryCall
            Proxy.Packet  sourcePacket = (Proxy.Packet) inboundPackage.getBody();
            dstTopic = sourcePacket.getHeader().getDst();
            srcTopic = sourcePacket.getHeader().getSrc();
        }

        Preconditions.checkNotNull(dstTopic, "dstTopic cannot be null");

        if(!isAllowed(srcTopic, dstTopic)){
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
        for(BasicMeta.Endpoint epoint: endpoints){
            RouterInfo router =  new RouterInfo();
            // ip is first priority
            if(!epoint.getIp().isEmpty()) {
                router.setHost(epoint.getIp());
            }else{
                router.setHost(epoint.getHostname());
            }
            router.setPort(epoint.getPort());
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
        logger.debug("start refreshed route table...");
        String fileMd5 = FileUtils.fileMd5(routeTableFile);
        if(null != fileMd5 && fileMd5.equals(lastFileMd5)){
            return;
        }
        lastFileMd5 = fileMd5;

        JsonParser jsonParser = new JsonParser();
        JsonReader jsonReader = null;
        JsonObject confJson = null;
        try {
            jsonReader = new JsonReader(new FileReader(routeTableFile));
            confJson = jsonParser.parse(jsonReader).getAsJsonObject();
        } catch (FileNotFoundException e) {
            logger.error("File not found: {}", routeTableFile);
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

        logger.info("refreshed route table at: {}", routeTableFile);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        logger.debug("in ConfigFileBasedServingRouter:afterPropertiesSet");
        routeType = RouteTypeConvertor.string2RouteType(routeTypeString);
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

                    if (endpointJson.has(HOSTNAME)) {
                        String targetHostname = endpointJson.get(HOSTNAME).getAsString();
                        endpointBuilder.setHostname(targetHostname);
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
