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

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static String fileMd5(String filePath) {
        InputStream in = null;
        try {
            in = new FileInputStream(filePath);
            return DigestUtils.md5DigestAsHex(in);
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return null;
    }

    public static boolean writeFile(String context, File target) {
        BufferedWriter out = null;
        try {
            if (!target.exists()) {
                target.createNewFile();
            }
            out = new BufferedWriter(new FileWriter(target));
            out.write(context);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return false;
        } finally {
            try {
                out.flush();
                out.close();
            } catch (IOException ex) {

            }
        }
        return true;
    }

}
