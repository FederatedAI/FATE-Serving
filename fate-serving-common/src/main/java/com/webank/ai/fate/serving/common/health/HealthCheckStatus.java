package com.webank.ai.fate.serving.common.health;

public enum HealthCheckStatus {
    /**
     * 健康
     */
    ok("状态健康"),

    /**
     * 异常
     */
    warn("状态异常"),

    /**
     * 错误
     */
    error("状态错误");

    private final String desc;

    HealthCheckStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
