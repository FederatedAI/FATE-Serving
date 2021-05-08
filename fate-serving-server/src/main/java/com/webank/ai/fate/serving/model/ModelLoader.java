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

import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.core.bean.Context;

public interface ModelLoader {

    public ModelProcessor loadModel(Context context, ModelLoaderParam modelLoaderParam);

    public ModelProcessor restoreModel(Context context, ModelLoaderParam modelLoaderParam);

    public enum LoadModelType {
        FATEFLOW,
        FILE,
        CACHE
    }

    public class ModelLoaderParam {
        String tableName;
        String nameSpace;
        LoadModelType loadModelType;
        String filePath;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getNameSpace() {
            return nameSpace;
        }

        public void setNameSpace(String nameSpace) {
            this.nameSpace = nameSpace;
        }

        public LoadModelType getLoadModelType() {
            return loadModelType;
        }

        public void setLoadModelType(LoadModelType loadModelType) {
            this.loadModelType = loadModelType;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }
    }

}
