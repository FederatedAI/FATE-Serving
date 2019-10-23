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

package com.webank.ai.fate.serving.manger;

import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.bean.ModelNamespaceData;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.FederatedParty;
import com.webank.ai.fate.serving.core.bean.FederatedRoles;
import com.webank.ai.fate.serving.core.bean.ModelInfo;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.federatedml.PipelineTask;
import com.webank.ai.fate.serving.interfaces.ModelCache;
import com.webank.ai.fate.serving.interfaces.ModelManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private final Logger logger = LogManager.getLogger();
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

    public static void main(String[] args) {

        URL serviceUrl = URL.valueOf("grpc://" + "127.0.0.1" + ":" + 1235 + Constants.PATH_SEPARATOR + "kkkkk");


    }

    @Override
    public ReturnResult publishLoadModel(FederatedParty federatedParty, FederatedRoles federatedRoles, Map<String, Map<String, ModelInfo>> federatedRolesModel) {
        String role = federatedParty.getRole();
        String partyId = federatedParty.getPartyId();
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
            PipelineTask model = pushModelIntoPool(modelInfo.getName(), modelInfo.getNamespace());
            if (model == null) {
                returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
                return returnResult;
            }
            federatedRolesModel.forEach((roleName, roleModelInfo) -> {
                roleModelInfo.forEach((p, m) -> {
                    if (!p.equals(partyId) || (p.equals(partyId) && !role.equals(roleName))) {
                        String partnerModelKey = ModelUtils.genModelKey(m.getName(), m.getNamespace());
                        partnerModelData.put(partnerModelKey, modelInfo);
                        logger.info("Create model index({}) for partner({}, {})", partnerModelKey, roleName, p);
                    }
                });
            });
            logger.info("load the model successfully");
            return returnResult;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            returnResult.setRetcode(InferenceRetCode.SYSTEM_ERROR);
        }
        return returnResult;
    }

    @Override
    public ReturnResult publishOnlineModel(FederatedParty federatedParty, FederatedRoles federatedRoles, Map<String, Map<String, ModelInfo>> federatedRolesModel) {
        String role = federatedParty.getRole();
        String partyId = federatedParty.getPartyId();
        ReturnResult returnResult = new ReturnResult();
        ModelInfo modelInfo = federatedRolesModel.get(role).get(partyId);
        if (modelInfo == null) {
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            returnResult.setRetmsg("No model for me.");
            return returnResult;
        }

        String modelKey = ModelUtils.genModelKey(modelInfo.getName(), modelInfo.getNamespace());
        PipelineTask model = modelCache.get(modelKey);
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
            appNamespaceMapPool.put(partyId, modelNamespace);
            logger.info("Enable model {} for namespace {} success", modelName, modelNamespace);
            logger.info("Get model namespace {} for app {}", modelNamespace, partyId);
            returnResult.setRetcode(InferenceRetCode.OK);
            if (zookeeperRegistry != null) {
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
    public PipelineTask getModel(String name, String namespace) {
        return modelCache.get(ModelUtils.genModelKey(name, namespace));
    }

    @Override
    public ModelNamespaceData getModelNamespaceData(String namespace) {
        return modelNamespaceDataMapPool.get(namespace);
    }

    @Override
    public String getModelNamespaceByPartyId(String partyId) {
        return appNamespaceMapPool.get(partyId);
    }

    @Override
    public ModelInfo getModelInfoByPartner(String partnerModelName, String partnerModelNamespace) {
        return partnerModelData.get(ModelUtils.genModelKey(partnerModelName, partnerModelNamespace));
    }


//
//    @Override
//    public  void  store(){
//
//        Map  storeData = Maps.newHashMap();
//        Set<String> keys =modelCache.getKeys();
//
//        if(keys.size()>0){
//
//            storeData.put(Dict.MODEL_KEYS,keys);
//
//           // storeData.put(Dict.MODEL_NANESPACE_DATA,modelNamespaceDataMapPool.getDataMap());
//
//
////            modelFederatedParty.put(modelKey,federatedParty);
////
////            modelFederatedRoles.put(modelKey,federatedRoles);
//            storeData.put(Dict.MODEL_FEDERATED_ROLES,this.modelFederatedRoles);
//
//            storeData.put(Dict.MODEL_FEDERATED_PARTY,this.modelFederatedParty);
//
//            storeData.put(Dict.APPID_NANESPACE_DATA,appNamespaceMapPool.getDataMap());
//
//            storeData.put(Dict.PARTNER_MODEL_DATA,partnerModelData);
//
//            String  content = JSON.toJSONString(storeData);
//
//            long version = lastCacheChanged.incrementAndGet();
//
//            if (version < lastCacheChanged.get()) {
//                return;
//            }
//            if (modelFile == null) {
//                return;
//            }
//            // Save
//            try {
//                File lockfile = new File(modelFile.getAbsolutePath() + ".lock");
//                if (!lockfile.exists()) {
//                    lockfile.createNewFile();
//                }
//                try (RandomAccessFile raf = new RandomAccessFile(lockfile, "rw");
//                     FileChannel channel = raf.getChannel()) {
//                    FileLock lock = channel.tryLock();
//                    if (lock == null) {
//                        throw new IOException("Can not lock the cache file " + modelFile.getAbsolutePath() );
//                    }
//                    try {
//                        if (!modelFile.exists()) {
//                            modelFile.createNewFile();
//                        }
//                        try (FileOutputStream outputFile = new FileOutputStream(modelFile)) {
//                            if(StringUtils.isNotEmpty(content)) {
//                                outputFile.write(content.getBytes());
//                            }
//
//                        }
//                    } finally {
//                        lock.release();
//                    }
//                }
//            } catch (Throwable e) {
//
//                logger.error("Failed to save modelFile cache file, cause: " + e.getMessage(), e);
//            }
//
//
//
//        }
//
//    }
//
//    @Override
//    public   void restore() {
//
//        try {
//            File lockfile = new File(modelFile.getAbsolutePath() + ".lock");
//            if (!lockfile.exists()) {
//                lockfile.createNewFile();
//            }
//            try (RandomAccessFile raf = new RandomAccessFile(lockfile, "rw");
//                 FileChannel channel = raf.getChannel()) {
//                FileLock lock = channel.tryLock();
//                if (lock == null) {
//                    throw new IOException("Can not lock the  cache file " + modelFile.getAbsolutePath() );
//                }
//                long length = modelFile.length();
//                if(length==0){
//                    return ;
//                }
//
//                byte[]  content =  new  byte[(int)length];
//
//                try {
//
//                    try (FileInputStream inputputFile = new FileInputStream(modelFile)) {
//
//                        inputputFile.read(content);
//
//                    }
//                    Map  contentMap = JSON.parseObject(content,Map.class);
//
//                    Map  appNamespaceData = (Map)contentMap.get(Dict.APPID_NANESPACE_DATA);
//
//                    List<String> modelKeys   =   (List)contentMap.get(Dict.MODEL_KEYS);
//
//
//
//                    if(CollectionUtils.isEmpty(modelKeys)){
//
//                        return ;
//
//                    }
//
//                    for(String  modelKey:modelKeys){
//
//                        String[]  modelKeyElements = ModelUtils.splitModelKey(modelKey);
//
//                        String name = modelKeyElements[0];
//
//                        String namespace =  modelKeyElements[1];
//
//                        logger.info("restore model name {} namespace {}",name,namespace);
//
//                        pushModelIntoPool(name,namespace);
//
//                    }
//
//
//                    this.appNamespaceMapPool.putAll(appNamespaceData);
//
//                    Map partModelData = (Map) contentMap.get(Dict.PARTNER_MODEL_DATA);
//
//                    if(partModelData!=null){
//
//                        partModelData.forEach((k,v)->{
//
//                            try{
//                                partnerModelData.put(k.toString(),parseModelInfo((Map)v));
//
//                            }
//                            catch(Throwable e){
//                                logger.error("set partnerModelData error" ,e);
//                            }
//
//                        });
//
//                        }
//
//
//
//                     Map  modelParty= (Map)contentMap.get(Dict.MODEL_FEDERATED_PARTY);
//
//                    if(modelParty!=null){
//
//                        modelParty.forEach((k,v)->{
//
//                            try{
//                                modelFederatedParty.put(k.toString(),parseFederatedParty((Map)v));
//
//                            }
//                            catch(Throwable e){
//                                logger.error("set partnerModelData error" ,e);
//                            }
//
//                        });
//
//                    }
//
//
//                    Map  modelRoles= (Map)contentMap.get(Dict.MODEL_FEDERATED_ROLES);
//
//                    if(modelRoles!=null){
//
//                        modelRoles.forEach((k,v)->{
//
//                            try{
//                                modelFederatedRoles.put(k.toString(),parseFederatedRoles((Map)v));
//
//                            }
//                            catch(Throwable e){
//                                logger.error("set partnerModelData error" ,e);
//                            }
//
//                        });
//
//                    }
//
//
//
//                   // modelNamespaceDataMapPool.put(modelNamespace, new ModelNamespaceData(modelNamespace, federatedParty, federatedRoles, modelName, model));
//
//
//                if(partnerModelData.size()>0){
//
//                        partnerModelData.forEach((k,v)->{
//
//                            String  namespace =v.getNamespace();
//
//                            FederatedParty  federatedParty =  modelFederatedParty.get(k);
//
//                            FederatedRoles  federatedRoles =  modelFederatedRoles.get(k);
//
//                            PipelineTask model = modelCache.get(k);
//                            if(federatedParty==null||federatedRoles==null||model==null){
//                                return;
//                            }
//
//                            ModelNamespaceData  modelNamespaceData = new  ModelNamespaceData(v.getNamespace(),federatedParty,federatedRoles,v.getName(),model);
//
//                            modelNamespaceDataMapPool.put(namespace,modelNamespaceData);
//
//                        });
//
//                }
//
//
//
//
//
//
//
////                    Map modeNamespaceDataMap = (Map) contentMap.get(Dict.MODEL_NANESPACE_DATA);
////
////                    if(modeNamespaceDataMap!=null){
////
////
////                        modeNamespaceDataMap.forEach((k,v)->{
////
////                            try {
////                                modelNamespaceDataMapPool.put(k.toString(), parseModelNamespaceData((Map) v));
////                            }catch (Throwable e){
////
////                                logger.error("set modelNamespaceDataMapPool error" ,e);
////                            }
////
////
////                        });
////
////                    }
//
//
//
//                } finally {
//                    lock.release();
//                }
//            }
//        } catch (Throwable e) {
//
//            logger.error("Failed to save modelFile cache file, cause: " + e.getMessage(), e);
//        }
//
//    }

    @Override
    public PipelineTask pushModelIntoPool(String name, String namespace) {
        PipelineTask model = ModelUtils.loadModel(name, namespace);
        if (model == null) {
            return null;
        }
        modelCache.put(ModelUtils.genModelKey(name, namespace), model);
        logger.info("Load model success, name: {}, namespace: {}, model cache size is {}", name, namespace, modelCache.getSize());
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

//    private  void  test(){
//
//        storeData.put(Dict.MODEL_KEYS,keys);
//
//        // storeData.put(Dict.MODEL_NANESPACE_DATA,modelNamespaceDataMapPool.getDataMap());
//
//
////            modelFederatedParty.put(modelKey,federatedParty);
////
////            modelFederatedRoles.put(modelKey,federatedRoles);
//        storeData.put(Dict.MODEL_FEDERATED_ROLES,this.modelFederatedRoles);
//
//        storeData.put(Dict.MODEL_FEDERATED_PARTY,this.modelFederatedParty);
//
//        storeData.put(Dict.APPID_NANESPACE_DATA,appNamespaceMapPool.getDataMap());
//
//        storeData.put(Dict.PARTNER_MODEL_DATA,partnerModelData);
//
//
//
//
//    }

    private ModelNamespaceData parseModelNamespaceData(Map data) {


        return null;

    }

    @Override
    public void afterPropertiesSet() throws Exception {

        //  test();


    }
}
