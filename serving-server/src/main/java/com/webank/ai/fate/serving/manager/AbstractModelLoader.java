package com.webank.ai.fate.serving.manager;


import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ModelPipeline;
import com.webank.ai.fate.serving.core.exceptions.ModelSerializeException;
import com.webank.ai.fate.serving.core.model.ModelProcessor;
import com.webank.ai.fate.serving.federatedml.PipelineTask;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public abstract class AbstractModelLoader<MODELDATA> implements ModelLoader {

    Logger logger = LoggerFactory.getLogger(AbstractModelLoader.class);

    @Override
    public ModelProcessor loadModel(Context context, String name, String namespace) {
        MODELDATA modelData = doLoadModel(context, name, namespace);
        if (modelData == null) {
            logger.info("load model error, name {} namespace {} ,try to restore from local cache", name, namespace);
            modelData = restore(context, name, namespace);
            if (modelData == null) {
                logger.info("load model from local cache error, name {} namespace {}", name, namespace);
            }
        } else {
            this.store(context, name, namespace, modelData);
        }
        return this.initPipeLine(context, modelData);
    }

    protected void store(Context context, String name, String namespace, MODELDATA data) {
        try {
            String cachePath = getCachePath(context, name, namespace);
            if (cachePath != null) {
                byte[] bytes = this.serialize(context, data);
                if (bytes == null) {
                    throw new ModelSerializeException();
                }
                File file = new File(cachePath);
                this.doStore(context, bytes, file);
            }
        } catch (ModelSerializeException e) {
            logger.error("serialize mode data error", e);
        } catch (Exception e) {
            logger.error("store mode data error", e);
        }
    }

    protected abstract byte[] serialize(Context context, MODELDATA data);

    protected abstract MODELDATA unserialize(Context context, byte[] data);

    protected MODELDATA restore(Context context, String name, String namespace) {
        try {
            String cachePath = getCachePath(context, name, namespace);
            if (cachePath != null) {

                byte[] bytes = doRestore(new File(cachePath));
                MODELDATA modelData = this.unserialize(context, bytes);
                return modelData;
            }
        } catch (Throwable e) {
            logger.error("restore model data error", e);
        }
        return null;
    }

    protected abstract ModelProcessor initPipeLine(Context context, MODELDATA modeldata);

    private String getCachePath(Context context, String name, String namespace) {
        StringBuilder sb = new StringBuilder();
        String locationPre = System.getProperty(Dict.PROPERTY_USER_HOME);
        if (StringUtils.isNotEmpty(locationPre) && StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(namespace)) {
            String cachFilePath = sb.append(locationPre).append("/.fate/model_").append(name).append("_").append(namespace).append("_").append("cache").toString();
            return cachFilePath;
        }
        return null;
    }

    protected void doStore(Context context, byte[] data, File file) {
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
                        outputFile.write(data);
                    }
                } finally {
                    lock.release();
                }
            }
        } catch (Throwable e) {
            logger.error("Failed to save model cache file, will retry, cause: " + e.getMessage(), e);
        }
    }

    protected byte[] doRestore(File file) {
        if (file != null && file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                Long filelength = file.length();
                byte[] filecontent = new byte[filelength.intValue()];
                int readCount = in.read(filecontent);
                if (readCount > 0) {
                    return filecontent;
                } else {
                    return null;
                }
            } catch (Throwable e) {
                logger.error("failed to doRestore file ", e);
            }
        }
        return null;
    }

    protected abstract MODELDATA doLoadModel(Context context, String name, String namespace);

}
