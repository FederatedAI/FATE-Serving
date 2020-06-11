package com.webank.ai.fate.serving.core.bean;

import com.webank.ai.fate.serving.core.utils.JsonUtil;

public class ServiceDataWrapper {

    private String url;
    private String project;
    private String environment;
    private String name;
    private String host;
    private Integer port;
    private String routerMode;
    private Long version;
    private Integer weight;
    private int index;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getRouterMode() {
        return routerMode;
    }

    public void setRouterMode(String routerMode) {
        this.routerMode = routerMode;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return JsonUtil.object2Json(this);
    }
}
