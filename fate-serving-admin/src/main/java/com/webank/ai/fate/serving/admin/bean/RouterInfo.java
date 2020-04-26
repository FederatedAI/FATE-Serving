package com.webank.ai.fate.serving.admin.bean;

import lombok.Data;

@Data
public class RouterInfo {

    private String host;

    private Integer port;

    @Override
    public String toString() {
        return new StringBuilder().append(host).append(":").append(port).toString();

    }

}