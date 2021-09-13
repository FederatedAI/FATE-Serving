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

import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.common.model.LocalInferenceAware;
import com.webank.ai.fate.serving.common.model.MergeInferenceAware;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.GuestMergeException;
import com.webank.ai.fate.serving.core.exceptions.ModelLoadException;
import com.webank.ai.fate.serving.core.exceptions.SbtDataException;
import org.apache.commons.collections4.map.HashedMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeteroSecureBoostingTreeGuest extends HeteroSecureBoost implements MergeInferenceAware, LocalInferenceAware, Returnable {
    private final String role = "guest";
    private boolean fastMode = true;
    private double sigmoid(double x) {
        return 1. / (1. + Math.exp(-x));
    }
    private Map<String, Object> softmax(double weights[]) {
        int n = weights.length;
        double max = weights[0];
        int maxIndex = 0;
        double denominator = 0.0;
        for (int i = 1; i < n; ++i) {
            if (weights[i] > weights[maxIndex]) {
                maxIndex = i;
                max = weights[i];
            }
        }
        for (int i = 0; i < n; i++) {
            weights[i] = Math.exp(weights[i] - max);
            denominator += weights[i];
        }
        ArrayList<Double> scores = new ArrayList<Double>();
        for (int i = 0; i < n; ++i) {
            scores.add(weights[i] / denominator);
        }
        Map<String, Object> ret = Maps.newHashMap();
        ret.put("label", this.classes.get(maxIndex));
        ret.put("score", scores);
        return ret;
    }

    private boolean isLocateInLeaf(int treeId, int treeNodeId) {
        return this.trees.get(treeId).getTree(treeNodeId).getIsLeaf();
    }

    private boolean checkLeafAll(int[] treeNodeIds) {
        for (int i = 0; i < this.treeNum; ++i) {
            if (!isLocateInLeaf(i, treeNodeIds[i])) {
                return false;
            }
        }
        return true;
    }

    private double getTreeLeafWeight(int treeId, int treeNodeId) {
        return this.trees.get(treeId).getTree(treeNodeId).getWeight();
    }

    private int traverseTree(int treeId, int treeNodeId, Map<String, Object> input) {
        while (!this.isLocateInLeaf(treeId, treeNodeId) && this.getSite(treeId, treeNodeId).equals(this.site)) {
            treeNodeId = this.gotoNextLevel(treeId, treeNodeId, input);
        }
        return treeNodeId;
    }

    private int fastTraverseTree(int treeId, int treeNodeId, Map<String, Object> input, Map<String, Object> lookUpTable) {
        while (!this.isLocateInLeaf(treeId, treeNodeId)) {
            if (this.getSite(treeId, treeNodeId).equals(this.site)) {
                treeNodeId = this.gotoNextLevel(treeId, treeNodeId, input);
            } else {
                Map<String, Boolean> lookUp = (Map<String, Boolean>) lookUpTable.get(String.valueOf(treeId));
                if(lookUp==null){
                    logger.error("treeId {} is not exist in {}",treeId,lookUpTable.keySet());
                    throw  new SbtDataException();
                }
                if(lookUp.get(String.valueOf(treeNodeId))!=null) {
                    if (lookUp.get(String.valueOf(treeNodeId))) {
                        treeNodeId = this.trees.get(treeId).getTree(treeNodeId).getLeftNodeid();
                    } else {
                        treeNodeId = this.trees.get(treeId).getTree(treeNodeId).getRightNodeid();
                    }
                }else{
                    logger.error("tree node id {} not exist in {} ,lookup table  {}",treeNodeId,lookUp,lookUpTable);
                    throw  new SbtDataException();
                }
            }
            if (logger.isDebugEnabled()) {
                logger.info("tree id is {}, tree node is {}", treeId, treeNodeId);
            }
        }
        return treeNodeId;
    }

    private Map<String, Object> getFinalPredict(double[] weights) {
        Map<String, Object> ret = new HashMap<String, Object>(8);
        if (this.numClasses == 2) {
            double sum = 0;
            for (int i = 0; i < this.treeNum; ++i) {
                sum += weights[i] * this.learningRate;
            }
            ret.put(Dict.SCORE, this.sigmoid(sum));
        } else if (this.numClasses > 2) {
            double[] sumWeights = new double[this.treeDim];
            for (int i = 0; i < this.treeNum; ++i) {
                sumWeights[i % this.treeDim] += weights[i] * this.learningRate;
            }
            for (int i = 0; i < this.treeDim; i++) {
                sumWeights[i] += this.initScore.get(i);
            }
            ret = softmax(sumWeights);
        } else {
            double sum = this.initScore.get(0);
            for (int i = 0; i < this.treeNum; ++i) {
                sum += weights[i] * this.learningRate;
            }
            ret.put(Dict.SCORE, sum);
        }
        return ret;
    }

    @Override
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> inputData) {
        Map<String, Object> result = Maps.newHashMap();
        int[] treeNodeIds = new int[this.treeNum];
        HashMap<String, Object> fidValueMapping = new HashMap<String, Object>(8);
        HashMap<String, Object> treeLocation = new HashMap<String, Object>(8);
        Map<String, Object> input = inputData.get(0);
        int featureHit = 0;
        for (String key : input.keySet()) {
            if (this.featureNameFidMapping.containsKey(key)) {
                fidValueMapping.put(this.featureNameFidMapping.get(key).toString(), input.get(key));
                ++featureHit;
            }
        }
        logger.info("feature hit rate : {}", 1.0 * featureHit / this.featureNameFidMapping.size());
        double[] weights = new double[this.treeNum];
        int communicationRound = 0;
        for (int i = 0; i < this.treeNum; ++i) {
            if (this.isLocateInLeaf(i, treeNodeIds[i])) {
                continue;
            }
            treeNodeIds[i] = this.traverseTree(i, treeNodeIds[i], fidValueMapping);
            if (!this.isLocateInLeaf(i, treeNodeIds[i])) {
                treeLocation.put(String.valueOf(i), treeNodeIds[i]);
            }
        }
        result.put(Dict.SBT_TREE_NODE_ID_ARRAY, treeNodeIds);
        result.put("fidValueMapping", fidValueMapping);
        return result;
    }

    private Map<String, Boolean> mergeTwoLookUpTable(Map<String, Boolean> lookUp1, Map<String, Boolean> lookUp2){

        Map<String, Boolean> merged = new HashMap<>();
        merged.putAll(lookUp1);
        merged.putAll(lookUp2);

        return merged;
    }

    private Map<String, Object> extractMultiPartiesData(Map<String, Object> remoteData, int tree_num){

        // merge multi host lookup table (if there are multiple hosts)
        Map<String, Object> extract_result = new HashedMap<String, Object>();
        Map<String, Object> mergedLookUpTable = new HashMap<>();

        // extract component data
        remoteData.forEach((k, v) -> {
            Map<String, Object> onePartyData = (Map<String, Object>) v;
            Map<String, Object> remoteComopnentData = (Map<String, Object>) onePartyData.get(this.getComponentName());
            if (remoteComopnentData == null) {
                remoteComopnentData = onePartyData;
            }
            extract_result.put(k, remoteComopnentData);
        });

        // merge host look up tables
        extract_result.forEach((k, v) -> {
            Map<String, Map<String, Boolean>> lookUp = (Map<String, Map<String, Boolean>>) v;
            for(int treeId=0; treeId<tree_num; treeId++){
                String str_key = String.valueOf(treeId);
                Map<String, Boolean> treeLookUp = lookUp.get(str_key);
                if(!mergedLookUpTable.containsKey(str_key)){
                    mergedLookUpTable.put(str_key,treeLookUp);
                }
                else{
                    Map<String, Boolean> savedLookUp = (Map<String, Boolean>)mergedLookUpTable.get(str_key);
                    Map<String, Boolean> mergedResult = this.mergeTwoLookUpTable(savedLookUp, treeLookUp);
                    mergedLookUpTable.put(str_key, mergedResult);
                }
            }
        });

        return mergedLookUpTable;
    }

    @Override
    public Map<String, Object> mergeRemoteInference(Context context, List<Map<String, Object>> localDataList, Map<String, Object> remoteData) {
        Map<String, Object> result = this.handleRemoteReturnData(remoteData);
        if ((int) result.get(Dict.RET_CODE) != StatusCode.SUCCESS) {
            return result;
        }
        Map<String, Object> localData = (Map<String, Object>) localDataList.get(0).get(this.getComponentName());
        int[] treeNodeIds = (int[]) localData.get(Dict.SBT_TREE_NODE_ID_ARRAY);
        double[] weights = new double[this.treeNum];
        if (treeNodeIds == null) {
            throw new GuestMergeException("tree node id array is not return from first loop");
        }
        HashMap<String, Object> fidValueMapping = (HashMap<String, Object>) localData.get("fidValueMapping");

        Map<String, Object> lookUpTable = this.extractMultiPartiesData(remoteData, this.treeNum);
        HashMap<String, Object> treeLocation = new HashMap<String, Object>(8);
        for (int i = 0; i < this.treeNum; ++i) {
            if (this.isLocateInLeaf(i, treeNodeIds[i])) {
                continue;
            }
            treeNodeIds[i] = this.traverseTree(i, treeNodeIds[i], fidValueMapping);
            if (!this.isLocateInLeaf(i, treeNodeIds[i])) {
                treeLocation.put(String.valueOf(i), treeNodeIds[i]);
            }
        }
        for (String treeIdx : treeLocation.keySet()) {
            int idx = Integer.valueOf(treeIdx);
            int curNodeId = (Integer) treeLocation.get(treeIdx);
            int final_node_id = this.fastTraverseTree(idx, curNodeId, fidValueMapping, lookUpTable);
            treeNodeIds[idx] = final_node_id;
        }
        for (int i = 0; i < this.treeNum; ++i) {
            weights[i] = getTreeLeafWeight(i, treeNodeIds[i]);
        }
        if (logger.isDebugEnabled()) {
            logger.info("tree leaf ids is {}", treeNodeIds);
            logger.info("weights is {}", weights);
        }

        return getFinalPredict(weights);
    }



}