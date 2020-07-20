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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchInferenceResult extends ReturnResult {

    List<SingleInferenceResult> batchDataList;
    private Map<Integer, SingleInferenceResult> singleInferenceResultMap;
    public List<SingleInferenceResult> getBatchDataList() {
        if (batchDataList == null) {
            batchDataList = new ArrayList<>();
        }
        return batchDataList;
    }
    public void setBatchDataList(List<SingleInferenceResult> batchDataList) {
        this.batchDataList = batchDataList;
    }
    public void rebuild() {
        Map result = Maps.newHashMap();
        List<BatchInferenceResult.SingleInferenceResult> batchInferences = this.getBatchDataList();
        for (BatchInferenceResult.SingleInferenceResult singleInferenceResult : batchInferences) {
            result.put(singleInferenceResult.getIndex(), singleInferenceResult);
        }
        singleInferenceResultMap = result;
    }

    public Map<Integer, SingleInferenceResult> getSingleInferenceResultMap() {
        if (singleInferenceResultMap == null) {
            rebuild();
        }
        return singleInferenceResultMap;
    }

    static public class SingleInferenceResult {
        Integer index;
        int retcode;
        String retmsg;
        Map<String, Object> data;
        public SingleInferenceResult() {
        }

        public SingleInferenceResult(Integer index, int retcode, String msg, Map<String, Object> data) {
            this.index = index;
            this.retcode = retcode;
            this.retmsg = msg;
            this.data = data;
        }

        public int getRetcode() {
            return retcode;
        }

        public void setRetcode(int retcode) {
            this.retcode = retcode;
        }

        public Map<String, Object> getData() {
            return data;
        }

        public void setData(Map<String, Object> data) {
            this.data = data;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getRetmsg() {
            return retmsg;
        }

        public void setRetmsg(String retmsg) {
            this.retmsg = retmsg;
        }
    }
}
