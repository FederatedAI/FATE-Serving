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

package com.webank.ai.fate.serving.proxy.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static String fileMd5(String filePath) {
        try (InputStream in = new FileInputStream(filePath)) {
            return DigestUtils.md5DigestAsHex(in);
        } catch (Exception e) {
            logger.error("Failed to calculate MD5 for file: {}, error: {}", filePath, e.getMessage());
        }
        return null;
    }

    /**
     * Write string to file,
     * synchronize operation, exclusive lock
     */
    public static boolean writeStr2ReplaceFileSync(String str, String pathFile) throws Exception {
        File file = new File(pathFile);

        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (!created) {
                    throw new IOException("Failed to create the file: " + pathFile);
                }
            } catch (IOException e) {
                logger.error("Failed to create the file. Check whether the path is valid and the read/write permission is correct", e);
                throw e;
            }
        }

        FileLock fileLock = null;
        try (FileOutputStream fileOutputStream = new FileOutputStream(file, false);
             FileChannel fileChannel = fileOutputStream.getChannel()) {
             fileLock = fileChannel.tryLock(0, Long.MAX_VALUE, false);
            if (fileLock == null) {
                throw new IOException("Unable to acquire file lock, the file is likely in use by another process");
            } else {
                fileChannel.write(ByteBuffer.wrap(str.getBytes()));
                if (fileLock.isValid()) {
                    fileLock.release();
                }
                if (file.length() != str.getBytes().length) {
                    throw new IOException("write successfully but the content was lost, reedit and try again");
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        } finally {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
            }
        }

        return true;
    }

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
