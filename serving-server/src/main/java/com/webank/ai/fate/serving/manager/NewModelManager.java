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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.register.common.NamedThreadFactory;
import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class NewModelManager implements InitializingBean, EnvironmentAware {
    @Autowired
    private ModelLoader modelLoader;

    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;

    Environment environment;

    private ConcurrentMap<String, String> serviceIdNamespaceMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Model> namespaceMap = new ConcurrentHashMap<String, Model>();

    File serviceIdFile;
    File namespaceFile;

    // old version cache file
    File publishLoadStoreFile;
    File publishOnlineStoreFile;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(), new NamedThreadFactory("ModelService", true));

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
    //        {
//            role: "guest"
//            partyId: "9999"
//        }
//        role {
//            key: "guest"
//            value {
//                partyId: "9999"
//            }
//        }
//        role {
//            key: "arbiter"
//            value {
//                partyId: "10000"
//            }
//        }
//        role {
//            key: "host"
//            value {
//                partyId: "10000"
//            }
//        }
//        model {
//            key: "host"
//            value {
//                roleModelInfo {
//                    key: "10000"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "host#10000#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }
//        model {
//            key: "guest"
//            value {
//                roleModelInfo {
//                    key: "9999"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "guest#9999#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }
//        model {
//            key: "arbiter"
//            value {
//                roleModelInfo {
//                    key: "10000"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "arbiter#10000#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }

    public synchronized ReturnResult unbind(Context context, ModelServiceProto.PublishRequest req) {
        String serviceId = req.getServiceId();
        Preconditions.checkArgument(serviceId != null);

        if (logger.isDebugEnabled()) {
            logger.debug("Try to unbind model, service id : {}", serviceId);
        }

        ReturnResult returnResult = new ReturnResult();
        returnResult.setRetcode(InferenceRetCode.OK);

        Model model = this.buildModel(context, req);
        String modelKey = this.getNameSpaceKey(model.getTableName(), model.getNamespace());

        if (!this.namespaceMap.containsKey(modelKey)) {
            logger.info("Not found model info, please check if the model is already loaded.");
            returnResult.setRetmsg("Not found model info, please check if the model is already loaded.");
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            return returnResult;
        }

        if (!this.serviceIdNamespaceMap.containsKey(serviceId)) {
            logger.info("Service ID: {} not bind", serviceId);
            returnResult.setRetmsg("Service ID not bind");
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            return returnResult;
        }

        // unregister
        Set<URL> registered = zookeeperRegistry.getRegistered();
        List<URL> unRegisterUrls = Lists.newArrayList();

        for (URL url : registered) {
            if (model.getPartId().equalsIgnoreCase(url.getEnvironment()) || serviceId.equalsIgnoreCase(url.getEnvironment())) {
                unRegisterUrls.add(url);
            }
        }

        for (URL url : unRegisterUrls) {
            zookeeperRegistry.unregister(url);
        }

        logger.info("Unregister urls: {}", unRegisterUrls);

        this.serviceIdNamespaceMap.remove(serviceId);
        // update cache
        this.store(serviceIdNamespaceMap, serviceIdFile);
        logger.info("Unbind model success");
        return returnResult;
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
        Map<String, RequestWapper> properties = new HashMap<>();
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
                    logger.info("Load model cache file " + file + ", data: " + properties);
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

    public synchronized void restore() {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to restore model cache");
        }

        // compatible 1.2.x
        restoreOldVersionCache();

        doLoadCache(namespaceMap, namespaceFile);
        doLoadCache(serviceIdNamespaceMap, serviceIdFile);

        // register service after restore
        if (namespaceMap != null && namespaceMap.size() > 0) {
            List<String> environments = Lists.newArrayList();
            for (Model model : namespaceMap.values()) {
                if (Dict.HOST.equals(model.getRole())) {
                    if (StringUtils.isNotEmpty(model.getServiceId())) {
                        environments.add(model.getServiceId());
                    }
                    String modelKey = ModelUtil.genModelKey(model.getTableName(), model.getNamespace());
                    environments.add(EncryptUtils.encrypt(modelKey, EncryptMethod.MD5));
                }
            }
            this.registerService(environments);
        }

        if (serviceIdNamespaceMap != null && serviceIdNamespaceMap.size() > 0) {
            List<String> environments = Lists.newArrayList();
            for (String modelKey : serviceIdNamespaceMap.values()) {
                Model model = namespaceMap.get(modelKey);
                if (StringUtils.isNotEmpty(model.getServiceId())) {
                    environments.add(model.getServiceId());
                }
                environments.add(model.getPartId());
            }
            this.registerService(environments);
        }

        logger.info("Restore model cache success");
    }

    private void registerService(Collection environments) {
        zookeeperRegistry.addDynamicEnvironment(environments);
        zookeeperRegistry.register(FateServer.serviceSets);
    }

    public synchronized ReturnResult bind(Context context, ModelServiceProto.PublishRequest req) {
        ReturnResult returnResult = new ReturnResult();
        returnResult.setRetcode(InferenceRetCode.OK);
        Model model = this.buildModel(context, req);

        String serviceId = req.getServiceId();

        String modelKey = this.getNameSpaceKey(model.getTableName(), model.getNamespace());
        Model loadedModel = this.namespaceMap.get(modelKey);
        if (loadedModel == null) {
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            returnResult.setRetmsg("Can not found model by these information.");
            return returnResult;
        }

        this.serviceIdNamespaceMap.put(serviceId, modelKey);
        if (StringUtils.isNotEmpty(serviceId)) {
            zookeeperRegistry.addDynamicEnvironment(serviceId);
        }
        zookeeperRegistry.addDynamicEnvironment(model.getPartId());
        zookeeperRegistry.register(FateServer.serviceSets);

        //update cache
        this.store(serviceIdNamespaceMap, serviceIdFile);

        return returnResult;
    }

    private Model buildModel(Context context, ModelServiceProto.PublishRequest req) {
        Model model = new Model();
        String role = req.getLocal().getRole();
        model.setPartId(req.getLocal().getPartyId());
        model.setRole(Dict.GUEST.equals(role) ? Dict.GUEST : Dict.HOST);
        String serviceId = req.getServiceId();
        model.setServiceId(serviceId);
        Map<String, ModelServiceProto.RoleModelInfo> modelMap = req.getModelMap();
        ModelServiceProto.RoleModelInfo roleModelInfo = modelMap.get(model.getRole().toString());
        Map<String, ModelServiceProto.ModelInfo> modelInfoMap = roleModelInfo.getRoleModelInfoMap();
        Map<String, ModelServiceProto.Party> roleMap = req.getRoleMap();

        if (model.getRole().equals(Dict.GUEST)) {

            ModelServiceProto.Party hostParty = roleMap.get(Dict.HOST);
            String hostPartyId = hostParty.getPartyIdList().get(0);
            ModelServiceProto.ModelInfo hostModelInfo = modelInfoMap.get(hostPartyId);
            String hostNamespace = hostModelInfo.getNamespace();
            String hostTableName = hostModelInfo.getTableName();
            Model hostModel = new Model();
            hostModel.setPartId(hostPartyId);
            hostModel.setNamespace(hostNamespace);
            hostModel.setTableName(hostTableName);
            model.setFederationModel(hostModel);

        }
        ModelServiceProto.Party selfParty = roleMap.get(model.getRole().toString());
        String selfPartyId = selfParty.getPartyIdList().get(0);
        ModelServiceProto.ModelInfo selfModelInfo = modelInfoMap.get(selfPartyId);
        String selfNamespace = selfModelInfo.getNamespace();
        String selfTableName = selfModelInfo.getTableName();
        model.setNamespace(selfNamespace);
        model.setTableName(selfTableName);
        return model;
    }

    ;

    public synchronized ReturnResult load(Context context, ModelServiceProto.PublishRequest req) {
        ReturnResult returnResult = new ReturnResult();
        returnResult.setRetcode(InferenceRetCode.OK);

        Model model = this.buildModel(context, req);
        String namespaceKey = this.getNameSpaceKey(model.getTableName(), model.getNamespace());
        ModelProcessor modelProcessor = modelLoader.loadModel(context, model.getTableName(), model.getNamespace());
//        Preconditions.checkArgument(modelProcessor!=null);
        if (modelProcessor == null) {
            returnResult.setRetmsg("load model failed");
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            return returnResult;
        }
        model.setModelProcessor(modelProcessor);
        //this.modelSet.add(model);
        this.namespaceMap.put(namespaceKey, model);

        if (Dict.HOST.equals(model.getRole())) {
            if (zookeeperRegistry != null) {
                if (StringUtils.isNotEmpty(model.getServiceId())) {
                    zookeeperRegistry.addDynamicEnvironment(model.getServiceId());
                }
                String modelKey = ModelUtil.genModelKey(model.getTableName(), model.getNamespace());
                zookeeperRegistry.addDynamicEnvironment(EncryptUtils.encrypt(modelKey, EncryptMethod.MD5));
                zookeeperRegistry.register(FateServer.serviceSets);
            }
        }

        // update cache
        this.store(namespaceMap, namespaceFile);

        return returnResult;
    }

    public Model getModelByServiceId(String serviceId) {
        String namespaceKey = serviceIdNamespaceMap.get(serviceId);
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

    public synchronized ReturnResult unload(String tableName, String namespace) {
        if (logger.isDebugEnabled()) {
            logger.debug("try to unload model, name: {}, namespace: {}", tableName, namespace);
        }

        ReturnResult returnResult = new ReturnResult();
        returnResult.setRetcode(InferenceRetCode.OK);

        Model model = this.getModelByTableNameAndNamespace(tableName, namespace);
//        Preconditions.checkArgument(model != null);
        if (model == null) {
            logger.info("model not loaded");
            returnResult.setRetcode(InferenceRetCode.LOAD_MODEL_FAILED);
            returnResult.setRetmsg("model not loaded");
            return returnResult;
        }

        // unregister serviceId, name, namespace
        String serviceId = model.getServiceId();
        boolean useRegister = this.environment.getProperty(Dict.USE_REGISTER, Boolean.class, Boolean.TRUE);
        if (useRegister) {
            String modelKey = ModelUtil.genModelKey(model.getTableName(), model.getNamespace());
            modelKey = EncryptUtils.encrypt(modelKey, EncryptMethod.MD5);

            logger.info("Unregister environments: {}", StringUtils.join(modelKey, ",", serviceId));

            Set<URL> registered = zookeeperRegistry.getRegistered();
            List<URL> unRegisterUrls = Lists.newArrayList();
            if (Dict.HOST.equals(model.getRole())) {
                for (URL url : registered) {
                    if (modelKey.equalsIgnoreCase(url.getEnvironment()) || serviceId.equalsIgnoreCase(url.getEnvironment())) {
                        unRegisterUrls.add(url);
                    }
                }
            } else if (Dict.GUEST.equals(model.getRole())) {
                for (URL url : registered) {
                    if (model.getPartId().equalsIgnoreCase(url.getEnvironment()) || serviceId.equalsIgnoreCase(url.getEnvironment())) {
                        unRegisterUrls.add(url);
                    }
                }
            }

            for (URL url : unRegisterUrls) {
                zookeeperRegistry.unregister(url);
            }

            logger.info("Unregister urls: {}", unRegisterUrls);
        }

        this.namespaceMap.remove(getNameSpaceKey(tableName, namespace));
        this.serviceIdNamespaceMap.remove(serviceId);

        logger.info("Unload model success");

        // update store
        this.store();

        return returnResult;
    }

    public void doSaveCache(Map data, File file, long version) {
        if (file == null) {
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
                logger.error("failed to doLoadCache file ", e);
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

    @Override
    public void afterPropertiesSet() throws Exception {
        String locationPre = System.getProperty(Dict.PROPERTY_USER_HOME);
        if (StringUtils.isNotEmpty(locationPre)) {
            // new version
            String loadModelStoreFileName = locationPre + "/.fate/loadModelStore.cache";
            String bindModelStoreFileName = locationPre + "/.fate/bindModelStore.cache";

            namespaceFile = new File(loadModelStoreFileName);
            generateParent(namespaceFile);

            serviceIdFile = new File(bindModelStoreFileName);
            generateParent(serviceIdFile);

            // compatible 1.2.x
            String publishLoadFileName = locationPre + "/.fate/publishLoadStore.cache";
            String publishOnlineFileName = locationPre + "/.fate/publishOnlineStore.cache";
            publishLoadStoreFile = new File(publishLoadFileName);
            publishOnlineStoreFile = new File(publishOnlineFileName);
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

}
