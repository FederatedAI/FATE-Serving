package com.webank.ai.fate.register.common;

public class ServiceWrapper {

    private Integer weight;
    private Long version;
    private String routerMode;

    public ServiceWrapper() {

    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getRouterMode() {
        return routerMode;
    }

    public void setRouterMode(String routerMode) {
        this.routerMode = routerMode;
    }

    public void update(ServiceWrapper wrapper) {
        if (wrapper.getRouterMode() != null) {
            this.setRouterMode(wrapper.getRouterMode());
        }
        if (wrapper.getWeight() != null) {
            this.setWeight(wrapper.getWeight());
        }
        if (wrapper.getVersion() != null) {
            this.setVersion(wrapper.getVersion());
        }
    }
}
