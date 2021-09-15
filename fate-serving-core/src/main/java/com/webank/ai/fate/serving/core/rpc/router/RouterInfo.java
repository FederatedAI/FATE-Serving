/*
 * Copyright 2019 The FATE Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.ai.fate.serving.core.rpc.router;

public class RouterInfo {

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    private Protocol  protocol;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String url;

    private String host;

    private Integer port;

    private boolean useSSL;

    private String negotiationType;

    private String certChainFile;

    private String privateKeyFile;

    private String caFile;

    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

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

    public String getCaFile() {
        return caFile;
    }

    public String getCertChainFile() {
        return certChainFile;
    }

    public String getNegotiationType() {
        return negotiationType;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setCaFile(String caFile) {
        this.caFile = caFile;
    }

    public void setCertChainFile(String certChainFile) {
        this.certChainFile = certChainFile;
    }

    public void setNegotiationType(String negotiationType) {
        this.negotiationType = negotiationType;
    }

    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(host).append(":").append(port);
        return sb.toString();
    }

}