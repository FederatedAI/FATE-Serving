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

package com.webank.ai.fate.serving.federatedml.model;

import com.webank.ai.fate.serving.core.bean.CacheManager;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.FederatedParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeteroSecureBoostingTreeHost extends HeteroSecureBoost {
    private final String site = "host";
    private final String modelId = "HeteroSecureBoostingTreeHost"; // need to change


    // DefaultCacheManager cacheManager = BaseContext.applicationContext.getBean(DefaultCacheManager.class);

    /*
    Map<String, Double> forward(List<Map<String, Object>> inputDatas) {
        Map<String, Object> inputData = inputDatas.get(0);
        HashMap<Integer, Object> fidValueMapping = new HashMap<Integer, Object>();
        int featureHit = 0;
        for (String key : inputData.keySet()) {
            if (this.featureNameFidMapping.containsKey(key)) {
                fidValueMapping.put(this.featureNameFidMapping.get(key), inputData.get(key));
                ++featureHit;
            }
        }
        LOGGER.info("feature hit rate : {}", 1.0 * featureHit / this.featureNameFidMapping.size());

    }
    */

    private int traverseTree(int treeId, int treeNodeId, Map<String, Object> input) {
        while (getSite(treeId, treeNodeId).equals(this.site)) {
            treeNodeId = this.gotoNextLevel(treeId, treeNodeId, input);
        }

        return treeNodeId;
    }

    public void saveData(Context context, String tag, Map<String, Object> data) {

        CacheManager.getInstance().store(context, tag, data);

    }

    public Map<String, Object> getData(Context context, String tag) {

        Map data = CacheManager.getInstance().restore(context, tag, Map.class);

        return data;
    }

    @Override
    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {

        LOGGER.info("HeteroSecureBoostingTreeHost FederatedParams {}", predictParams);

        Map<String, Object> input = inputData.get(0);

        String tag = predictParams.getCaseId() + "." + this.componentName + "." + Dict.INPUT_DATA;
        Map<String, Object> ret = new HashMap<String, Object>();

        HashMap<String, Object> fidValueMapping = new HashMap<String, Object>();
        int featureHit = 0;
        for (String key : input.keySet()) {
            if (this.featureNameFidMapping.containsKey(key)) {
                fidValueMapping.put(this.featureNameFidMapping.get(key).toString(), input.get(key));
                ++featureHit;
            }
        }
        this.saveData(context, tag, fidValueMapping);
        return ret;
    }


    public Map<String, Object> predictSingleRound(Context context, Map<String, Object> interactiveData, FederatedParams predictParams) {
        String tag = predictParams.getCaseId() + "." + this.componentName + "." + Dict.INPUT_DATA;
        Map<String, Object> input = this.getData(context, tag);
        Map<String, Object> ret = new HashMap<String, Object>();
        for (String treeIdx : interactiveData.keySet()) {
            int idx = Integer.valueOf(treeIdx);
            int nodeId = this.traverseTree(idx, (Integer) interactiveData.get(treeIdx), input);
            ret.put(treeIdx, nodeId);
        }

        return ret;
    }
}
