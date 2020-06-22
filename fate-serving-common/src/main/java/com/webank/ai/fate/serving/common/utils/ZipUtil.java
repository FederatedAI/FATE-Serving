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

package com.webank.ai.fate.serving.core.utils;
package com.webank.ai.fate.serving.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {

    private static Logger logger = LoggerFactory.getLogger(ZipUtil.class);

    public static String unzip(File zipFile, String outputDirectory) throws Exception {
        String suffix = zipFile.getName().substring(zipFile.getName().lastIndexOf("."));
        if (!zipFile.isFile() || !suffix.equalsIgnoreCase(".zip")) {
            logger.error("{} is not zip file", zipFile.getAbsolutePath());
            return null;
        }

        ZipFile zip = new ZipFile(new File(zipFile.getAbsolutePath()), Charset.forName("UTF-8"));
        String uuid = UUID.randomUUID().toString();
        File tempDir = new File(outputDirectory + uuid);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();

            File outputFile = new File(outputDirectory + uuid + File.separator + entry.getName());
            if (entry.isDirectory()) {
                outputFile.mkdirs();
                continue;
            } else {
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
            }

            try (InputStream in = zip.getInputStream(entry); FileOutputStream out = new FileOutputStream(outputFile)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }

        return tempDir.getAbsolutePath();
    }

    public static void delete(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                for (File listFile : file.listFiles()) {
                    if (listFile.isDirectory()) {
                        delete(listFile);
                    }
                    listFile.delete();
                }
            }
            file.delete();
        }
    }

    public static void clear(String outputPath) {
        logger.info("try to clear {}", outputPath);
        if (StringUtils.isNotBlank(outputPath)) {
            delete(new File(outputPath));
        }
    }
}
