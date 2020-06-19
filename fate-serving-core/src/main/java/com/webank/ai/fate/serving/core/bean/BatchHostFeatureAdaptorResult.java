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

package com.webank.ai.fate.serving.core.bean;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * adaptor 专用
 */
public class BatchHostFeatureAdaptorResult {

    String retcode;
    String caseId;
    /**
     * key 为请求中的index
     */
    Map<Integer, SingleBatchHostFeatureAdaptorResult> indexResultMap = Maps.newHashMap();

    public String getRetcode() {
        return retcode;
    }

    public void setRetcode(String retcode) {
        this.retcode = retcode;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public Map<Integer, SingleBatchHostFeatureAdaptorResult> getIndexResultMap() {
        return indexResultMap;
    }

    public void setIndexResultMap(Map<Integer, SingleBatchHostFeatureAdaptorResult> indexResultMap) {
        this.indexResultMap = indexResultMap;
    }

    public static class SingleBatchHostFeatureAdaptorResult {

        int index;
        String retcode;
        String msg;
        Map<String, Object> features;

        public Map<String, Object> getFeatures() {
            return features;
        }

        public void setFeatures(Map<String, Object> features) {
            this.features = features;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getRetcode() {
            return retcode;
        }

        public void setRetcode(String retcode) {
            this.retcode = retcode;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

    }

}
