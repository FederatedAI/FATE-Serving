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

import com.webank.ai.fate.serving.core.bean.Dict;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DSLParser {
    private static final Logger logger = LoggerFactory.getLogger(DSLParser.class);
    private HashMap<String, String> componentModuleMap = new HashMap<String, String>(8);
    private HashMap<String, Integer> componentIds = new HashMap<String, Integer>(8);
    private HashMap<String, List<String>> downStream = new HashMap<String, List<String>>(8);
    private HashMap<Integer, HashSet<Integer>> upInputs = new HashMap<Integer, HashSet<Integer>>(8);
    private ArrayList<String> topoRankComponent = new ArrayList<String>();
    private String modelPackage = "com.webank.ai.fate.serving.federatedml.model";

    public int parseDagFromDSL(String jsonStr) {
        logger.info("start parse dag from dsl");
        try {
            JSONObject dsl = new JSONObject(jsonStr);
            JSONObject components = dsl.getJSONObject(Dict.DSL_COMPONENTS);

            logger.info("start topo sort");
            topoSort(components, this.topoRankComponent);

            logger.info("components size is {}", this.topoRankComponent.size());
            for (int i = 0; i < this.topoRankComponent.size(); ++i) {
                this.componentIds.put(this.topoRankComponent.get(i), i);
            }

            for (int i = 0; i < topoRankComponent.size(); ++i) {
                String componentName = topoRankComponent.get(i);
                logger.info("component is {}", componentName);
                JSONObject component = components.getJSONObject(componentName);
                String[] codePath = ((String) component.get(Dict.DSL_CODE_PATH)).split("/", -1);
                logger.info("code path splits is {}", codePath);
                String module = codePath[codePath.length - 1];
                logger.info("module is {}", module);
                componentModuleMap.put(componentName, module);

                JSONObject upData = null;
                if (component.has(Dict.DSL_INPUT) && component.getJSONObject(Dict.DSL_INPUT).has(Dict.DSL_DATA)) {
                    upData = component.getJSONObject(Dict.DSL_INPUT).getJSONObject(Dict.DSL_DATA);
                }
                if (upData != null) {
                    int componentId = this.componentIds.get(componentName);
                    Iterator<String> dataKeyIterator = upData.keys();
                    while (dataKeyIterator.hasNext()) {
                        String dataKey = dataKeyIterator.next();
                        JSONArray data = upData.getJSONArray(dataKey);
                        for (int j = 0; j < data.length(); j++) {
                            String upComponent = data.getString(j).split("\\.", -1)[0];
                            if (!upInputs.containsKey(componentId)) {
                                upInputs.put(componentId, new HashSet<Integer>());
                            }

                            if (upComponent.equals(Dict.DSL_ARGS)) {
                                upInputs.get(componentId).add(-1);
                            } else {
                                upInputs.get(componentId).add(this.componentIds.get(upComponent));
                            }
                        }

                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("DSLParser init catch error:{}", ex);
        }
        logger.info("Finish init DSLParser");
        return 0;
    }

    public void topoSort(JSONObject components, ArrayList<String> topoRankComponent) {
        Stack<Integer> stk = new Stack();
        HashMap<String, Integer> componentIndexMapping = new HashMap<String, Integer>(8);
        ArrayList<String> componentList = new ArrayList<String>();
        int index = 0;
        Iterator<String> componentNames = components.keys();

        while (componentNames.hasNext()) {
            String componentName = componentNames.next();
            componentIndexMapping.put(componentName, index);
            ++index;
            componentList.add(componentName);
        }

        int[] inDegree = new int[index];
        HashMap<Integer, ArrayList<Integer>> edges = new HashMap<Integer, ArrayList<Integer>>(8);

        for (int i = 0; i < componentList.size(); ++i) {
            String componentName = componentList.get(i);
            JSONObject component = components.getJSONObject(componentName);
            JSONObject upData = null;
            if (component.has(Dict.DSL_INPUT) && component.getJSONObject(Dict.DSL_INPUT).has(Dict.DSL_DATA)) {
                upData = component.getJSONObject(Dict.DSL_INPUT).getJSONObject(Dict.DSL_DATA);
            }
            Integer componentId = componentIndexMapping.get(componentName);
            if (upData != null) {
                Iterator<String> dataKeyIterator = upData.keys();
                while (dataKeyIterator.hasNext()) {
                    String dataKey = dataKeyIterator.next();
                    JSONArray data = upData.getJSONArray(dataKey);
                    for (int j = 0; j < data.length(); j++) {
                        String upComponent = data.getString(j).split("\\.", -1)[0];
                        int upComponentId = -1;
                        if (!"args".equals(upComponent)) {
                            upComponentId = componentIndexMapping.get(upComponent);
                        }

                        if (upComponentId != -1) {
                            if (!edges.containsKey(upComponentId)) {
                                edges.put(upComponentId, new ArrayList<Integer>());
                            }
                            inDegree[componentId]++;
                            edges.get(upComponentId).add(componentId);
                        }
                    }
                }
            }
        }

        logger.info("end of construct edges");
        for (int i = 0; i < index; i++) {
            if (inDegree[i] == 0) {
                stk.push(i);
            }
        }

        while (!stk.empty()) {
            Integer vertex = stk.pop();
            topoRankComponent.add(componentList.get(vertex));
            ArrayList<Integer> adjV = edges.get(vertex);
            if (adjV == null) {
                continue;
            }
            for (int i = 0; i < adjV.size(); ++i) {
                Integer downV = adjV.get(i);
                --inDegree[downV];
                if (inDegree[downV] == 0) {
                    stk.push(downV);
                }
            }
        }

        logger.info("end of topo");
    }

    public HashMap<String, String> getComponentModuleMap() {
        return this.componentModuleMap;
    }

    public ArrayList<String> getAllComponent() {
        return this.topoRankComponent;
    }

    public HashSet<Integer> getUpInputComponents(int idx) {
        if (this.upInputs.containsKey(idx)) {
            return this.upInputs.get(idx);
        } else {
            return null;
        }
    }

}
