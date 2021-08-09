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

package com.webank.ai.fate.serving.common.model;

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Model implements Comparable<Model>, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    String resourceName;
    private long timestamp;
    private String tableName;
    private String namespace;
    /**
     * guest or host
     */
    private String role;
    private String partId;
    /**
     * 对端模型信息,因为需要支持多方,所以设计成Map
     */
    private Map<String, Model> federationModelMap = Maps.newHashMap();
    /**
     * 实例化好的模型处理类
     */
    private transient ModelProcessor modelProcessor;
    private List<String> serviceIds = new ArrayList<>();
    private List<Map> rolePartyMapList;

    private Double allowQps;

    private String resourceAdress;

    public String getResourceAdress() {
        return resourceAdress;
    }

    public void setResourceAdress(String resourceAdress) {
        resourceAdress = resourceAdress;
    }

    public Double getAllowQps() {
        return allowQps;
    }

    public void setAllowQps(Double allowQps) {
        this.allowQps = allowQps;
    }

    public Model() {
        this.timestamp = System.currentTimeMillis();
    }

    public List<Map> getRolePartyMapList() {
        return rolePartyMapList;
    }

    public void setRolePartyMapList(List<Map> rolePartyMapList) {
        this.rolePartyMapList = rolePartyMapList;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public ModelProcessor getModelProcessor() {
        return modelProcessor;
    }

    public void setModelProcessor(ModelProcessor modelProcessor) {
        this.modelProcessor = modelProcessor;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<String> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<String> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public Map<String, Model> getFederationModelMap() {
        return federationModelMap;
    }

    public void setFederationModelMap(Map<String, Model> federationModelMap) {
        this.federationModelMap = federationModelMap;
    }

    @Override
    public int compareTo(Model o) {
        if (this.timestamp > o.timestamp) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    public int hashCode() {
        return (tableName + namespace).intern().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        Model model = (Model) obj;
        return this.namespace.equals(model.namespace) && this.tableName.equals(model.tableName);
    }

    @Override
    public Object clone() {
        return SerializationUtils.clone(this);
    }

    @Override
    public String toString() {
        return JsonUtil.object2Json(this);
    }

    public String getResourceName() {
        if (StringUtils.isBlank(resourceName)) {
            resourceName = "M_" + tableName + "_" + namespace;
        }
        return resourceName;
    }

}
