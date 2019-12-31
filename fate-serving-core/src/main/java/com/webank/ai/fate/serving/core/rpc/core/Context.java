package com.webank.ai.fate.serving.core.rpc.core;

import com.webank.ai.fate.serving.core.rpc.grpc.GrpcType;
import com.webank.ai.fate.serving.core.rpc.router.RouterInfo;

/**
 * @Description TODO
 * @Author
 **/
public class Context {

    public GrpcType getGrpcType() {
        return grpcType;
    }

    public void setGrpcType(GrpcType grpcType) {
        this.grpcType = grpcType;
    }

    private GrpcType grpcType;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private String  version;

    private   String  guestAppId;

    private   String  hostAppid;


    public String getGuestAppId() {
        return guestAppId;
    }

    public void setGuestAppId(String guestAppId) {
        this.guestAppId = guestAppId;
    }

    public String getHostAppid() {
        return hostAppid;
    }

    public void setHostAppid(String hostAppid) {
        this.hostAppid = hostAppid;
    }

    public RouterInfo  getRouterInfo() {
        return routerInfo;
    }

    public void setRouterInfo(RouterInfo routerInfo) {
        this.routerInfo = routerInfo;
    }

    private RouterInfo routerInfo;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    private String  caseId;

    public Object getResultData() {
        return resultData;
    }

    public void setResultData(Object resultData) {
        this.resultData = resultData;
    }

    private Object resultData;


    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }


    private String returnCode;

    public long getDownstreamCost() {
        return downstreamCost;
    }

    public void setDownstreamCost(long downstreamCost) {
        this.downstreamCost = downstreamCost;
    }

    private long downstreamCost;

    public long getDownstreamBegin() {
        return downstreamBegin;
    }

    public void setDownstreamBegin(long downstreamBegin) {
        this.downstreamBegin = downstreamBegin;
    }

    private long  downstreamBegin;

    public long getRouteBasis() {
        return routeBasis;
    }
    public void setRouteBasis(long routeBasis) {
        this.routeBasis = routeBasis;
    }
    private long routeBasis;

    public String getSourceIp() {
        return sourceIp;
    }
    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }
    private String  sourceIp;

    public String getServiceName() {
        return serviceName;
    }
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    String serviceName;

}
