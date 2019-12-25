package com.webank.ai.fate.serving.proxy.rpc.router;

public class RouterInfo {
    private String host;

    private Integer port;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host == null ? null : host.trim();
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }


    @Override
    public String  toString(){

        StringBuilder   sb = new StringBuilder();
        sb.append(host).append(":").append(port);
        return  sb.toString();

    }

}