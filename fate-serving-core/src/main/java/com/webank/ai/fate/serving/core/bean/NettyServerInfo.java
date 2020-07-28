package com.webank.ai.fate.serving.core.bean;


import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;

public class NettyServerInfo {
    public NettyServerInfo() {
        this.negotiationType = NegotiationType.PLAINTEXT;
    }
    public NettyServerInfo(String negotiationType, String certChainFilePath, String privateKeyFilePath, String trustCertCollectionFilePath) {
        if("PLAINTEXT".equals(negotiationType)){
            this.negotiationType = NegotiationType.PLAINTEXT;
        } else {
            this.negotiationType = NegotiationType.TLS;
        }
        this.privateKeyFilePath = privateKeyFilePath;
        this.certChainFilePath = certChainFilePath;
        this.trustCertCollectionFilePath = trustCertCollectionFilePath;
    }

    private NegotiationType negotiationType;

    private String certChainFilePath;

    public NegotiationType getNegotiationType() {
        return negotiationType;
    }

    public String getCertChainFilePath() {
        return certChainFilePath;
    }

    public String getPrivateKeyFilePath() {
        return privateKeyFilePath;
    }

    private String privateKeyFilePath;

    public String getTrustCertCollectionFilePath() {
        return trustCertCollectionFilePath;
    }

    private String trustCertCollectionFilePath;
}
