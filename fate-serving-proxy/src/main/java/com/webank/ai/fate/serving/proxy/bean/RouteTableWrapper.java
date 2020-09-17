package com.webank.ai.fate.serving.proxy.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.webank.ai.fate.serving.core.utils.JsonUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RouteTableWrapper {

    private static final String ROUTE_TABLE = "route_table";
    private static final String PERMISSION = "permission";
    private static final String IP = "ip";
    private static final String PORT = "port";
    private static final String DEFAULT_ALLOW = "default_allow";
    private static final String ALLOW = "allow";
    private static final String DENY = "deny";

    public static void main(String[] args) {
        String json = "{\"route_table\":{\"default\":{\"default\":[{\"ip\":\"127.0.0.1\",\"port\":12345}]},\"10000\":{\"default\":[{\"ip\":\"127.0.0.1\",\"port\":8889}],\"serving\":[{\"ip\":\"127.0.0.1\",\"port\":8080}]}},\"permission\":{\"default_allow\":true,\"allow\":[{\"from\":{\"coordinator\":\"9999\",\"role\":\"guest\"},\"to\":{\"coordinator\":\"10000\",\"role\":\"host\"}}],\"deny\":[{\"from\":{\"coordinator\":\"9999\",\"role\":\"guest\"},\"to\":{\"coordinator\":\"10000\",\"role\":\"host\"}}]}}";

        Map wrapper = JsonUtil.json2Object(json, Map.class);

        System.out.println(JsonUtil.object2Json(wrapper));

        RouteTableWrapper wrapper1 = new RouteTableWrapper().parse(JsonUtil.object2Json(wrapper));
        System.out.println(JsonUtil.object2Json(wrapper1));

        wrapper1.getRouteTable().getService("default").getNodes("default").get(0).setPort(6666);

        System.out.println(wrapper1.toString());
    }

    /**
     * json to RouteTable
     * @param json
     * @return
     */
    public RouteTableWrapper parse(String json) {
        try {
            Map map = JsonUtil.json2Object(json, Map.class);
            Map routeTableMap = (Map) map.get(ROUTE_TABLE);
            Map permissionMap = (Map) map.get(PERMISSION);

            RouteTableWrapper wrapper = new RouteTableWrapper();
            RouteTable routeTable = new RouteTable();
            Permission permission = new Permission();

            for (Object e : routeTableMap.entrySet()) {
                Map.Entry entry = (Map.Entry) e;
                String partner = (String) entry.getKey(); // 10000
                Map serviceMap = (Map) entry.getValue();

                Service service = routeTable.getPartnerServiceMap().computeIfAbsent(partner, k -> new Service());

                for (Object se : serviceMap.entrySet()) {
                    Map.Entry serviceEntry = (Map.Entry) se;
                    String serviceName = (String) serviceEntry.getKey();// serving
                    List<Map> nodes = (List<Map>) serviceEntry.getValue();

                    List<Node> nodeList = service.getServiceNodesMap().computeIfAbsent(serviceName, k -> new ArrayList<>());
                    for (Map nodeMap : nodes) {
                        String ip = (String) nodeMap.get(IP);
                        int port = (int) nodeMap.get(PORT);
                        nodeList.add(new Node(ip, port));
                    }
                }
            }

            permission.setDefaultAllow((Boolean) permissionMap.get(DEFAULT_ALLOW));
            permission.setAllow((List<Map>) permissionMap.get(ALLOW));
            permission.setDeny((List<Map>) permissionMap.get(DENY));

            wrapper.setRouteTable(routeTable);
            wrapper.setPermission(permission);
            return wrapper;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * routeTable to json
     * @return
     */
    @Override
    public String toString() {
        Map resultMap = new HashMap();
        // route_table
        HashMap routeTableMap = (HashMap) resultMap.computeIfAbsent(ROUTE_TABLE, k -> new HashMap<>());
        for (Map.Entry<String, Service> partnerServiceEntry : routeTable.getPartnerServiceMap().entrySet()) {
            // partnerServiceEntry.getKey() == 10000 partner => service
            HashMap serviceNodesMap = (HashMap) routeTableMap.computeIfAbsent(partnerServiceEntry.getKey(), k -> new HashMap<>());

            Service service = partnerServiceEntry.getValue();
            for (Map.Entry<String, List<Node>> serviceNodeEntry : service.getServiceNodesMap().entrySet()) {
                // serving => ip:port
                List<Node> nodeList = (List<Node>) serviceNodesMap.computeIfAbsent(serviceNodeEntry.getKey(), k -> new ArrayList<>());
                nodeList.addAll(serviceNodeEntry.getValue());
            }
        }

        // permission
        HashMap permissionMap = (HashMap) resultMap.computeIfAbsent(PERMISSION, k -> new HashMap<>());
        permissionMap.putIfAbsent(DEFAULT_ALLOW, this.getPermission().isDefaultAllow());
        permissionMap.putIfAbsent(ALLOW, this.getPermission().getAllow());
        permissionMap.putIfAbsent(DENY, this.getPermission().getDeny());

        return JsonUtil.object2Json(resultMap);
    }

    public final static String DEFAULT_NODE = "default";
    public final static String DEFAULT_SERVICE = "default";

    @JsonProperty("route_table")
    private RouteTable routeTable;

    private Permission permission;

    public RouteTable getRouteTable() {
        return routeTable;
    }

    public void setRouteTable(RouteTable routeTable) {
        this.routeTable = routeTable;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    class RouteTable {

        @JsonIgnore
        private ConcurrentMap<String, Service> partnerServiceMap = new ConcurrentHashMap<>();

        public ConcurrentMap<String, Service> getPartnerServiceMap() {
            return partnerServiceMap;
        }

        public Service getService(String partner) {
            return this.partnerServiceMap.get(partner);
        }

        public void setService(String partner, Service service) {
            this.partnerServiceMap.put(partner, service);
        }
    }

    class Service {
        @JsonIgnore
        private ConcurrentMap<String, List<Node>> serviceNodesMap = new ConcurrentHashMap<>();

        public ConcurrentMap<String, List<Node>> getServiceNodesMap() {
            return serviceNodesMap;
        }

        public List<Node> getNodes(String serviceName) {
            return this.serviceNodesMap.get(serviceName);
        }

        public void setNodes(String serviceName, List<Node> nodes) {
            this.serviceNodesMap.put(serviceName, nodes);
        }
    }

    class Node {

        public Node(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        private String ip;
        private int port;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    class Permission {
        @JsonProperty("default_allow")
        private boolean defaultAllow = Boolean.TRUE;

        // allow, json array
        private List<Map> allow;

        // allow, json array
        private List<Map> deny;

        public Permission() {
        }

        public boolean isDefaultAllow() {
            return defaultAllow;
        }

        public void setDefaultAllow(boolean defaultAllow) {
            this.defaultAllow = defaultAllow;
        }

        public List<Map> getAllow() {
            return allow;
        }

        public void setAllow(List<Map> allow) {
            this.allow = allow;
        }

        public List<Map> getDeny() {
            return deny;
        }

        public void setDeny(List<Map> deny) {
            this.deny = deny;
        }
    }

    /**
     * {
     *   "route_table": {
     *     "default": {
     *       "default": [
     *         {
     *           "ip": "127.0.0.1",
     *           "port": 12345
     *         }
     *       ]
     *     },
     *     "10000": {
     *       "default": [
     *         {
     *           "ip": "127.0.0.1",
     *           "port": 8889
     *         }
     *       ],
     *       "serving": [
     *         {
     *           "ip": "127.0.0.1",
     *           "port": 8080
     *         }
     *       ]
     *     }
     *   },
     *   "permission": {
     *     "default_allow": true,
     * 	"allow": [{
     * 		"from": {
     * 			"coordinator": "9999",
     * 			"role": "guest"
     *                },
     * 		"to": {
     * 			"coordinator": "10000",
     * 			"role": "host"
     *        }* 	}],
     * 	"allow": [{
     * 		"from": {
     * 			"coordinator": "9999",
     * 			"role": "guest"
     * 		}        ,
     * 		"to": {
     * 			"coordinator": "10000",
     * 			"role": "host"
     *        }
     *    }]
     *   }
     * }
     */
}

