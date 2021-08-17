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
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

    public static synchronized void saveRouter(String routerInfo) {
        try {
            if (!RouteTableJsonValidator.isJSON(routerInfo)) {
                logger.error("validate router_table.json format error");
            }
        } catch (Exception e) {
            throw new RouterInfoOperateException("validate router_table.json format error:" + e.getMessage());
        }
        if (writeRouterFile(JsonUtil.formatJson(routerInfo))) {
                logger.error("write router_table.json error");
                throw new RouterInfoOperateException("write router_table.json error");
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

    public static final class RouteTableJsonValidator {

        /**
         * 数组指针
         */
        private static int index;
        /**
         * 字符串
         */
        private static String value;
        /**
         * 指针当前字符
         */
        private static char curchar;

        /**
         * 工具类非公有构造函数
         */
        private RouteTableJsonValidator() {
        }

        /**
         * @param rawValue 字符串参数
         * @return boolean 是否是JSON
         */
        public static boolean isJSON(String rawValue) throws Exception {
                index = 0;
                value = rawValue;
                switch (nextClean()) {
                    case '[':
                        if (nextClean() == ']') {
                            return true;
                        }
                        back();
                        return validateArray();
                    case '{':
                        if (nextClean() == '}') {
                            return true;
                        }
                        back();
                        return validateObject();
                    default:
                        return false;
                }
        }

        /**
         * @return char 下一个有效实义字符 char<=' ' char!=127
         * @throws JSONException 自定义JSON异常
         */
        public static char nextClean() throws JSONException {
            skipComment:
            do {
                next();
                if (curchar == '/') { // 跳过//类型与/*类型注释 遇回车或者null为注释内容结束
                    switch (next()) {
                        case 47: // '/'
                            do {
                                curchar = next();
                            } while (curchar != '\n' && curchar != '\r' && curchar != 0);
                            continue;
                        case 42: // '*'
                            do {
                                do {
                                    next();
                                    if (curchar == 0) {
                                        throw syntaxError("Unclosed comment");
                                    }
                                } while (curchar != '*');
                                if (next() == '/') {
                                    continue skipComment;
                                }
                                back();
                            } while (true);
                    }
                    back();
                    return '/';
                }
                if (curchar != '#') { //跳过#类型注释 遇回车或者null为注释内容结束
                    break;
                }
                do {
                    next();
                } while (curchar != '\n' && curchar != '\r' && curchar != 0);
            } while (true);
            if (curchar != 0 && (curchar <= ' ' || curchar == 127)) {
                throw syntaxError("JSON can not contain control character!");
            }
            return curchar;
        }

        /**
         * @return char 下一个字符
         */
        public static char next() {
            if (index < 0 || index >= value.length()) {
                return '\0';
            }
            curchar = value.charAt(index);
            if (curchar <= 0) {
                return '\0';
            } else {
                index++;
                return curchar;
            }
        }

        /**
         * 将指针移至上一个字符，回退一位
         */
        public static void back() { //异常在next中进行返回null
            index--;
        }

        /**
         * @param message 异常自定义信息
         * @return JSONException 自定义JSON异常
         */
        public static JSONException syntaxError(String message) {
            return new JSONException((new StringBuilder(String.valueOf(message))).toString());
        }

        /**
         * @return boolean 是否是JSONArray
         * @throws JSONException 自定义JSON异常
         */
        public static boolean validateArray() throws JSONException {
            do {
                //入口为合法 [ array 起点
                nextClean(); //下一位有效字符，跳过注释
                if (curchar == ']') { //空array 直接闭合返回
                    return true;
                } else if (curchar == ',') { //null
                    continue;
                } else if (curchar == '"') { //String
                    validateString();
                } else if (curchar == '-' || (curchar >= 48 && curchar <= 57)) { // number
                    validateNumber();
                } else if (curchar == '{') { // object
                    if (!validateObject()) { //递归校验
                        return false;
                    }
                } else if (curchar == '[') { // array
                    if (!validateArray()) { //递归校验
                        return false;
                    }
                } else if (curchar == 't' || curchar == 'f' || curchar == 'n') { // boolean and JSONNull
                    validateBooleanAndNull();
                } else {
                    return false;
                }
                switch (nextClean()) {
                    case ',':
                        continue;
                    case ']':
                        return true;
                    default:
                        return false;
                }
            } while (true);
        }

        /**
         * @return boolean 是否是JSONObject
         * @throws JSONException 自定义JSON异常
         */
        public static boolean validateObject() throws JSONException {
            do {
                nextClean();
                if (curchar == '}') {
                    return true;
                } else if (curchar == '"') { //String
                    validateString();
                } else {
                    return false;
                }
                if (nextClean() != ':') {
                    return false;
                }
                nextClean();
                if (curchar == ',') { //null
                    throw syntaxError("Missing value");
                } else if (curchar == '"') { //String
                    validateString();
                } else if (curchar == '-' || (curchar >= 48 && curchar <= 57)) { // number
                    validateNumber();
                } else if (curchar == '{') { // object
                    if (!validateObject()) {
                        return false;
                    }
                } else if (curchar == '[') { // array
                    if (!validateArray()) {
                        return false;
                    }
                } else if (curchar == 't' || curchar == 'f' || curchar == 'n') { // boolean and JSONNull
                    validateBooleanAndNull();
                } else {
                    return false;
                }
                switch (nextClean()) {
                    case ',':
                        continue;
                    case '}':
                        return true;
                    default:
                        return false;
                }
            } while (true);
        }

        /**
         * @throws JSONException 自定义JSON异常
         */
        public static void validateString() throws JSONException {
            StringBuilder sb = new StringBuilder();
            do {
                curchar = next(); //JSON对字符串中的转义项有严格规定
                sb.append(curchar);
                if (curchar == '\\') {
                    if ("\"\\/bfnrtu".indexOf(next()) < 0) {
                        throw syntaxError("Invalid escape string");
                    }
                    if (curchar == 'u') { //校验unicode格式 后跟4位16进制 0-9 a-f A-F
                        for (int i = 0; i < 4; i++) {
                            next();
                            if (curchar < 48 || (curchar > 57 && curchar < 65) || (curchar > 70 && curchar < 97)
                                    || curchar > 102) {
                                throw syntaxError("Invalid hexadecimal digits");
                            }
                        }
                    }
                }
            } while (curchar >= ' ' && "\":{[,#/".indexOf(curchar)< 0 && curchar != 127);
            if (curchar == 0) { //仅正常闭合双引号可通过
                throw syntaxError("Unclosed quot");
            } else if (curchar != '"') {
                throw syntaxError("Invalid string {\""+ sb +"}, missing quot ");
            } else if (value.charAt(index)=='"') {
                throw syntaxError("Missing comma after string: \"" + sb);
            } else if (value.charAt(index)==':' ) {
                String str = sb.substring(0, sb.length() - 1);
                if (!validateRouteTableKey(sb.charAt(0), str)) {
                    throw syntaxError("Invalid RouteTable KEY:\"" + sb);
                }
                validateRouteTableValue(str);
            }
        }

        /**
         * @throws JSONException 自定义JSON异常
         */
        public static void validateNumber() throws JSONException {
            StringBuilder sb = new StringBuilder();
            if (curchar == '-') { //可选负号
                curchar = next();
            }
            if (curchar > 48 && curchar <= 57) { //整数部分
                do {
                    sb.append(curchar);
                    curchar = next();
                } while (curchar >= 48 && curchar <= 57);
            } else if (curchar == 48) {
                curchar = next();
            } else {
                throw syntaxError("Invalid number");
            }
            if (curchar == '.') { //小数部分
                do { //.后可不跟数字 如 5. 为合法数字
                    curchar = next();
                } while (curchar >= 48 && curchar <= 57);
            }
            if (curchar == 'e' || curchar == 'E') { //科学计数部分
                curchar = next();
                if (curchar == '+' || curchar == '-') {
                    curchar = next();
                }
                if (curchar < 48 || curchar > 57) {
                    throw syntaxError("Invalid number");
                }
                do {
                    curchar = next();
                } while (curchar >= 48 && curchar <= 57);
            }
            if (curchar == '"') {
                throw syntaxError("Missing comma after number: " + sb);
            }
            back(); //指针移至数字值最后一位，取下一位即判断是,或者],或者是合法注释
        }

        public static void validateRouteTableValue(String key) throws JSONException {
            int a = index;
            char c;
            List<String> num_list = Arrays.asList("port");
            List<String> boolean_list = Arrays.asList("useSSL", "default_allow");
            do {
                ++a;
                c = value.charAt(a);
            } while (c == ' ');
            if (num_list.contains(key) && !(c == '-' || (c >= 48 && c <= 57))) {
                throw syntaxError("RouteTable KEY:" + key + " match NumberType");
            }
            if (boolean_list.contains(key) && !(c == 't' || c == 'f' || c == 'n')) {
                throw syntaxError("RouteTable KEY:" + key + " match BooleanType");
            }
        }

        public static boolean validateRouteTableKey(char firstChar, String str) throws JSONException {
            if ("".equals(str)) return false;
            List<String> a_list = Arrays.asList("allow");
            List<String> c_list = Arrays.asList("certChainFile","caFile","coordinator");
            List<String> d_list = Arrays.asList("default", "default_allow","deny");
            List<String> f_list = Arrays.asList("from");
            List<String> h_list = Arrays.asList("host");
            List<String> i_list = Arrays.asList("ip");
            List<String> n_list = Arrays.asList("negotiationType","caFile");
            List<String> p_list = Arrays.asList("permission", "port", "privateKeyFile");
            List<String> r_list = Arrays.asList("route_table","role");
            List<String> s_list = Arrays.asList("serving");
            List<String> t_list = Arrays.asList("to");
            List<String> u_list = Arrays.asList("useSSL");
            switch (firstChar) {
                case ' ':
                    return false;
                case 'a':
                    return a_list.contains(str);
                case 'f':
                    return f_list.contains(str);
                case 't':
                    return t_list.contains(str);
                case 'i':
                    return i_list.contains(str);
                case 'h':
                    return h_list.contains(str);
                case 's':
                    return s_list.contains(str);
                case 'u':
                    return u_list.contains(str);
                case 'c':
                    return c_list.contains(str);
                case 'n':
                    return n_list.contains(str);
                case 'r':
                    return r_list.contains(str);
                case 'd':
                    return d_list.contains(str);
                case 'p':
                    return p_list.contains(str);
                default:
                    return true;
            }

        }

        /**
         * @throws JSONException 自定义JSON异常
         */
        public static void validateBooleanAndNull() throws JSONException {
            StringBuilder sb = new StringBuilder();
            do {
                sb.append(curchar);
                curchar = next();
            } while (curchar >= ' ' && "\",]#/}".indexOf(curchar) < 0 && curchar != 127);
            if (!"null".equals(sb.toString()) && !"true".equals(sb.toString()) && !"false".equals(sb.toString())) {
                throw syntaxError("Boolean/null spelling errors : " + sb);
            }
            if (curchar == '"') {
                throw syntaxError("Missing comma after Boolean: " + sb);
            }
            back();
        }
    }

    public static void main(String[] args) {
//        String str = "{\"route_table\": {\"default\":{\"default\":[{\"ip\":\"127.0.0.1\",\"port\":9999,\"useSSL\":false}]},\"10000\":{\"default\":[{\"ip\":\"127.0.0.1\",\"port\":8889}],\"serving\":[{\"ip\":\"127.0.0.1\",\"port\":8080}]},\"123\":[{\"host\":\"127.0.0.1\",\"port\":8888,\"useSSL\":false,\"negotiationType\":\"\",\"certChainFile\":\"\",\"privateKeyFile\":\"\",\"caFile\":\"\"}]},\"permission\":{\"default_allow\":true}}";
        String str = "{\"route_table\":{\"default\":{\"default\":[{\"ip\":\"127.0.0.1\",\"port\":12345}]},\"10000\":{\"default\":[{\"ip\":\"127.0.0.1\",\"port\":8889}],\"serving\":[{\"ip\":\"127.0.0.1\",\"port\":8080}]}},\"permission\":{\"default_allow\":true,\"allow\":[{\"from\":{\"coordinator\":\"9999\",\"role\":\"guest\"},\"to\":{\"coordinator\":\"10000\",\"role\":\"host\"}}],\"deny\":[{\"from\":{\"coordinator\":\"9999\",\"role\":\"guest\"},\"to\":{\"coordinator\":\"10000\",\"role\":\"host\"}}]}}";
        try {
            if (RouteTableJsonValidator.isJSON(str)) {
                String s = JsonUtil.formatJson(str);
                System.out.println(s);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
