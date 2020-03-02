package com.webank.ai.fate.serving.manager;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.service.ModelService;
import io.netty.handler.codec.base64.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class NewModelManager   {
        @Autowired
        private ModelLoader  modelLoader;
        private ConcurrentMap<String,String>  serviceIdNamespaceMap =  new ConcurrentHashMap<>();
        private ConcurrentMap<String,Model>  namespaceMap = new ConcurrentHashMap<String,Model> ();

        java.util.Base64.Encoder  encoder = java.util.Base64.getEncoder();
        java.util.Base64.Decoder  decoder = java.util.Base64.getDecoder();

        File  serviceIdFile;

        File  namespaceFile;

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



//    public synchronized  int unbind(Context context,ModelServiceProto.PublishRequest    req){
//
//    }
//
//
//    public synchronized  int unload(Context context,String tableName,String  namespace){
//
//
//
//
//    }


//    public synchronized  void  store(){
//
//
//
//    }
//
//    public  synchronized   void  restore(){
//
//
//    }


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

    Model getModelByTableNameAndNamespace(String  tableName ,String  namespace){
        String key =  getNameSpaceKey(tableName, namespace);
        return namespaceMap.get(key);
    }


    private  String getNameSpaceKey (String tableName,String namespace ){

        return  new  StringBuilder().append(tableName).append("_").append(namespace).toString();
    }

    public  void unload(String tableName,String namespace){

        Model  model =this.getModelByTableNameAndNamespace(tableName, namespace);
        Preconditions.checkArgument(model!=null);


    }

    public void store() {

        doSaveProperties(namespaceMap, namespaceFile, 0);
        doSaveProperties(serviceIdNamespaceMap, serviceIdFile, 0);

    }


    public void doSaveProperties(Map data, File file, long version) {

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
