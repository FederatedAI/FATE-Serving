package com.webank.ai.fate.serving.proxy.common;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.bean.RouterTableResponseRecord;
import com.webank.ai.fate.serving.core.exceptions.RouterInfoOperateException;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import com.webank.ai.fate.serving.proxy.rpc.grpc.RouterTableServiceProto;
import com.webank.ai.fate.serving.proxy.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @auther Xiongli
 * @date 2021/6/23
 * @remark
 */
public class RouterTableUtils {

    private static final Logger logger = LoggerFactory.getLogger(RouterTableUtils.class);
    private static final String USER_DIR = System.getProperty(Dict.PROPERTY_USER_DIR);
    private static final String FILE_SEPARATOR = System.getProperty(Dict.PROPERTY_FILE_SEPARATOR);
    private static final String DEFAULT_ROUTER_FILE = "conf" + System.getProperty(Dict.PROPERTY_FILE_SEPARATOR) + "route_table.json";
    private final static String DEFAULT_ROUTER_TABLE = "{\"route_table\":{\"default\":{\"default\":[{\"ip\":\"127.0.0.1\",\"port\":9999,\"useSSL\":false}]}},\"permission\":{\"default_allow\":true}}";
    private final static String BASE_ROUTER_TABLE = "{\"route_table\":{},\"permission\":{\"default_allow\":true}}";
    static String router_table;

    public  static  String getRouterFile(){

        String filePath;
        if (StringUtils.isNotEmpty(MetaInfo.PROPERTY_ROUTE_TABLE)) {
            filePath = MetaInfo.PROPERTY_ROUTE_TABLE;
        } else {
            filePath = USER_DIR + FILE_SEPARATOR + DEFAULT_ROUTER_FILE;
        }
        return filePath;
    }

    public static JsonObject loadRoutTable() {
        String filePath = getRouterFile();

        if (logger.isDebugEnabled()) {
            logger.debug("start load route table...,try to load {}", filePath);
        }

        File file = new File(filePath);
        if (!file.exists()) {
            logger.error("router table {} is not exist", filePath);
            return null;
        }
        JsonReader routerReader = null;
        JsonObject confJson;
        try {
            routerReader = new JsonReader(new FileReader(filePath));
            confJson = JsonParser.parseReader(routerReader).getAsJsonObject();
            if (confJson == null || confJson.size() == 0) {
                confJson = initRouter();
            } else {
                router_table = confJson.toString();
            }
            logger.info("load router table {} {}", filePath,confJson);
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

//    public static void addRouter(List<RouterTableServiceProto.RouterTableInfo> routerInfoList) {
//        try {
//            long now = new Date().getTime();
//            JsonObject routerJson = loadRoutTable();
//            if (routerJson == null) {
//                throw new RouterInfoOperateException("router_table.json not exists");
//            }
//            JsonObject route_table = routerJson.getAsJsonObject("route_table");
//            if (route_table == null) {
//                throw new RouterInfoOperateException("missing routing configuration");
//            }
//            for (RouterTableServiceProto.RouterTableInfo routerInfo : routerInfoList) {
//                JsonObject partyIdRouter = route_table.getAsJsonObject(routerInfo.getPartyId());
//                if (partyIdRouter == null) {
//                    partyIdRouter = new JsonObject();
//                    partyIdRouter.addProperty("createTime", now);
//                    partyIdRouter.addProperty("updateTime", now);
//                    route_table.add(routerInfo.getPartyId(), partyIdRouter);
//                }
//                JsonArray serverTypeArray = partyIdRouter.getAsJsonArray(routerInfo.getServerType());
//                if (serverTypeArray == null) {
//                    serverTypeArray = new JsonArray();
//                    partyIdRouter.add(routerInfo.getServerType(), serverTypeArray);
//                }
//                if (getIndex(serverTypeArray, routerInfo.getHost(), routerInfo.getPort()) != -1) {
//                    throw new RouterInfoOperateException("partyId : " + routerInfo.getPartyId() + ", serverType : " + routerInfo.getServerType()
//                            + ", Network Access : " + routerInfo.getHost() + ":" + routerInfo.getPort() + " is already exists");
//                }
//                serverTypeArray.add(parseRouterInfo(routerInfo));
//                partyIdRouter.add(routerInfo.getServerType(), serverTypeArray);
//            }
//            routerJson.add("route_table", route_table);
//            if (writeRouterFile(JsonUtil.formatJson(routerJson.toString()))) {
//                logger.error("write router_table.json error");
//                throw new RouterInfoOperateException("write router_table.json error");
//            }
//        } catch (RouterInfoOperateException routerEx) {
//            throw new RouterInfoOperateException(routerEx.getMessage());
//        } catch (Exception e) {
//            logger.error("parse router table error", e);
//            throw new RouterInfoOperateException("parse router table error");
//        }
//    }

//    public static void updateRouter(List<RouterTableServiceProto.RouterTableInfo> routerInfoList) {
//        try {
//            JsonObject routerJson = loadRoutTable();
//            if (routerJson == null) {
//                throw new RouterInfoOperateException("router_table.json not exists");
//            }
//            JsonObject route_table = routerJson.getAsJsonObject("route_table");
//            if (route_table == null) {
//                throw new RouterInfoOperateException("missing routing configuration");
//            }
//            for (RouterTableServiceProto.RouterTableInfo routerInfo : routerInfoList) {
//                JsonObject partyIdRouter = route_table.getAsJsonObject(routerInfo.getPartyId());
//                if (partyIdRouter == null) {
//                    throw new RouterInfoOperateException("there is no configuration with partyId = " + routerInfo.getPartyId());
//                }
//                partyIdRouter.addProperty("updateTime", new Date().getTime());
//                JsonArray serverTypeArray = partyIdRouter.getAsJsonArray(routerInfo.getServerType());
//                if (serverTypeArray == null)
//                    throw new RouterInfoOperateException("there is no configuration with partyId = " + routerInfo.getPartyId() + " and serverType = " + routerInfo.getServerType());
//                int index = getIndex(serverTypeArray, routerInfo.getHost(), routerInfo.getPort());
//                if (index == -1) {
//                    throw new RouterInfoOperateException("partyId : " + routerInfo.getPartyId() + ", serverType : " + routerInfo.getServerType()
//                            + ", Network Access : " + routerInfo.getHost() + ":" + routerInfo.getPort() + " is not exists");
//                }
//                JsonObject singleRoute;
//                singleRoute = parseRouterInfo(routerInfo);
//                serverTypeArray.set(index, singleRoute);
//                partyIdRouter.add(routerInfo.getServerType(), serverTypeArray);
//            }
//            if (writeRouterFile(JsonUtil.formatJson(routerJson.toString()))) {
//                logger.error("write router_table.json error");
//                throw new RouterInfoOperateException("write router_table.json error");
//            }
//        } catch (RouterInfoOperateException routerEx) {
//            throw new RouterInfoOperateException(routerEx.getMessage());
//        } catch (Exception e) {
//            logger.error("parse router table error");
//            throw new RouterInfoOperateException("parse router table error");
//        }
//    }

//    public static void deleteRouter(List<RouterTableServiceProto.RouterTableInfo> routerInfoList) {
//        try {
//            JsonObject routerJson = loadRoutTable();
//            if (routerJson == null) {
//                throw new RouterInfoOperateException("router_table.json not exists");
//            }
//            JsonObject route_table = routerJson.getAsJsonObject("route_table");
//            if (route_table == null) {
//                throw new RouterInfoOperateException("missing routing configuration");
//            }
//            for (RouterTableServiceProto.RouterTableInfo routerInfo : routerInfoList) {
//                JsonObject partyIdRouter = route_table.getAsJsonObject(routerInfo.getPartyId());
//                if (StringUtils.isBlank(routerInfo.getServerType())) {
//                    route_table.remove(routerInfo.getPartyId());
//                    if (writeRouterFile(JsonUtil.formatJson(routerJson.toString()))) {
//                        logger.error("write router_table.json error");
//                        throw new RouterInfoOperateException("write router_table.json error");
//                    }
//                    return;
//                }
//                if (partyIdRouter == null) {
//                    throw new RouterInfoOperateException("there is no configuration with partyId = " + routerInfo.getPartyId());
//                }
//                partyIdRouter.addProperty("updateTime", new Date().getTime());
//                JsonArray serverTypeArray = partyIdRouter.getAsJsonArray(routerInfo.getServerType());
//                if (serverTypeArray == null) {
//                    throw new RouterInfoOperateException("there is no configuration with partyId = " + routerInfo.getPartyId() + " and serverType = " + routerInfo.getServerType());
//                }
//                int index = getIndex(serverTypeArray, routerInfo.getHost(), routerInfo.getPort());
//                if (index == -1) {
//                    throw new RouterInfoOperateException("partyId : " + routerInfo.getPartyId() + ", serverType : " + routerInfo.getServerType()
//                            + ", Network Access : " + routerInfo.getHost() + ":" + routerInfo.getPort() + " is not exists");
//                }
//                serverTypeArray.remove(index);
//                partyIdRouter.add(routerInfo.getServerType(), serverTypeArray);
//            }
//            routerJson.add("route_table", route_table);
//            if (writeRouterFile(JsonUtil.formatJson(routerJson.toString()))) {
//                logger.error("write router_table.json error");
//                throw new RouterInfoOperateException("write router_table.json error");
//            }
//        } catch (RouterInfoOperateException routerEx) {
//            throw new RouterInfoOperateException(routerEx.getMessage());
//        } catch (Exception e) {
//            logger.error("parse router table error");
//            throw new RouterInfoOperateException("parse router table error");
//        }
//    }

    public static void saveRouter(String routerInfo) {
        try {
            if (writeRouterFile(JsonUtil.formatJson(routerInfo))) {
                logger.error("write router_table.json error");
                throw new RouterInfoOperateException("write router_table.json error");
            }
        } catch (RouterInfoOperateException routerEx) {
            throw new RouterInfoOperateException(routerEx.getMessage());
        } catch (Exception e) {
            logger.error("parse router table error", e);
            throw new RouterInfoOperateException("parse router table error");
        }
    }

//    private static JsonObject parseRouterInfo(RouterTableServiceProto.RouterTableInfo routerInfo) {
//        if (routerInfo == null) {
//            return null;
//        }
//        JsonObject result = new JsonObject();
//        result.addProperty("ip", routerInfo.getHost());
//        result.addProperty("port", routerInfo.getPort());
//        result.addProperty("useSSL", routerInfo.getUseSSL());
//        if (StringUtils.isNotBlank(routerInfo.getNegotiationType())) {
//            result.addProperty("negotiationType", routerInfo.getNegotiationType());
//        }
//        if (StringUtils.isNotBlank(routerInfo.getCertChainFile())) {
//            result.addProperty("certChainFile", routerInfo.getCertChainFile());
//        }
//        if (StringUtils.isNotBlank(routerInfo.getPrivateKeyFile())) {
//            result.addProperty("privateKeyFile", routerInfo.getPrivateKeyFile());
//        }
//        if (StringUtils.isNotBlank(routerInfo.getCaFile())) {
//            result.addProperty("caFile", routerInfo.getCaFile());
//        }
//        return result;
//    }

    public static boolean writeRouterFile(String context) {
        String filePath = getRouterFile();
        logger.info("write router table file {} {}",filePath,context);
        return !FileUtils.writeFile(context, new File(filePath));
    }

    public static List<RouterTableResponseRecord> parseJson2RouterInfoList(JsonObject routerTableJson) {
        List<RouterTableResponseRecord> routerTableInfoList = new ArrayList<>();
        if (routerTableJson == null) {
            return routerTableInfoList;
        }
        for (Map.Entry<String, JsonElement> tableEntry : routerTableJson.entrySet()) {
            JsonObject routerInfos;
            routerInfos = tableEntry.getValue().getAsJsonObject();
            if (routerInfos == null) {
                continue;
            }
            RouterTableResponseRecord responseRecord = new RouterTableResponseRecord();
            responseRecord.setPartyId(tableEntry.getKey());
            List<RouterTableResponseRecord.RouterTable> routerList = new ArrayList<>();
            for (Map.Entry<String, JsonElement> routerInfosEntry : routerInfos.entrySet()) {
                String serverType = routerInfosEntry.getKey();
                switch (serverType) {
                    case "createTime":
                        responseRecord.setCreateTime(routerInfosEntry.getValue().getAsLong());
                        break;
                    case "updateTime":
                        responseRecord.setUpdateTime(routerInfosEntry.getValue().getAsLong());
                        break;
                    default:
                        JsonArray routerInfosArr = routerInfosEntry.getValue().getAsJsonArray();
                        if (routerInfosArr == null) {
                            continue;
                        }
                        for (JsonElement jsonElement : routerInfosArr) {
                            JsonObject routerTableSignleJson = jsonElement.getAsJsonObject();
                            routerTableSignleJson.addProperty("serverType", serverType);
                            routerList.add(JsonUtil.json2Object(routerTableSignleJson, RouterTableResponseRecord.RouterTable.class));
                        }
                        break;
                }
            }
            responseRecord.setRouterList(routerList);
            responseRecord.setCount(routerList.size());
            routerTableInfoList.add(responseRecord);
        }
        return routerTableInfoList;
    }

    public static int getIndex(JsonArray sourceArr, String host, int port) {
        int result = -1;
        String new_address = host + ":" + port;
        for (int i = 0; i < sourceArr.size(); i++) {
            JsonElement jsonElement = sourceArr.get(i);
            String old_address = jsonElement.getAsJsonObject().get("ip").getAsString() + ":" + jsonElement.getAsJsonObject().get("port").toString();
            if (new_address.equals(old_address)) {
                result = i;
            }
        }
        return result;
    }


}
