package com.webank.ai.fate.serving.common.bean;

import com.google.common.collect.Lists;

import java.util.List;

public  class HealthCheckResult{
        List<String> okList= Lists.newArrayList();
        List<String> errorList= Lists.newArrayList();

    public List<String> getOkList() {
        return okList;
    }

    public void setOkList(List<String> okList) {
        this.okList = okList;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<String> errorList) {
        this.errorList = errorList;
    }

    public List<String> getWarnList() {
        return warnList;
    }

    public void setWarnList(List<String> warnList) {
        this.warnList = warnList;
    }

    List<String> warnList = Lists.newArrayList();
    }
