package com.webank.ai.fate.serving.proxy.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.proxy.rpc.grpc.RouterTableServiceProto;
import com.webank.ai.fate.serving.proxy.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @auther Xiongli
 * @date 2021/6/23
 * @remark
 */
public class RouterTableUtils {

    private static final Logger logger = LoggerFactory.getLogger(RouterTableUtils.class);
    private static final String USERDIR = System.getProperty(Dict.PROPERTY_USER_DIR);
    private static final String FILESEPARATOR = System.getProperty(Dict.PROPERTY_FILE_SEPARATOR);
    private static final String DEFAULT_ROUTER_FILE = "conf" + System.getProperty(Dict.PROPERTY_FILE_SEPARATOR) + "route_table.json";
    private final static String DEFAULT_ROUTER_TABLE = "{\"route_table\":{\"default\":{\"default\":[{\"ip\":\"127.0.0.1\",\"port\":9999,\"useSSL\":false}]}},\"permission\":{\"default_allow\":true}}";
    private static String router_table = "";

    public static JsonObject loadRoutTable() {
        String filePath;
        if (StringUtils.isNotEmpty(MetaInfo.PROPERTY_ROUTE_TABLE)) {
            filePath = MetaInfo.PROPERTY_ROUTE_TABLE;
        } else {
            filePath = USERDIR + FILESEPARATOR + DEFAULT_ROUTER_FILE;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("start load route table...,try to load {}", filePath);
        }

        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("router table {} is not exist", filePath);
            return null;
        }
        JsonParser jsonParser = new JsonParser();
        JsonReader routerReader = null;
        JsonObject confJson;
        try {
            routerReader = new JsonReader(new FileReader(filePath));
            confJson = jsonParser.parse(routerReader).getAsJsonObject();
            if (confJson == null || confJson.size() == 0) {
                confJson = initRouter();
            } else {
                router_table = confJson.toString();
            }
            logger.info("load router table {}", confJson);
        } catch (Exception e) {
            logger.error("parse router table error: {}", filePath);
            throw new RuntimeException(e);
        } finally {
            if (routerReader != null) {
                try {
                    routerReader.close();
                } catch (IOException ignore) {

                }
            }
        }
        return confJson;
    }

    public static JsonObject initRouter() {
        router_table = DEFAULT_ROUTER_TABLE;
        return JsonParser.parseString(DEFAULT_ROUTER_TABLE).getAsJsonObject();
    }

    public static String updateRouter(RouterTableServiceProto.RouterTableInfo routerInfo) {
        try {
            JsonObject routerJson = StringUtils.isBlank(router_table) ?
                    loadRoutTable() : JsonParser.parseString(router_table).getAsJsonObject();
            if (routerJson == null) {
                return "router_table.json not exists";
            }
            JsonObject route_table = routerJson.getAsJsonObject("route_table");
            if (route_table == null) {
                return "missing routing configuration";
            }
            if (!route_table.has(routerInfo.getPartyId())) {
                return "partyId (" + routerInfo.getPartyId() + ") is not exists";
            }
            route_table.add(routerInfo.getPartyId(), parseRouterInfo(routerInfo));
            if (writeRouterFile(routerJson.toString())) {
                logger.error("write router_table.json error");
                return "write router_table.json error";
            }
        } catch (Exception e) {
            logger.error("parse router table error");
            return "parse router table error";
        }
        return "";
    }

    public static String addRouter(RouterTableServiceProto.RouterTableInfo routerInfo) {
        try {
            JsonObject routerJson = StringUtils.isBlank(router_table) ?
                    loadRoutTable() : JsonParser.parseString(router_table).getAsJsonObject();
            if (routerJson == null) {
                return "router_table.json not exists";
            }
            JsonObject route_table = routerJson.getAsJsonObject("route_table");
            if (route_table == null) {
                return "missing routing configuration";
            }
            if (route_table.has(routerInfo.getPartyId())) {
                return "partyId (" + routerInfo.getPartyId() + ") is already exists";
            }
            route_table.add(routerInfo.getPartyId(), parseRouterInfo(routerInfo));
            if (writeRouterFile(routerJson.toString())) {
                logger.error("write router_table.json error");
                return "write router_table.json error";
            }
        } catch (Exception e) {
            logger.error("parse router table error");
            return "parse router table error";
        }
        return "";
    }

    public static String deleteRouter(RouterTableServiceProto.RouterTableInfo routerInfo) {
        try {
            JsonObject routerJson = StringUtils.isBlank(router_table) ?
                    loadRoutTable() : JsonParser.parseString(router_table).getAsJsonObject();
            if (routerJson == null) {
                return "router_table.json not exists";
            }
            JsonObject route_table = routerJson.getAsJsonObject("route_table");
            if (route_table == null) {
                return "missing routing configuration";
            }
            if (!route_table.has(routerInfo.getPartyId())) {
                return "partyId (" + routerInfo.getPartyId() + ") is not exists";
            }
            route_table.remove(routerInfo.getPartyId());
            if (writeRouterFile(routerJson.toString())) {
                logger.error("write router_table.json error");
                return "write router_table.json error";
            }
        } catch (Exception e) {
            logger.error("parse router table error");
            return "parse router table error";
        }
        return "";
    }

    private static JsonObject parseRouterInfo(RouterTableServiceProto.RouterTableInfo routerInfo) {
        if (routerInfo == null) {
            return null;
        }
        RouterInfo target = new RouterInfo();
        BeanUtils.copyProperties(routerInfo, target);
        return JsonUtil.object2JsonObject(target);
    }


    public static boolean writeRouterFile(String context) {
        String filePath = USERDIR + FILESEPARATOR + DEFAULT_ROUTER_FILE;
        return FileUtils.writeFile(context, new File(filePath));
    }

    public static List<RouterTableServiceProto.RouterTableInfo> parseJson2RouterInfoList(JsonObject routerTable) {
        List<RouterTableServiceProto.RouterTableInfo> routerTableInfoList = new ArrayList<>();
        if(routerTable == null){
            return routerTableInfoList;
        }
        JsonObject tableJson = null;
        for (Map.Entry<String, JsonElement> tableEntry : routerTable.entrySet()) {
            tableJson = tableEntry.getValue().getAsJsonObject();
            tableJson.addProperty("partyId",tableEntry.getKey());
            routerTableInfoList.add(JsonUtil.JsonObject2Objcet(tableJson,RouterTableServiceProto.RouterTableInfo.class));
        }
        return routerTableInfoList;
    }

}
