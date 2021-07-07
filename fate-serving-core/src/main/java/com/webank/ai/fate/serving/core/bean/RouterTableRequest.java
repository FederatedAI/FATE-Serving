package com.webank.ai.fate.serving.core.bean;

import java.util.List;

/**
 * @auther Xiong li
 * @date 2021/6/29
 * @remark
 */
public class RouterTableRequest {
    String serverHost;
    Integer serverPort;
    List<RouterTable> routerTableList;
    Integer page;
    Integer pageSize;

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public List<RouterTable> getRouterTableList() {
        return routerTableList;
    }

    public void setRouterTableList(List<RouterTable> routerTableList) {
        this.routerTableList = routerTableList;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public static class RouterTable {
        String partyId;
        String ip;
        Integer port;
        boolean useSSL;
        String negotiationType;
        String certChainFile;
        String privateKeyFile;
        String caFile;
        String serverType;

        public String getPartyId() {
            return partyId;
        }

        public void setPartyId(String partyId) {
            this.partyId = partyId;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public boolean isUseSSL() {
            return useSSL;
        }

        public void setUseSSL(boolean useSSL) {
            this.useSSL = useSSL;
        }

        public String getNegotiationType() {
            return negotiationType;
        }

        public void setNegotiationType(String negotiationType) {
            this.negotiationType = negotiationType;
        }

        public String getCertChainFile() {
            return certChainFile;
        }

        public void setCertChainFile(String certChainFile) {
            this.certChainFile = certChainFile;
        }

        public String getPrivateKeyFile() {
            return privateKeyFile;
        }

        public void setPrivateKeyFile(String privateKeyFile) {
            this.privateKeyFile = privateKeyFile;
        }

        public String getCaFile() {
            return caFile;
        }

        public void setCaFile(String caFile) {
            this.caFile = caFile;
        }

        public String getServerType() {
            return serverType;
        }

        public void setServerType(String serverType) {
            this.serverType = serverType;
        }
    }

}
