package com.webank.ai.fate.serving.core.bean;

import java.util.List;

/**
 * @auther Xiongli
 * @date 2021/6/29
 * @remark
 */
public class RouterTableRequest {
    String serverHost;
    Integer serverPort;
    List<RouterTable> routerTableList;

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

    public static class RouterTable {
        String partyId;
        String host;
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
