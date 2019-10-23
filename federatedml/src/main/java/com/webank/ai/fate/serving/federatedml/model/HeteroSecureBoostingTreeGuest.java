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
import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.FederatedParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeteroSecureBoostingTreeGuest extends HeteroSecureBoost {

    private final String site = "guest";

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
            // denominator += Math.exp(weights[i] - min);
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

    /*
    Map<String, Double> forward(List<Map<String, Object>> inputDatas) {
        Map<String, Object> inputData = inputDatas.get(0);
        HashMap<Integer, Object> fidValueMapping = new HashMap<Integer, Object>();
        int featureHit = 0;
        for (String key : inputData.keySet()) {
            if (this.featureNameFidMapping.containsKey(key)) {
                fidValueMapping.put(this.featureNameFidMappin.get(key), inputData.get(key));
                ++featureHit;
            }
        }
        LOGGER.info("feature hit rate : {}", 1.0 * featureHit / this.featureNameFidMapping.size());

    }
    */

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


    private Map<String, Object> getFinalPredict(double[] weights) {
        Map<String, Object> ret = new HashMap<String, Object>();
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

            for (int i = 0; i < this.treeDim; i++)
                sumWeights[i] += this.initScore.get(i);

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
    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {

        LOGGER.info("HeteroSecureBoostingTreeGuest FederatedParams {}", predictParams);

        Map<String, Object> input = inputData.get(0);
        HashMap<String, Object> fidValueMapping = new HashMap<String, Object>();

        ReturnResult returnResult = this.getFederatedPredict(context, predictParams, Dict.FEDERATED_INFERENCE, false);

        int featureHit = 0;
        for (String key : input.keySet()) {
            if (this.featureNameFidMapping.containsKey(key)) {
                fidValueMapping.put(this.featureNameFidMapping.get(key).toString(), input.get(key));
                ++featureHit;
            }
        }
        LOGGER.info("feature hit rate : {}", 1.0 * featureHit / this.featureNameFidMapping.size());
        int[] treeNodeIds = new int[this.treeNum];
        double[] weights = new double[this.treeNum];
        int communicationRound = 0;
        while (true) {
            HashMap<String, Object> treeLocation = new HashMap<String, Object>();
            for (int i = 0; i < this.treeNum; ++i) {
                if (this.isLocateInLeaf(i, treeNodeIds[i])) {
                    continue;
                }
                treeNodeIds[i] = this.traverseTree(i, treeNodeIds[i], fidValueMapping);
                if (!this.isLocateInLeaf(i, treeNodeIds[i])) {
                    treeLocation.put(String.valueOf(i), treeNodeIds[i]);
                }
            }
            if (treeLocation.size() == 0) {
                break;
            }
            //  String tag = this.generateTag(predictParams.getCaseId(), this.componentName, communicationRound++);

            // predictParams.getData().put(Dict.TAG,tag);

            predictParams.getData().put(Dict.COMPONENT_NAME, this.componentName);

            predictParams.getData().put(Dict.TREE_COMPUTE_ROUND, communicationRound++);

            predictParams.getData().put(Dict.TREE_LOCATION, treeLocation);

            try {
                LOGGER.info("begin to federated");

                ReturnResult tempResult = this.getFederatedPredict(context, predictParams, Dict.FEDERATED_INFERENCE_FOR_TREE, false);

                Map<String, Object> afterLocation = tempResult.getData();

                LOGGER.info("after loccation is {}", afterLocation);
                for (String location : afterLocation.keySet()) {
                    treeNodeIds[new Integer(location)] = ((Number) afterLocation.get(location)).intValue();
                }

                if (afterLocation == null) {
                    LOGGER.info("receive predict result of host is null");
                    throw new Exception("Null Data");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }

        for (int i = 0; i < this.treeNum; ++i) {
            weights[i] = getTreeLeafWeight(i, treeNodeIds[i]);
        }

        LOGGER.info("tree leaf ids is {}", treeNodeIds);
        LOGGER.info("weights is {}", weights);

        return getFinalPredict(weights);
    }
}
