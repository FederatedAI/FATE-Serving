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

package com.webank.ai.fate.serving.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {

    private static final Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    private static final String ZIP_SUFFIX = ".zip";

    public static String unzip(File zipFile, String outputDirectory) throws Exception {
        String suffix = zipFile.getName().substring(zipFile.getName().lastIndexOf("."));
        if (!zipFile.isFile() || !ZIP_SUFFIX.equalsIgnoreCase(suffix)) {
            logger.error("{} is not zip file", zipFile.getAbsolutePath());
            return null;
        }

        try (ZipFile zip = new ZipFile(new File(zipFile.getAbsolutePath()), StandardCharsets.UTF_8)) {
            String uuid = UUID.randomUUID().toString();
            File tempDir = new File(outputDirectory + uuid);
            String resultPath;
            try {
                if (!tempDir.exists()) {
                    tempDir.mkdirs();
                }
                resultPath = tempDir.getAbsolutePath();
                Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();

                    File outputFile = new File(outputDirectory + uuid, entry.getName());

                    if (!outputFile.toPath().normalize().startsWith(outputDirectory + uuid)) {
                        throw new RuntimeException("Bad zip entry");
                    }

                    if (entry.isDirectory()) {
                        outputFile.mkdirs();
                        continue;
                    } else {
                        if (!outputFile.getParentFile().exists()) {
                            outputFile.getParentFile().mkdirs();
                        }
                    }

                    try (InputStream in = new BufferedInputStream(zip.getInputStream(entry));
                         FileOutputStream out = new FileOutputStream(outputFile);
                         BufferedOutputStream bout = new BufferedOutputStream(out)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            bout.write(buf, 0, len);
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to unzip file: {}", zipFile.getAbsolutePath(), e);
                throw e;
            }

            return resultPath;
        }
    }

    public static void delete(File file) {
        if (file == null || !file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File listFile : files) {
                    delete(listFile);
                }
            }
        }

        if (!file.delete()) {
            logger.warn("Failed to delete file: {}", file.getAbsolutePath());
        }
    }

    public static void clear(String outputPath) {
        logger.info("try to clear {}", outputPath);
        if (StringUtils.isNotBlank(outputPath)) {
            delete(new File(outputPath));
        }
    }
}
