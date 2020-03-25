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

package com.webank.ai.fate.serving.core.model;


import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.List;

public class Model implements Comparable<Model>, Serializable {

    private  long timestamp;

    private String tableName;

    private String namespace;

    /**
     *  guest or host
     */
    private String   role;

    private String partId;

    /**
     *  对端模型信息
     */
    private Model federationModel;

    /**
     * 实例化好的模型处理类
     */
    private transient ModelProcessor modelProcessor;

    private String serviceId;


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getRole() {
        return role;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }


    public void setRole(String role) {
        this.role = role;
    }

    public String getPartId() {
        return partId;
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    public Model getFederationModel() {
        return federationModel;
    }

    public void setFederationModel(Model federationModel) {
        this.federationModel = federationModel;
    }

    public ModelProcessor getModelProcessor() {
        return modelProcessor;
    }

    public void setModelProcessor(ModelProcessor modelProcessor) {
        this.modelProcessor = modelProcessor;
    }

    public Model() {
        this.timestamp =  System.currentTimeMillis();
    }


    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public int compareTo(Model o) {
        if(this.timestamp>o.timestamp) {
            return 1;
        }
            else {
            return -1;
        }
    }


    @Override
    public int hashCode()
    {
        return (tableName+namespace).intern().hashCode();
    }
    @Override
    public boolean equals(Object obj)
    {
        Model model =(Model)obj;
        return  this.namespace.equals(model.namespace)&&this.tableName.equals(model.tableName);
    }


    @Override
    public  String toString(){

        return JSON.toJSONString(this);
    }









}
