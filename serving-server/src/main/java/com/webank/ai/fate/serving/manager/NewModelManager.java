package com.webank.ai.fate.serving.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.register.provider.FateServer;
import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.utils.EncryptUtils;
import com.webank.ai.fate.serving.service.ModelService;
import io.netty.handler.codec.base64.Base64;
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
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

    java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
    java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();

    File serviceIdFile;

    File namespaceFile;

    Logger logger = LoggerFactory.getLogger(this.getClass());
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


    public synchronized int unbind(Context context, ModelServiceProto.PublishRequest req) {
        String serviceId = req.getServiceId();
        Preconditions.checkArgument(serviceId != null);

        if (logger.isDebugEnabled()) {
            logger.debug("Try to unbind model, service id : {}", serviceId);
        }

        Model model = this.buildModel(context, req);
        String modelKey = this.getNameSpaceKey(model.getTableName(), model.getNamespace());

        if (!this.namespaceMap.containsKey(modelKey)) {
            logger.info("Not found model info, please check if the model is already loaded.");
            return 1;
        }

        if (!this.serviceIdNamespaceMap.containsKey(serviceId)) {
            logger.info("Service ID: {} not bind", serviceId);
            return 1;
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
        return 0;
    }

//    public synchronized  int unload(Context context,String tableName,String  namespace){
//
//
//
//
//    }

    public synchronized void store(Map data, File file) {
        doSaveCache(data, file, 0);
        logger.info("Store model cache success, file path: {}", serviceIdFile.getAbsolutePath());
    }

    public synchronized void store() {
//         for test
//        Model  model =  new  Model();
//        model.setPartId("10000");
//        model.setRole(Dict.HOST);
//        model.setServiceId("test02");
//        model.setTableName("202003021604");
//        model.setNamespace("9999#guest#10000#host");
//        String  namespaceKey =   this.getNameSpaceKey(model.getTableName(),model.getNamespace());
//        namespaceMap.put(namespaceKey, model);
//        serviceIdNamespaceMap.put("test02", namespaceKey);
        // ==========
        doSaveCache(namespaceMap, namespaceFile, 0);
        doSaveCache(serviceIdNamespaceMap, serviceIdFile, 0);

        logger.info("Store model cache success");
    }

    public synchronized void restore() {
        if (logger.isDebugEnabled()) {
            logger.debug("Try to restore model cache");
        }

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

    public synchronized  int bind(Context context,ModelServiceProto.PublishRequest    req){

        Model  model = this.buildModel(context,req);

        String serviceId = req.getServiceId();

        String modelKey = this.getNameSpaceKey(model.getTableName(),model.getNamespace());

        this.serviceIdNamespaceMap.put(serviceId,modelKey);

        return 0;

    }


    private  Model  buildModel(Context  context, ModelServiceProto.PublishRequest    req){
        Model  model =  new  Model();
        String  role = req.getLocal().getRole();
        model.setPartId(req.getLocal().getPartyId());
        model.setRole(Dict.GUEST.equals(role)?Dict.GUEST:Dict.HOST);
        String  serviceId =req.getServiceId();
        model.setServiceId(serviceId);
        Map<String ,ModelServiceProto.RoleModelInfo> modelMap = req.getModelMap();
        ModelServiceProto.RoleModelInfo roleModelInfo= modelMap.get(model.getRole().toString());
        Map<String,ModelServiceProto.ModelInfo> modelInfoMap=  roleModelInfo.getRoleModelInfoMap();
        Map<String, ModelServiceProto.Party> roleMap = req.getRoleMap();

        if(model.getRole().equals(Dict.GUEST)) {

            ModelServiceProto.Party hostParty = roleMap.get(Dict.HOST);
            String  hostPartyId = hostParty.getPartyIdList().get(0);
            ModelServiceProto.ModelInfo hostModelInfo = modelInfoMap.get(hostPartyId);
            String  hostNamespace = hostModelInfo.getNamespace();
            String  hostTableName  = hostModelInfo.getTableName();
            Model  hostModel =  new  Model();
            hostModel.setPartId(hostPartyId);
            hostModel.setNamespace(hostNamespace);
            hostModel.setTableName(hostTableName);
            model.setFederationModel(hostModel);

        }
        ModelServiceProto.Party selfParty  = roleMap.get(model.getRole().toString());
        String selfPartyId = selfParty.getPartyIdList().get(0);
        ModelServiceProto.ModelInfo selfModelInfo = modelInfoMap.get(selfPartyId);
        String selfNamespace = selfModelInfo.getNamespace();
        String selfTableName = selfModelInfo.getTableName();
        model.setNamespace(selfNamespace);
        model.setTableName(selfTableName);
        return  model;
    };

    public  synchronized boolean load(Context context , ModelServiceProto.PublishRequest    req){
        Model  model = this.buildModel(context,req);
        String  namespaceKey =   this.getNameSpaceKey(model.getTableName(),model.getNamespace());
        ModelProcessor  modelProcessor = modelLoader.loadModel(context,model.getTableName(),model.getNamespace());
        Preconditions.checkArgument(modelProcessor!=null);
        model.setModelProcessor(modelProcessor);
        //this.modelSet.add(model);
        this.namespaceMap.put(namespaceKey,model);
        return true;

    }


    public Model  getModelByServiceId(String serviceId){


        String  namespaceKey =  serviceIdNamespaceMap.get(serviceId);
        return this.namespaceMap.get(namespaceKey);
    }

    /**
     * 获取所有模型信息
     * @return
     */
    List<Model>  listAllModel(){
        return  new ArrayList(this.namespaceMap.values());
    }

    public Model getModelByTableNameAndNamespace(String  tableName ,String  namespace){
        String key =  getNameSpaceKey(tableName, namespace);
        return namespaceMap.get(key);
    }


    private  String getNameSpaceKey (String tableName,String namespace ){

        return  new  StringBuilder().append(tableName).append("_").append(namespace).toString();
    }

    public synchronized void unload(String tableName, String namespace) {
        if (logger.isDebugEnabled()) {
            logger.debug("try to unload model, name: {}, namespace: {}", tableName, namespace);
        }

        Model model = this.getModelByTableNameAndNamespace(tableName, namespace);
        Preconditions.checkArgument(model != null);

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
    }

//    public void store() {
//
//        doSaveProperties(namespaceMap, namespaceFile, 0);
//        doSaveProperties(serviceIdNamespaceMap, serviceIdFile, 0);
//
//    }


    /*public void doSaveProperties(Map data, File file, long version) {

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
                        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputFile, Charset.forName("UTF-8")))) {
                            bufferedWriter.newLine();
                          //  List<ModelService.RequestWapper> sortedList = sortRequest(data);
//                            sortedList.forEach(( v) -> {
//                                try {
//                                    String content = v.md5 + "=" + v.toString();
//                                    bufferedWriter.write(content);
//                                    bufferedWriter.newLine();
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                    logger.error("write mode file error", e);
//                                }
//
//
//                            });

                            data.forEach((k,v)->{
                                try{
                                    StringBuilder  sb = new StringBuilder();
                                    String lineContent = sb.append(k.toString()).append("=").append(encoder.encode(v.toString().getBytes())).toString();
                                    bufferedWriter.write(lineContent);
                                    bufferedWriter.newLine();
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                    logger.error("write mode file error", e);
                                }



                            });

                        }


                    }
                } finally {
                    lock.release();
                }
            }
        } catch (Throwable e) {
            logger.error("Failed to save model cache file, will retry, cause: " + e.getMessage(), e);
        }
    }*/

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

    @Override
    public void afterPropertiesSet() throws Exception {
        String locationPre = System.getProperty(Dict.PROPERTY_USER_HOME);
        if (StringUtils.isNotEmpty(locationPre)) {
//            String publishLoadFileName = locationPre + "/.fate/publishLoadStore.cache";
//            String publishOnlineFileName = locationPre + "/.fate/publishOnlineStore.cache";
            String loadModelStoreFileName = locationPre + "/.fate/loadModelStore.cache";
            String bindModelStoreFileName = locationPre + "/.fate/bindModelStore.cache";

            namespaceFile = new File(loadModelStoreFileName);
            if (!namespaceFile.exists() && namespaceFile.getParentFile() != null && !namespaceFile.getParentFile().exists()) {
                if (!namespaceFile.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid model cache file " + namespaceFile + ", cause: Failed to create directory " + namespaceFile.getParentFile() + "!");
                }
            }
            serviceIdFile = new File(bindModelStoreFileName);
            if (!serviceIdFile.exists() && serviceIdFile.getParentFile() != null && !serviceIdFile.getParentFile().exists()) {
                if (!serviceIdFile.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid model cache file " + serviceIdFile + ", cause: Failed to create directory " + serviceIdFile.getParentFile() + "!");
                }
            }

        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

//    private List<ModelService.RequestWapper> loadProperties(File file, Map<String,ModelService.RequestWapper> properties) {
//
//        if (file != null && file.exists()) {
//            InputStream in = null;
//            try {
//                in = new FileInputStream(file);
//                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {
//                    final AtomicInteger count= new AtomicInteger(0);
//
//                    bufferedReader.lines().forEach(temp -> {
//                        count.addAndGet(1);
//                        int index = temp.indexOf("=");
//                        if (index > 0) {
//                            String key = temp.substring(0, index);
//                            String value = temp.substring(index + 1);
//                            String[] args =value.split(":");
//                            String content = args[0];
//                            long timestamp = count.longValue();;
//                            if(args.length>=2){
//                                timestamp =  new Long(args[1]);
//                            }
//                            properties.put(key, new ModelService.RequestWapper(content,timestamp,key));
//                        }
//                    });
//                }
//                if (logger.isInfoEnabled()) {
//                    logger.info("Load model cache file " + file + ", data: " + properties);
//                }
//                return  sortRequest(properties);
//            } catch (Throwable e) {
//                logger.error("failed to load cache file {} ", file);
//            } finally {
//                if (in != null) {
//                    try {
//                        in.close();
//                    } catch (IOException e) {
//                        logger.warn(e.getMessage(), e);
//                    }
//                }
//            }
//
//        }
//        return null;
//    }


//    public  void  restore(){
//
//        List<ModelService.RequestWapper> publishLoadList = loadProperties(publishLoadStoreFile, publishLoadReqMap);
//        List<ModelService.RequestWapper> publishOnlineList = loadProperties(publishOnlineStoreFile, publicOnlineReqMap);
//        if(publishLoadList!=null) {
//            publishLoadList.forEach((v) -> {
//                try {
//                    byte[] data = decoder.decode(v.content.getBytes());
//                    ModelServiceProto.PublishRequest req = ModelServiceProto.PublishRequest.parseFrom(data);
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("restore publishLoadModel req {}", req);
//                    }
//                    Context context = new BaseContext();
//                    context.putData(Dict.SERVICE_ID, req.getServiceId());
//                    modelManager.publishLoadModel(context,
//                            new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
//                            ModelUtil.getFederatedRoles(req.getRoleMap()),
//                            ModelUtil.getFederatedRolesModel(req.getModelMap()));
//                } catch (Exception e) {
//                    logger.error("restore publishLoadModel error", e);
//                    e.printStackTrace();
//                }
//            });
//        }
//        if(publishOnlineList!=null) {
//            publishOnlineList.forEach((v) -> {
//                try {
//                    byte[] data = decoder.decode(v.content.getBytes());
//                    ModelServiceProto.PublishRequest req = ModelServiceProto.PublishRequest.parseFrom(data);
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("restore publishOnlineModel req {} base64 {}", req, v);
//                    }
//                    Context context = new BaseContext();
//                    context.putData(Dict.SERVICE_ID, req.getServiceId());
//                    modelManager.publishOnlineModel(context,
//                            new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
//                            ModelUtil.getFederatedRoles(req.getRoleMap()),
//                            ModelUtil.getFederatedRolesModel(req.getModelMap()));
//                } catch (Exception e) {
//                    logger.error("restore publishOnlineModel error", e);
//                    e.printStackTrace();
//                }
//
//            });
//        }
//    }
}
