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

package com.webank.ai.fate.serving.federatedml;

import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.core.constant.StatusCode;
import com.webank.ai.fate.core.mlmodel.buffer.PipelineProto;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import com.webank.ai.fate.serving.federatedml.model.BaseModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class PipelineTask {
    private static final Logger LOGGER = LogManager.getLogger();
    private List<BaseModel> pipeLineNode = new ArrayList<>();
    private Map<String, BaseModel> modelMap = new HashMap<String, BaseModel>();
    private DSLParser dslParser = new DSLParser();
    private String modelPackage = "com.webank.ai.fate.serving.federatedml.model";

    public BaseModel getModelByComponentName(String name) {
        return this.modelMap.get(name);
    }

    public int initModel(Map<String, byte[]> modelProtoMap) {
        LOGGER.info("start init Pipeline");
        try {
            Map<String, byte[]> newModelProtoMap = changeModelProto(modelProtoMap);
            String pipelineProtoName = "pipeline.pipeline:Pipeline";
            PipelineProto.Pipeline pipeLineProto = PipelineProto.Pipeline.parseFrom(newModelProtoMap.get(pipelineProtoName));
            String dsl = pipeLineProto.getInferenceDsl().toStringUtf8(); //inference_dsl;
            dslParser.parseDagFromDSL(dsl);
            ArrayList<String> components = dslParser.getAllComponent();
            HashMap<String, String> componentModuleMap = dslParser.getComponentModuleMap();

            for (int i = 0; i < components.size(); ++i) {
                String componentName = components.get(i);
                String className = componentModuleMap.get(componentName);
                LOGGER.info("Start get className:{}", className);
                try {
                    Class modelClass = Class.forName(this.modelPackage + "." + className);
                    BaseModel mlNode = (BaseModel) modelClass.getConstructor().newInstance();
                    mlNode.setComponentName(componentName);
                    byte[] protoMeta = newModelProtoMap.get(componentName + ".Meta");
                    byte[] protoParam = newModelProtoMap.get(componentName + ".Param");
                    mlNode.initModel(protoMeta, protoParam);
                    modelMap.put(componentName, mlNode);
                    pipeLineNode.add(mlNode);
                    LOGGER.info(" Add class {} to pipeline task list", className);
                } catch (Exception ex) {
                    pipeLineNode.add(null);
                    LOGGER.warn("Can not instance {} class", className);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            LOGGER.info("Pipeline init catch error:{}", ex);
        }
        LOGGER.info("Finish init Pipeline");
        return StatusCode.OK;
    }


    public Map<String, Object> predict(Context context, Map<String, Object> inputData, FederatedParams predictParams) {
        LOGGER.info("Start Pipeline predict use {} model node.", this.pipeLineNode.size());
        List<Map<String, Object>> outputData = new ArrayList<>();
        for (int i = 0; i < this.pipeLineNode.size(); i++) {
            if (this.pipeLineNode.get(i) != null) {
                LOGGER.info("component class is {}", this.pipeLineNode.get(i).getClass().getName());
            } else {
                LOGGER.info("component class is {}", this.pipeLineNode.get(i));
            }
            List<Map<String, Object>> inputs = new ArrayList<>();
            HashSet<Integer> upInputComponents = this.dslParser.getUpInputComponents(i);
            if (upInputComponents != null) {
                Iterator<Integer> iters = upInputComponents.iterator();
                while (iters.hasNext()) {
                    Integer upInput = iters.next();
                    if (upInput == -1) {
                        inputs.add(inputData);
                    } else {
                        inputs.add(outputData.get(upInput));
                    }
                }
            } else {
                inputs.add(inputData);
            }
            if (this.pipeLineNode.get(i) != null) {
                outputData.add(this.pipeLineNode.get(i).handlePredict(context, inputs, predictParams));
            } else {
                outputData.add(inputs.get(0));
            }

        }
        ReturnResult federatedResult = context.getFederatedResult();
        if (federatedResult != null) {
            inputData.put(Dict.RET_CODE, federatedResult.getRetcode());
        }

        LOGGER.info("Finish Pipeline predict");
        return outputData.get(outputData.size() - 1);
    }

    private HashMap<String, byte[]> changeModelProto(Map<String, byte[]> modelProtoMap) {
        HashMap<String, byte[]> newModelProtoMap = new HashMap<String, byte[]>();
        for (Map.Entry<String, byte[]> entry : modelProtoMap.entrySet()) {
            String key = entry.getKey();
            if (!"pipeline.pipeline:Pipeline".equals(key)) {
                String[] componentNameSegments = key.split("\\.", -1);
                if (componentNameSegments.length != 2) {
                    newModelProtoMap.put(entry.getKey(), entry.getValue());
                    continue;
                }

                if (componentNameSegments[1].endsWith("Meta")) {
                    newModelProtoMap.put(componentNameSegments[0] + ".Meta", entry.getValue());
                } else if (componentNameSegments[1].endsWith("Param")) {
                    newModelProtoMap.put(componentNameSegments[0] + ".Param", entry.getValue());
                }
            } else {
                newModelProtoMap.put(entry.getKey(), entry.getValue());
            }
        }

        return newModelProtoMap;
    }
}
