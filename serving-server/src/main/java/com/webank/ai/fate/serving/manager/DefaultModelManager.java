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

package com.webank.ai.fate.serving.manager;


import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.bean.ModelNamespaceData;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.federatedml.PipelineTask;
import com.webank.ai.fate.serving.interfaces.ModelCache;
import com.webank.ai.fate.serving.interfaces.ModelManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DefaultModelManager implements ModelManager, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(DefaultModelManager.class);
    private final AtomicLong lastCacheChanged = new AtomicLong();
    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;
    private Map<String, String> appNamespaceMap;
    private Map<String, FederatedParty> modelFederatedParty;
    private Map<String, FederatedRoles> modelFederatedRoles;
    private Map<String, ModelNamespaceData> modelNamespaceDataMap;
    private ReentrantReadWriteMapPool<String, String> appNamespaceMapPool;
    private ReentrantReadWriteMapPool<String, ModelNamespaceData> modelNamespaceDataMapPool;
    @Autowired
    private ModelCache modelCache;
    private ConcurrentHashMap<String, ModelInfo> partnerModelData;
    private File modelFile;
    @Autowired
    private ModelLoader modelLoader;


    public DefaultModelManager() {

        appNamespaceMap = new HashMap<>();
        modelNamespaceDataMap = new HashMap<>();
        appNamespaceMapPool = new ReentrantReadWriteMapPool<>(appNamespaceMap);
        modelNamespaceDataMapPool = new ReentrantReadWriteMapPool<>(modelNamespaceDataMap);
        partnerModelData = new ConcurrentHashMap<>();
        modelFederatedParty = new HashMap<>();
        modelFederatedRoles = new HashMap<>();

        String filename = System.getProperty(Dict.PROPERTY_USER_HOME) + "/.fate/fate-model.cache";
        File file = null;
        if (StringUtils.isNotEmpty(filename)) {
            file = new File(filename);
            if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid model cache file " + file + ", cause: Failed to create directory " + file.getParentFile() + "!");
                }
            }
        }
        this.modelFile = file;
    }

    @Override
    public ReturnResult publishLoadModel(Context context, FederatedParty federatedParty, FederatedRoles federatedRoles, Map<String, Map<String, ModelInfo>> federatedRolesModel) {
        String role = federatedParty.getRole();
        String partyId = federatedParty.getPartyId();
        String serviceId = null;
        if (context.getData(Dict.SERVICE_ID) != null) {
            serviceId = context.getData(Dict.SERVICE_ID).toString();
        }

        ReturnResult returnResult = new ReturnResult();
        returnResult.setRetcode(InferenceRetCode.OK);
        try {
            ModelInfo modelInfo;
            if (federatedRolesModel.containsKey(role) && federatedRolesModel.get(role).containsKey(partyId)) {
                modelInfo = federatedRolesModel.get(role).get(partyId);
            } else {
                modelInfo = null;
            }
            if (modelInfo == null) {
                returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
                return returnResult;
            }

            PipelineTask model = pushModelIntoPool(context, modelInfo.getName(), modelInfo.getNamespace());
            if (model == null) {
                returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
                return returnResult;
            }

            federatedRolesModel.forEach((roleName, roleModelInfo) -> {
                roleModelInfo.forEach((p, m) -> {
                    if (!p.equals(partyId) || (p.equals(partyId) && !role.equals(roleName))) {
                        String partnerModelKey = ModelUtil.genModelKey(m.getName(), m.getNamespace());
                        partnerModelData.put(partnerModelKey, modelInfo);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Create model index({}) for partner({}, {})", partnerModelKey, roleName, p);
                        }
                    }
                });
            });

            if (Dict.HOST.equals(role)) {
                if (zookeeperRegistry != null) {
                    if (StringUtils.isNotEmpty(serviceId)) {
                        zookeeperRegistry.addDynamicEnvironment(serviceId);
                    }
                    partnerModelData.forEach((key, v) -> {
                        String keyMd5 = EncryptUtils.encrypt(key, EncryptMethod.MD5);
                        if (logger.isDebugEnabled()) {
                            logger.debug("transform key {} to md5key {}", key, keyMd5);
                        }
                        zookeeperRegistry.addDynamicEnvironment(keyMd5);
                    });
                    zookeeperRegistry.register(FateServer.serviceSets);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("load the model successfully");
            }
            return returnResult;
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
            returnResult.setRetcode(InferenceRetCode.SYSTEM_ERROR);
        }
        return returnResult;
    }

    @Override
    public ReturnResult publishOnlineModel(Context context, FederatedParty federatedParty, FederatedRoles federatedRoles, Map<String, Map<String, ModelInfo>> federatedRolesModel) {
        String role = federatedParty.getRole();
        String partyId = federatedParty.getPartyId();
        String serviceId = null;

        if (context.getData(Dict.SERVICE_ID) != null && StringUtils.isNotEmpty(context.getData(Dict.SERVICE_ID).toString().trim())) {
            serviceId = context.getData(Dict.SERVICE_ID).toString();
        } else {
            logger.info("service id is null");
        }
        ReturnResult returnResult = new ReturnResult();
        ModelInfo modelInfo = federatedRolesModel.get(role).get(partyId);
        if (modelInfo == null) {
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            returnResult.setRetmsg("No model for me.");
            return returnResult;
        }

        String modelKey = ModelUtil.genModelKey(modelInfo.getName(), modelInfo.getNamespace());
        PipelineTask model = modelCache.get(context, modelKey);
        if (model == null) {
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            returnResult.setRetmsg("Can not found model by these information.");
            return returnResult;
        }
        modelFederatedParty.put(modelKey, federatedParty);

        modelFederatedRoles.put(modelKey, federatedRoles);


        try {
            String modelNamespace = modelInfo.getNamespace();
            String modelName = modelInfo.getName();
            modelNamespaceDataMapPool.put(modelNamespace, new ModelNamespaceData(modelNamespace, federatedParty, federatedRoles, modelName, model));
            //appNamespaceMapPool.put(partyId, modelNamespace);
            if (StringUtils.isNotEmpty(serviceId)) {
                logger.info("put serviceId {} input pool", serviceId);
                appNamespaceMapPool.put(serviceId, modelNamespace+":"+modelName);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Enable model {} for namespace {} success", modelName, modelNamespace);
                logger.debug("Get model namespace {} for app {}", modelNamespace, partyId);
            }
            returnResult.setRetcode(InferenceRetCode.OK);
            if (zookeeperRegistry != null) {
                if (StringUtils.isNotEmpty(serviceId)) {
                    zookeeperRegistry.addDynamicEnvironment(serviceId);
                }
                zookeeperRegistry.addDynamicEnvironment(partyId);
                zookeeperRegistry.register(FateServer.serviceSets);
            }
//            zookeeperRegistry.register();
        } catch (Exception ex) {
            returnResult.setRetcode(InferenceRetCode.SYSTEM_ERROR);
            returnResult.setRetmsg(ex.getMessage());
        }
        return returnResult;
    }

    @Override
    public PipelineTask getModel(Context context, String name, String namespace) {
        return modelCache.get(context, ModelUtil.genModelKey(name, namespace));
    }

    @Override
    public ModelNamespaceData getModelNamespaceData(Context context, String namespace) {
        return modelNamespaceDataMapPool.get(namespace);
    }

    @Override
    public String getModelNamespaceByPartyId(Context context, String partyId) {
        return appNamespaceMapPool.get(partyId);
    }

    @Override
    public ModelInfo getModelInfoByPartner(Context context, String partnerModelName, String partnerModelNamespace) {
        return partnerModelData.get(ModelUtil.genModelKey(partnerModelName, partnerModelNamespace));
    }

    @Override
    public PipelineTask pushModelIntoPool(Context context, String name, String namespace) {
        PipelineTask model = modelLoader.loadModel(context, name, namespace);
        if (model != null) {
            modelCache.put(context, ModelUtil.genModelKey(name, namespace), model);
            logger.info("load model success, name: {}, namespace: {}, model cache size is {}", name, namespace, modelCache.getSize());
        }
        return model;
    }

    private FederatedRoles parseFederatedRoles(Map data) {
        return null;
    }

    private FederatedParty parseFederatedParty(Map data) {
        return null;
    }

    private ModelInfo parseModelInfo(Map data) {
        return null;
    }

    private ModelNamespaceData parseModelNamespaceData(Map data) {
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
