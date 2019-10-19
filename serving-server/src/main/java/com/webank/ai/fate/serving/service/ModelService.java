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

package com.webank.ai.fate.serving.service;

import ch.ethz.ssh2.crypto.Base64;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceGrpc;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.PublishRequest;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto.PublishResponse;
import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.core.utils.ObjectTransform;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.serving.core.bean.*;
import com.webank.ai.fate.serving.interfaces.ModelManager;
import com.webank.ai.fate.serving.manger.ModelUtils;
import io.grpc.stub.StreamObserver;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ModelService extends ModelServiceGrpc.ModelServiceImplBase implements InitializingBean {
    private static final Logger logger = LogManager.getLogger();
    @Autowired
    ModelManager modelManager;


    LinkedHashMap<String, String> publishLoadReqMap = new LinkedHashMap();
    LinkedHashMap<String, String> publicOnlineReqMap = new LinkedHashMap();
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    //    ConcurrentMap<String,PublishRequest> publishLoadReqMap =new ConcurrentHashMap<>();
//    ConcurrentMap<String,PublishRequest> publicOnlineReqMap =new  ConcurrentHashMap();
    File publishLoadStoreFile;
    File publishOnlineStoreFile;

    public ModelService() {

        String locationPre = System.getProperty(Dict.PROPERTY_USER_HOME);
        if (StringUtils.isNotEmpty(locationPre)) {
            String publishLoadFileName = locationPre + "/.fate/publishLoadStore.cache";
            String publishOnlineFileName = locationPre + "/.fate/publishOnlineStore.cache";

            publishLoadStoreFile = new File(publishLoadFileName);
            if (!publishLoadStoreFile.exists() && publishLoadStoreFile.getParentFile() != null && !publishLoadStoreFile.getParentFile().exists()) {
                if (!publishLoadStoreFile.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid model cache file " + publishLoadStoreFile + ", cause: Failed to create directory " + publishLoadStoreFile.getParentFile() + "!");
                }
            }
            publishOnlineStoreFile = new File(publishOnlineFileName);
            if (!publishOnlineStoreFile.exists() && publishOnlineStoreFile.getParentFile() != null && !publishOnlineStoreFile.getParentFile().exists()) {
                if (!publishOnlineStoreFile.getParentFile().mkdirs()) {
                    throw new IllegalArgumentException("Invalid model cache file " + publishOnlineStoreFile + ", cause: Failed to create directory " + publishOnlineStoreFile.getParentFile() + "!");
                }
            }

        }

    }

    @Override
    @RegisterService(serviceName = "publishLoad")
    public synchronized void publishLoad(PublishRequest req, StreamObserver<PublishResponse> responseStreamObserver) {

        Context context = new BaseContext(new BaseLoggerPrinter());
        context.setActionType(ModelActionType.MODEL_LOAD.name());
        context.preProcess();
        ReturnResult returnResult = null;

        try {
            PublishResponse.Builder builder = PublishResponse.newBuilder();
            returnResult = modelManager.publishLoadModel(
                    new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
                    ModelUtils.getFederatedRoles(req.getRoleMap()),
                    ModelUtils.getFederatedRolesModel(req.getModelMap()));
            builder.setStatusCode(returnResult.getRetcode())
                    .setMessage(returnResult.getRetmsg())
                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult.getData()).getBytes()));
            builder.setStatusCode(returnResult.getRetcode());

            if (returnResult.getRetcode() == 0) {

                String key = Md5Crypt.md5Crypt(req.toByteArray());

                publishLoadReqMap.put(key, new String(Base64.encode(req.toByteArray())));

                fireStoreEvent();
            }
            responseStreamObserver.onNext(builder.build());
            responseStreamObserver.onCompleted();
        } finally {
            context.postProcess(req, returnResult);
        }
    }

    @Override
    @RegisterService(serviceName = "publishOnline")
    public synchronized void publishOnline(PublishRequest req, StreamObserver<PublishResponse> responseStreamObserver) {
        Context context = new BaseContext(new BaseLoggerPrinter());
        context.setActionType(ModelActionType.MODEL_PUBLISH_ONLINE.name());
        context.preProcess();
        ReturnResult returnResult = null;
        try {
            PublishResponse.Builder builder = PublishResponse.newBuilder();
            returnResult = modelManager.publishOnlineModel(
                    new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
                    ModelUtils.getFederatedRoles(req.getRoleMap()),
                    ModelUtils.getFederatedRolesModel(req.getModelMap())
            );
            builder.setStatusCode(returnResult.getRetcode())
                    .setMessage(returnResult.getRetmsg())
                    .setData(ByteString.copyFrom(ObjectTransform.bean2Json(returnResult.getData()).getBytes()));
            if (returnResult.getRetcode() == 0) {

                String content = new String(Base64.encode(req.toByteArray()));
                String key = Md5Crypt.md5Crypt(content.getBytes());

                publicOnlineReqMap.put(key, content);
                fireStoreEvent();
            }
            responseStreamObserver.onNext(builder.build());
            responseStreamObserver.onCompleted();
        } finally {
            context.postProcess(req, returnResult);
        }
    }

    public void fireStoreEvent() {

        executorService.submit(() -> {

            store();

        });
    }


    public void restore() {

    }


    public void store() {

        doSaveProperties(publishLoadReqMap, publishLoadStoreFile, 0);
        doSaveProperties(publicOnlineReqMap, publishOnlineStoreFile, 0);

    }


    public void doSaveProperties(Map properties, File file, long version) {
        logger.info("prepare to save modelinfo {} {}", file, properties);

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
                    throw new IOException("Can not lock the registry cache file " + file.getAbsolutePath() + ", ignore and retry later, maybe multi java process use the file");
                }
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    try (FileOutputStream outputFile = new FileOutputStream(file)) {
                        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputFile, Charset.forName("UTF-8")))) {
                            bufferedWriter.newLine();
                            publishLoadReqMap.forEach((k, v) -> {

                                try {

                                    String content = k + "=" + v;
                                    logger.info("write content {}", content);
                                    bufferedWriter.write(content);
                                    bufferedWriter.newLine();
                                } catch (IOException e) {
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
//            savePropertiesRetryTimes.incrementAndGet();
//            if (savePropertiesRetryTimes.get() >= MAX_RETRY_TIMES_SAVE_PROPERTIES) {
//                logger.warn("Failed to save registry cache file after retrying " + MAX_RETRY_TIMES_SAVE_PROPERTIES + " times, cause: " + e.getMessage(), e);
//                savePropertiesRetryTimes.set(0);
//                return;
//            }
//            if (version < lastCacheChanged.get()) {
//                savePropertiesRetryTimes.set(0);
//                return;
//            } else {
//                registryCacheExecutor.execute(new AbstractRegistry.SaveProperties(lastCacheChanged.incrementAndGet()));
//            }
            logger.error("Failed to save model cache file, will retry, cause: " + e.getMessage(), e);
        }
    }

    private void loadProperties(File file, Map properties) {


        if (file != null && file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                //properties.load(in);
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in))) {


                    bufferedReader.lines().forEach(temp -> {
                        int index = temp.indexOf("=");
                        if (index > 0) {
                            String key = temp.substring(0, index);
                            String value = temp.substring(index + 1);
                            properties.put(key, value);
                        }
                    });
                }


                if (logger.isInfoEnabled()) {
                    logger.info("Load model cache file " + file + ", data: " + properties);
                }
            } catch (Throwable e) {
                logger.error("failed to load cache file {} ", file);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {

        loadProperties(publishLoadStoreFile, publishLoadReqMap);
        loadProperties(publishOnlineStoreFile, publicOnlineReqMap);
        publishLoadReqMap.forEach((k, v) -> {
            try {
                byte[] data = Base64.decode(v.toString().toCharArray());
                PublishRequest req = PublishRequest.parseFrom(data);
                logger.info("resotre publishLoadModel req {}", req);
                modelManager.publishLoadModel(
                        new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
                        ModelUtils.getFederatedRoles(req.getRoleMap()),
                        ModelUtils.getFederatedRolesModel(req.getModelMap()));
            } catch (Exception e) {
                logger.error("restore publishLoadModel error", e);
                e.printStackTrace();
            }
        });
        publicOnlineReqMap.forEach((k, v) -> {
            try {
                byte[] data = Base64.decode(v.toString().toCharArray());
                PublishRequest req = PublishRequest.parseFrom(data);

                logger.info("resotre publishOnlineModel req {}", req);
                modelManager.publishOnlineModel(
                        new FederatedParty(req.getLocal().getRole(), req.getLocal().getPartyId()),
                        ModelUtils.getFederatedRoles(req.getRoleMap()),
                        ModelUtils.getFederatedRolesModel(req.getModelMap()));
            } catch (Exception e) {
                logger.error("restore publishOnlineModel error", e);
                e.printStackTrace();
            }

        });


    }
}
