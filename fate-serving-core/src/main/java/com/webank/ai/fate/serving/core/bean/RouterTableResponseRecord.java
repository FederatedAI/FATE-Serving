package com.webank.ai.fate.serving.core.bean;

import com.google.gson.JsonArray;

import java.util.List;

/**
 * @auther Xiongli
 * @date 2021/6/29
 * @remark
 */
public class RouterTableResponseRecord {
    String partyId;
    Long createTime;
    Long updateTime;
    JsonArray routerList;
    int count;

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public JsonArray getRouterList() {
        return routerList;
    }

    public void setRouterList(JsonArray routerList) {
        this.routerList = routerList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
