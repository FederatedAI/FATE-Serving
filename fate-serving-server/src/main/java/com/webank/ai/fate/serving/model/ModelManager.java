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

package com.webank.ai.fate.serving.model;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.register.common.NamedThreadFactory;
import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.common.bean.BaseContext;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.exceptions.ModelNullException;
import com.webank.ai.fate.serving.core.exceptions.ModelProcessorInitException;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ModelManager implements InitializingBean {

    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;
    @Autowired
    ModelLoaderFactory modelLoaderFactory;
    File serviceIdFile;
    File namespaceFile;
    // old version cache file
    File publishLoadStoreFile;
    File publishOnlineStoreFile;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("ModelService", true));
    private ConcurrentMap<String, String> serviceIdNamespaceMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Model> namespaceMap = new ConcurrentHashMap<String, Model>();
    // (guest) name + namespace -> (host) model
    private ConcurrentMap<String, Model> partnerModelMap = new ConcurrentHashMap<String, Model>();

    private static String[] URL_FILTER_CHARACTER = {"?", ":", "/", "&"};

    public synchronized ModelServiceProto.UnbindResponse unbind(Context context, ModelServiceProto.UnbindRequest req) {
        ModelServiceProto.UnbindResponse.Builder resultBuilder = ModelServiceProto.UnbindResponse.newBuilder();
        List<String> serviceIds = req.getServiceIdsList();
        Preconditions.checkArgument(serviceIds != null && serviceIds.size() != 0, "param service id is blank");
        logger.info("try to unbind model, service id : {}", serviceIds);
        String modelKey = this.getNameSpaceKey(req.getTableName(), req.getNamespace());
        if (!this.namespaceMap.containsKey(modelKey)) {
            logger.error("not found model info table name {} namespace {}, please check if the model is already loaded.", req.getTableName(), req.getNamespace());
            throw new ModelNullException(" found model info, please check if the model is already loaded.");
        }
        Model model = this.namespaceMap.get(modelKey);
        String tableNamekey = this.getNameSpaceKey(model.getTableName(), model.getNamespace());

        serviceIds.forEach(serviceId -> {
            if (!tableNamekey.equals(this.serviceIdNamespaceMap.get(serviceId))) {
                logger.info("unbind request info is error {}", req);
                throw new ModelNullException("unbind request info is error");
            }
        });
        if (zookeeperRegistry != null) {
            Set<URL> registered = zookeeperRegistry.getRegistered();
            List<URL> unRegisterUrls = Lists.newArrayList();
            for (URL url : registered) {
                if (model.getPartId().equalsIgnoreCase(url.getEnvironment()) || serviceIds.contains(url.getEnvironment())) {
                    unRegisterUrls.add(url);
                }
            }
            for (URL url : unRegisterUrls) {
                zookeeperRegistry.unregister(url);
            }
            logger.info("Unregister urls: {}", unRegisterUrls);
        }
        serviceIds.forEach(serviceId -> {
            this.serviceIdNamespaceMap.remove(serviceId);
        });
        this.store(serviceIdNamespaceMap, serviceIdFile);
        logger.info("unbind model success");
        resultBuilder.setStatusCode(StatusCode.SUCCESS);
        return resultBuilder.build();
    }

    public synchronized void store(Map data, File file) {
        executorService.submit(() -> {
            doSaveCache(data, file, 0);
        });

        logger.info("Store model cache success, file path: {}", serviceIdFile.getAbsolutePath());
    }

    public synchronized void store() {
        executorService.submit(() -> {
            doSaveCache(namespaceMap, namespaceFile, 0);
            doSaveCache(serviceIdNamespaceMap, serviceIdFile, 0);
        });

        logger.info("Store model cache success");
    }

    private List<RequestWapper> doLoadOldVersionCache(File file) {
        Map<String, RequestWapper> properties = new HashMap<>(8);
        if (file != null && file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {
                    final AtomicInteger count = new AtomicInteger(0);
                    bufferedReader.lines().forEach(temp -> {
                        count.addAndGet(1);
                        int index = temp.indexOf("=");
                        if (index > 0) {
                            String key = temp.substring(0, index);
                            String value = temp.substring(index + 1);
                            String[] args = value.split(":");
                            String content = args[0];
                            long timestamp = count.longValue();
                            ;
                            if (args.length >= 2) {
                                timestamp = new Long(args[1]);
                            }
                            properties.put(key, new RequestWapper(content, timestamp, key));
                        }
                    });
                }
                if (logger.isInfoEnabled()) {
                    logger.info("load model cache file " + file + ", data: " + properties);
                }

                List<RequestWapper> list = Lists.newArrayList();
                properties.forEach((k, v) -> {
                    list.add(v);
                });
                Collections.sort(list, (o1, o2) -> o1.timestamp - o2.timestamp > 0 ? 1 : -1);
                return list;
            } catch (Throwable e) {
                logger.error("failed to load cache file {} ", file);
            }

        }
        return null;
    }

    private void restoreOldVersionCache() {
        // restore 1.2.x model cache
        if (!namespaceFile.exists() && publishLoadStoreFile.exists()) {
            List<RequestWapper> requestWappers = doLoadOldVersionCache(publishLoadStoreFile);
            if (requestWappers != null && !requestWappers.isEmpty()) {
                requestWappers.forEach((v) -> {
                    try {
                        byte[] data = Base64.getDecoder().decode(v.content.getBytes());
                        ModelServiceProto.PublishRequest req = ModelServiceProto.PublishRequest.parseFrom(data);
                        if (logger.isDebugEnabled()) {
                            logger.debug("restore publishLoadModel req {}", req);
                        }
                        this.load(new BaseContext(), req);
                    } catch (Exception e) {
                        logger.error("restore publishLoadModel error", e);
                        e.printStackTrace();
                    }
                });
            }
            try {
                // Create new cache file after restore
                generateParent(namespaceFile);
                namespaceFile.createNewFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid model cache file " + namespaceFile + ", cause: Failed to create file " + namespaceFile.getAbsolutePath() + "!");
            }
        }

        if (!serviceIdFile.exists() && publishOnlineStoreFile.exists()) {
            List<RequestWapper> requestWappers = doLoadOldVersionCache(publishOnlineStoreFile);
            if (requestWappers != null && !requestWappers.isEmpty()) {
                requestWappers.forEach((v) -> {
                    try {
                        byte[] data = Base64.getDecoder().decode(v.content.getBytes());
                        ModelServiceProto.PublishRequest req = ModelServiceProto.PublishRequest.parseFrom(data);
                        if (logger.isDebugEnabled()) {
                            logger.debug("restore publishOnlineModel req {} base64 {}", req, v);
                        }
                        this.bind(new BaseContext(), req);
                    } catch (Exception e) {
                        logger.error("restore publishOnlineModel error", e);
                        e.printStackTrace();
                    }
                });
            }

            try {
                // Create new cache file after restore
                generateParent(serviceIdFile);
                serviceIdFile.createNewFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid model cache file " + serviceIdFile + ", cause: Failed to create file " + serviceIdFile.getAbsolutePath() + "!");
            }
        }
    }

    public synchronized void restore(Context context) {
        // compatible 1.2.x
        restoreOldVersionCache();
        ConcurrentMap<String, String> tempServiceIdNamespaceMap = new ConcurrentHashMap<>(8);
        ConcurrentMap<String, Model> tempNamespaceMap = new ConcurrentHashMap<>(8);
        doLoadCache(tempNamespaceMap, namespaceFile);
        doLoadCache(tempServiceIdNamespaceMap, serviceIdFile);

        ModelLoader.ModelLoaderParam modelLoaderParam = new ModelLoader.ModelLoaderParam();
        ModelLoader modelLoader = this.modelLoaderFactory.getModelLoader(context, ModelLoader.LoadModelType.FATEFLOW);

        tempNamespaceMap.forEach((k, model) -> {
            try {
                modelLoaderParam.setLoadModelType(ModelLoader.LoadModelType.FATEFLOW);
                modelLoaderParam.setTableName(model.getTableName());
                modelLoaderParam.setNameSpace(model.getNamespace());
                ModelProcessor modelProcessor = modelLoader.restoreModel(context, modelLoaderParam);
                if (modelProcessor != null) {
                    model.setModelProcessor(modelProcessor);
                    if (model.getRole().equals(Dict.GUEST)) {
                        for (Model value : model.getFederationModelMap().values()) {
                            value.setRole(Dict.HOST);
                        }
                    }
                    namespaceMap.put(k, model);
                    if (Dict.HOST.equals(model.getRole())) {
                        model.getFederationModelMap().values().forEach(remoteModel -> {
                            String remoteNamespaceKey = this.getNameSpaceKey(remoteModel.getTableName(), remoteModel.getNamespace());
                            this.partnerModelMap.put(remoteNamespaceKey, model);
                        });
                    }

                    logger.info("restore model {} success ", k);
                }

            } catch (Exception e) {
                logger.info("restore model {} error {} ", k, e.getMessage());
            }

        });

        tempServiceIdNamespaceMap.forEach((k, v) -> {
            if (namespaceMap.get(v) != null) {
                serviceIdNamespaceMap.put(k, v);
            }
        });

        // register service after restore
        if (namespaceMap != null && namespaceMap.size() > 0) {
            List<String> environments = Lists.newArrayList();
            for (Model model : namespaceMap.values()) {
                if (Dict.HOST.equals(model.getRole())) {
                    String modelKey = ModelUtil.genModelKey(model.getTableName(), model.getNamespace());
                    environments.add(EncryptUtils.encrypt(modelKey, EncryptMethod.MD5));
                }
            }
            this.registerService(environments);
        }

        if (serviceIdNamespaceMap != null && serviceIdNamespaceMap.size() > 0) {
            List<String> environments = Lists.newArrayList();
            for (Map.Entry<String, String> modelEntry : serviceIdNamespaceMap.entrySet()) {
                Model model = namespaceMap.get(modelEntry.getValue());
                if (model != null) {
                    environments.add(modelEntry.getKey());
                    //environments.add(model.getPartId());
                }
            }
            this.registerService(environments);
        }

        logger.info("restore model success ");
    }

    private void registerService(Collection environments) {
        if (zookeeperRegistry != null) {
            zookeeperRegistry.addDynamicEnvironment(environments);
            zookeeperRegistry.register(FateServer.serviceSets);
        }
    }

    public synchronized ReturnResult bind(Context context, ModelServiceProto.PublishRequest req) {
        if (logger.isDebugEnabled()) {
            logger.debug("try to bind model, receive request : {}", req);
        }
        ReturnResult returnResult = new ReturnResult();
        String serviceId = req.getServiceId();
        Preconditions.checkArgument(StringUtils.isNotBlank(serviceId), "param service id is blank");
        Preconditions.checkArgument(!StringUtils.containsAny(serviceId, URL_FILTER_CHARACTER), "Service id contains special characters, " + JsonUtil.object2Json(URL_FILTER_CHARACTER));

        returnResult.setRetcode(StatusCode.SUCCESS);
        Model model = this.buildModelForBind(context, req);
        String modelKey = this.getNameSpaceKey(model.getTableName(), model.getNamespace());
        Model loadedModel = this.namespaceMap.get(modelKey);
        if (loadedModel == null) {
            throw new ModelNullException("model " + modelKey + " is not exist ");
        }
        this.serviceIdNamespaceMap.put(serviceId, modelKey);
        if (zookeeperRegistry != null) {
            if (StringUtils.isNotEmpty(serviceId)) {
                zookeeperRegistry.addDynamicEnvironment(serviceId);
            }
            zookeeperRegistry.register(FateServer.serviceSets);
        }
        //update cache
        this.store(serviceIdNamespaceMap, serviceIdFile);
        return returnResult;
    }

    private Model buildModelForBind(Context context, ModelServiceProto.PublishRequest req) {
        Model model = new Model();
        String role = req.getLocal().getRole();
        model.setPartId(req.getLocal().getPartyId());
        model.setRole(Dict.GUEST.equals(role) ? Dict.GUEST : Dict.HOST);
        String serviceId = req.getServiceId();
        model.getServiceIds().add(serviceId);
        Map<String, ModelServiceProto.RoleModelInfo> modelMap = req.getModelMap();
        ModelServiceProto.RoleModelInfo roleModelInfo = modelMap.get(model.getRole());
        Map<String, ModelServiceProto.ModelInfo> modelInfoMap = roleModelInfo.getRoleModelInfoMap();
        Map<String, ModelServiceProto.Party> roleMap = req.getRoleMap();
        ModelServiceProto.Party selfParty = roleMap.get(model.getRole());
        String selfPartyId = selfParty.getPartyIdList().get(0);
        ModelServiceProto.ModelInfo selfModelInfo = modelInfoMap.get(selfPartyId);
        String selfNamespace = selfModelInfo.getNamespace();
        String selfTableName = selfModelInfo.getTableName();
        model.setNamespace(selfNamespace);
        model.setTableName(selfTableName);
        return model;
    }

    private Model buildModelForLoad(Context context, ModelServiceProto.PublishRequest req) {
        Model model = new Model();
        String role = req.getLocal().getRole();
        model.setPartId(req.getLocal().getPartyId());
        model.setRole(Dict.GUEST.equals(role) ? Dict.GUEST : Dict.HOST);
        Map<String, ModelServiceProto.RoleModelInfo> modelMap = req.getModelMap();
        ModelServiceProto.RoleModelInfo roleModelInfo = modelMap.get(model.getRole());
        Map<String, ModelServiceProto.ModelInfo> modelInfoMap = roleModelInfo.getRoleModelInfoMap();
        Map<String, ModelServiceProto.Party> roleMap = req.getRoleMap();
        String remotePartyRole = model.getRole().equals(Dict.GUEST) ? Dict.HOST : Dict.GUEST;
        ModelServiceProto.Party remoteParty = roleMap.get(remotePartyRole);
        String remotePartyId = remoteParty.getPartyIdList().get(0);
        ModelServiceProto.RoleModelInfo remoteRoleModelInfo = modelMap.get(remotePartyRole);
        ModelServiceProto.ModelInfo remoteModelInfo = remoteRoleModelInfo.getRoleModelInfoMap().get(remotePartyId);
        String remoteNamespace = remoteModelInfo.getNamespace();
        String remoteTableName = remoteModelInfo.getTableName();
        Model remoteModel = new Model();
        remoteModel.setPartId(remotePartyId);
        remoteModel.setNamespace(remoteNamespace);
        remoteModel.setTableName(remoteTableName);
        remoteModel.setRole(remotePartyRole);
        model.getFederationModelMap().put(remoteModel.getPartId(), remoteModel);
        ModelServiceProto.Party selfParty = roleMap.get(model.getRole());
        String selfPartyId = selfParty.getPartyIdList().get(0);
        ModelServiceProto.ModelInfo selfModelInfo = modelInfoMap.get(selfPartyId);
        String selfNamespace = selfModelInfo.getNamespace();
        String selfTableName = selfModelInfo.getTableName();
        model.setNamespace(selfNamespace);
        model.setTableName(selfTableName);
        return model;
    }

    public synchronized ReturnResult load(Context context, ModelServiceProto.PublishRequest req) {
        if (logger.isDebugEnabled()) {
            logger.debug("try to load model, receive request : {}", req);
        }
        ReturnResult returnResult = new ReturnResult();
        returnResult.setRetcode(StatusCode.SUCCESS);
        Model model = this.buildModelForLoad(context, req);
        String namespaceKey = this.getNameSpaceKey(model.getTableName(), model.getNamespace());
        ModelLoader.ModelLoaderParam modelLoaderParam = new ModelLoader.ModelLoaderParam();
        String loadType = req.getLoadType();
        if (StringUtils.isNotEmpty(loadType)) {
            modelLoaderParam.setLoadModelType(ModelLoader.LoadModelType.valueOf(loadType));
        } else {
            modelLoaderParam.setLoadModelType(ModelLoader.LoadModelType.FATEFLOW);
        }
        modelLoaderParam.setTableName(model.getTableName());
        modelLoaderParam.setNameSpace(model.getNamespace());
        modelLoaderParam.setFilePath(req.getFilePath());
        ModelLoader modelLoader = this.modelLoaderFactory.getModelLoader(context, modelLoaderParam.getLoadModelType());
        Preconditions.checkArgument(modelLoader != null, "model loader not found");
        ModelProcessor modelProcessor = modelLoader.loadModel(context, modelLoaderParam);
        if (modelProcessor == null) {
            throw new ModelProcessorInitException("model initialization error, please check if the model exists and the configuration of the FATEFLOW load model process is correct.");
        }
        model.setModelProcessor(modelProcessor);
        this.namespaceMap.put(namespaceKey, model);

        if (Dict.HOST.equals(model.getRole())) {
            model.getFederationModelMap().values().forEach(remoteModel -> {
                String remoteNamespaceKey = this.getNameSpaceKey(remoteModel.getTableName(), remoteModel.getNamespace());
                this.partnerModelMap.put(remoteNamespaceKey, model);
            });
        }
        /**
         *  host model
         */
        if (Dict.HOST.equals(model.getRole()) && zookeeperRegistry != null) {
            String modelKey = ModelUtil.genModelKey(model.getTableName(), model.getNamespace());
            zookeeperRegistry.addDynamicEnvironment(EncryptUtils.encrypt(modelKey, EncryptMethod.MD5));
            zookeeperRegistry.register(FateServer.hostServiceSets);
        }
        // update cache
        this.store(namespaceMap, namespaceFile);
        return returnResult;

    }

    public List<Model> queryModel(Context context, ModelServiceProto.QueryModelRequest queryModelRequest) {
        int queryType = queryModelRequest.getQueryType();
        switch (queryType) {
            case 0:
                List<Model> allModels = listAllModel();
                return allModels.stream().map(e -> {
                    Model clone = (Model) e.clone();
                    clone.setModelProcessor(e.getModelProcessor());
                    this.serviceIdNamespaceMap.forEach((k, v) -> {
                        if (clone.getServiceIds() == null) {
                            clone.setServiceIds(Lists.newArrayList());
                        }
                        String nameSpaceKey = this.getNameSpaceKey(clone.getTableName(), clone.getNamespace());
                        if (nameSpaceKey.equals(v)) {
                            clone.getServiceIds().add(k);
                        }
                    });
                    return clone;
                }).collect(Collectors.toList());
            case 1:
                // Fuzzy query
                String serviceId = queryModelRequest.getServiceId();
                return this.namespaceMap.values().stream()
                        .filter(e -> {
                            String nameSpaceKey = this.getNameSpaceKey(e.getTableName(), e.getNamespace());
                            boolean isMatch = false;
                            for (Map.Entry<String, String> entry : this.serviceIdNamespaceMap.entrySet()) {
                                if (entry.getKey().toLowerCase().indexOf(serviceId.toLowerCase()) > -1 && nameSpaceKey.equals(entry.getValue())) {
                                    isMatch = true;
                                    break;
                                }
                            }
                            return isMatch;
                        })
                        .map(e -> {
                            Model clone = (Model) e.clone();
                            if (clone.getServiceIds() == null) {
                                clone.setServiceIds(Lists.newArrayList());
                            }
                            String nameSpaceKey = this.getNameSpaceKey(clone.getTableName(), clone.getNamespace());

                            this.serviceIdNamespaceMap.forEach((k, v) -> {
                                if (v.equals(nameSpaceKey)) {
                                    clone.getServiceIds().add(k);
                                }
                            });
                            return clone;
                        })
                        .collect(Collectors.toList());
            default:
                return null;
        }
    }

    public Model getModelByServiceId(String serviceId) {
        String namespaceKey = serviceIdNamespaceMap.get(serviceId);
        if (namespaceKey == null) {
            throw new ModelNullException("serviceId is not bind model");
        }
        return this.namespaceMap.get(namespaceKey);
    }

    /**
     * 获取所有模型信息
     *
     * @return
     */
    public List<Model> listAllModel() {
        return new ArrayList(this.namespaceMap.values());
    }

    public Model getModelByTableNameAndNamespace(String tableName, String namespace) {
        String key = getNameSpaceKey(tableName, namespace);
        return namespaceMap.get(key);
    }

    private String getNameSpaceKey(String tableName, String namespace) {
        return new StringBuilder().append(tableName).append("_").append(namespace).toString();
    }

    private void clearCache(String name, String namespace) {
        StringBuilder sb = new StringBuilder();
        String locationPre = MetaInfo.PROPERTY_MODEL_CACHE_PATH;
        if (StringUtils.isNotEmpty(locationPre) && StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(namespace)) {
            String cacheFilePath = sb.append(locationPre).append("/.fate/model_").append(name).append("_").append(namespace).append("_").append("cache").toString();
            File cacheFile = new File(cacheFilePath);
            if (cacheFile.exists()) {
                cacheFile.delete();
            }
            File lockFile = new File(cacheFilePath + ".lock");
            if (lockFile.exists()) {
                lockFile.delete();
            }
        }
    }

    public synchronized ModelServiceProto.UnloadResponse unload(Context context, ModelServiceProto.UnloadRequest request) {
        ModelServiceProto.UnloadResponse.Builder resultBuilder = ModelServiceProto.UnloadResponse.newBuilder();
        if (logger.isDebugEnabled()) {
            logger.debug("try to unload model, name: {}, namespace: {}", request.getTableName(), request.getNamespace());
        }
        resultBuilder.setStatusCode(StatusCode.SUCCESS);
        Model model = this.getModelByTableNameAndNamespace(request.getTableName(), request.getNamespace());
        if (model == null) {
            logger.error("not found model info table name {} namespace {}, please check if the model is already loaded.", request.getTableName(), request.getNamespace());
            throw new ModelNullException(" found model info, please check if the model is already loaded.");
        }
        List<String> serviceIds = Lists.newArrayList();
        String nameSpaceKey = this.getNameSpaceKey(model.getTableName(), model.getNamespace());
        serviceIdNamespaceMap.forEach((k, v) -> {
            if (v.equals(nameSpaceKey)) {
                serviceIds.add(k);
            }
        });

        boolean useRegister = MetaInfo.PROPERTY_USE_REGISTER;
        if (useRegister) {
            String modelKey = ModelUtil.genModelKey(model.getTableName(), model.getNamespace());
            modelKey = EncryptUtils.encrypt(modelKey, EncryptMethod.MD5);
            logger.info("Unregister environments: {}", StringUtils.join(modelKey, ",", serviceIds));
            Set<URL> registered = zookeeperRegistry.getRegistered();
            List<URL> unRegisterUrls = Lists.newArrayList();
            if (Dict.HOST.equals(model.getRole())) {
                for (URL url : registered) {
                    if (modelKey.equalsIgnoreCase(url.getEnvironment()) || serviceIds.contains(url.getEnvironment())) {
                        unRegisterUrls.add(url);
                    }
                }
            } else if (Dict.GUEST.equals(model.getRole())) {
                for (URL url : registered) {
                    if (model.getPartId().equalsIgnoreCase(url.getEnvironment()) || serviceIds.contains(url.getEnvironment())) {
                        unRegisterUrls.add(url);
                    }
                }
            }
            for (URL url : unRegisterUrls) {
                zookeeperRegistry.unregister(url);
            }
            logger.info("unregister urls: {}", unRegisterUrls);
        }
        this.namespaceMap.remove(nameSpaceKey);
        serviceIds.forEach(serviceId -> {
            this.serviceIdNamespaceMap.remove(serviceId);
        });
        logger.info("unload model success");
        this.store();
        this.clearCache(model.getTableName(), model.getNamespace());
        return resultBuilder.build();
    }

    public void doSaveCache(Map data, File file, long version) {
        if (file == null) {
            logger.error("save cache file error , file is null");
            return;
        }
        // Save
        try {
            File lockfile = new File(file.getAbsolutePath() + ".lock");
            if (!lockfile.exists()) {
                lockfile.createNewFile();
            }
            try (RandomAccessFile raf = new RandomAccessFile(lockfile, "rw");
                 FileChannel channel = raf.getChannel()) {
                FileLock lock = channel.tryLock();
                if (lock == null) {
                    throw new IOException("can not lock the model cache file " + file.getAbsolutePath() + ", ignore and retry later, maybe multi java process use the file");
                }
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    try (FileOutputStream outputFile = new FileOutputStream(file)) {
                        byte[] serialize = SerializationUtils.serialize(data);
                        outputFile.write(serialize);
                    }
                } finally {
                    lock.release();
                }
            }
            logger.info("save cache file {} success", file.getAbsolutePath());
        } catch (Throwable e) {
            logger.error("Failed to save model cache file, will retry, cause: " + e.getMessage(), e);
        }
    }

    private void doLoadCache(Map data, File file) {
        if (file != null && file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                Long length = file.length();
                byte[] bytes = new byte[length.intValue()];
                int readCount = in.read(bytes);
                if (readCount > 0) {
                    data.clear();
                    ConcurrentMap deserialize = (ConcurrentMap) SerializationUtils.deserialize(bytes);
                    data.putAll(deserialize);
                }
            } catch (Throwable e) {
                logger.error("failed to doLoadCache file {}", file, e);
            }
        }
    }

    private void generateParent(File file) {
        if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IllegalArgumentException("Invalid model cache file " + file + ", cause: Failed to create directory " + file.getParentFile() + "!");
            }
        }
    }

    public Model getPartnerModel(String tableName, String namespace) {
        return this.partnerModelMap.get(getNameSpaceKey(tableName, namespace));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String locationPre = MetaInfo.PROPERTY_MODEL_CACHE_PATH;
        logger.info("try to find model cache {}", locationPre);
        if (StringUtils.isNotEmpty(locationPre)) {
            // new version
            String loadModelStoreFileName = locationPre + "/.fate/loadModelStore.cache";
            String bindModelStoreFileName = locationPre + "/.fate/bindModelStore.cache";
            namespaceFile = new File(loadModelStoreFileName);
            generateParent(namespaceFile);
            serviceIdFile = new File(bindModelStoreFileName);
            generateParent(serviceIdFile);
            // compatible 1.2.x
            locationPre = System.getProperty(Dict.PROPERTY_USER_HOME);
            String publishLoadFileName = locationPre + "/.fate/publishLoadStore.cache";
            String publishOnlineFileName = locationPre + "/.fate/publishOnlineStore.cache";
            publishLoadStoreFile = new File(publishLoadFileName);
            publishOnlineStoreFile = new File(publishOnlineFileName);
        }
    }

    private static class RequestWapper {
        String content;
        long timestamp;
        String md5;

        public RequestWapper(String content, long timestamp, String md5) {
            this.content = content;
            this.timestamp = timestamp;
            this.md5 = md5;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getMd5() {
            return md5;
        }

        public void setMd5(String md5) {
            this.md5 = md5;
        }

        @Override
        public String toString() {
            return content + ":" + timestamp;
        }
    }

}
