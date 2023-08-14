package com.webank.ai.fate.serving.common.health;

public enum HealthCheckItemEnum {
    CHECK_ROUTER_FILE("check route_table.json exist",HealthCheckComponent.SERVINGPROXY),
    CHECK_ZOOKEEPER_CONFIG("check zk config",HealthCheckComponent.ALL),
    CHECK_ROUTER_NET("check router",HealthCheckComponent.SERVINGPROXY),
    CHECK_MEMORY_USAGE("check memory usage",HealthCheckComponent.ALL),
    CHECK_CERT_FILE("check cert file",HealthCheckComponent.SERVINGPROXY),
    CHECK_DEFAULT_FATEFLOW("check default fateflow config",HealthCheckComponent.SERVINGSERVER),
    CHECK_FATEFLOW_IN_ZK("check fateflow in zookeeper",HealthCheckComponent.SERVINGSERVER),
    CHECK_MODEL_LOADED("check model loaded",HealthCheckComponent.SERVINGSERVER);


    private String itemName;
    private HealthCheckComponent component;

    HealthCheckItemEnum(String name, HealthCheckComponent healthCheckComponent){
        this.component = healthCheckComponent;
        this.itemName= name;
    }
    public  String getItemName(){
        return itemName;
    }

    public  HealthCheckComponent  getComponent(){
        return this.component;
    }








}
