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
import com.webank.ai.fate.core.constant.StatusCode;
import com.webank.ai.fate.core.mlmodel.buffer.BoostTreeModelParamMeta.BoostingTreeModelMeta;
import com.webank.ai.fate.core.mlmodel.buffer.BoostTreeModelParamProto.BoostingTreeModelParam;
import com.webank.ai.fate.core.mlmodel.buffer.BoostTreeModelParamProto.DecisionTreeModelParam;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

public abstract class HeteroSecureBoost extends BaseModel {
    public static final Logger LOGGER = LogManager.getLogger();
    protected List<Map<Integer, Double>> split_maskdict;
    protected Map<String, Integer> featureNameFidMapping = Maps.newHashMap();
    protected int treeNum;
    protected List<Double> initScore;
    protected List<DecisionTreeModelParam> trees;
    protected int numClasses;
    protected List<String> classes;
    protected int treeDim;
    protected double learningRate;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        LOGGER.info("start init HeteroLR class");
        try {

            BoostingTreeModelParam param = this.parseModel(BoostingTreeModelParam.parser(), protoParam);
            BoostingTreeModelMeta meta = this.parseModel(BoostingTreeModelMeta.parser(), protoMeta);

            java.util.Map<java.lang.Integer, java.lang.String> featureNameMapping = param.getFeatureNameFidMapping();

            featureNameMapping.forEach((k, v) -> {

                featureNameFidMapping.put(v, k);


            });


            this.treeNum = param.getTreeNum();
            this.initScore = param.getInitScoreList();
            this.trees = param.getTreesList();
            this.numClasses = param.getNumClasses();
            this.classes = param.getClassesList();
            this.treeDim = param.getTreeDim();
            this.learningRate = meta.getLearningRate();

        } catch (Exception ex) {
            ex.printStackTrace();
            return StatusCode.ILLEGALDATA;
        }
        LOGGER.info("Finish init HeteroSecureBoost class");
        return StatusCode.OK;
    }

    protected String getSite(int treeId, int treeNodeId) {
        return this.trees.get(treeId).getTree(treeNodeId).getSitename().split(":", -1)[0];
    }

    protected String generateTag(String caseId, String modelId, int communicationRound) {
        return caseId + "_" + modelId + "_" + String.valueOf(communicationRound);
    }

    protected String[] parseTag(String tag) {

        return tag.split("_");
    }

    protected int gotoNextLevel(int treeId, int treeNodeId, Map<String, Object> input) {
        int nextTreeNodeId;
        int fid = this.trees.get(treeId).getTree(treeNodeId).getFid();
        double splitValue = this.trees.get(treeId).getSplitMaskdict().get(treeNodeId);
        String fidStr = String.valueOf(fid);
        if (input.containsKey(fidStr)) {
            if (Double.parseDouble(input.get(fidStr).toString()) <= splitValue + 1e-20) {
                nextTreeNodeId = this.trees.get(treeId).getTree(treeNodeId).getLeftNodeid();
            } else {
                nextTreeNodeId = this.trees.get(treeId).getTree(treeNodeId).getRightNodeid();
            }
        } else {
            if (this.trees.get(treeId).getMissingDirMaskdict().containsKey(treeNodeId)) {
                int missingDir = this.trees.get(treeId).getMissingDirMaskdict().get(treeNodeId);
                if (missingDir == 1) {
                    nextTreeNodeId = this.trees.get(treeId).getTree(treeNodeId).getRightNodeid();
                } else {
                    nextTreeNodeId = this.trees.get(treeId).getTree(treeNodeId).getLeftNodeid();
                }
            } else {
                nextTreeNodeId = this.trees.get(treeId).getTree(treeNodeId).getRightNodeid();
            }
        }

        return nextTreeNodeId;

    }

    @Override
    public abstract Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams);


}

