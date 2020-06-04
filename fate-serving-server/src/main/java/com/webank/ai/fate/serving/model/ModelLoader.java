package com.webank.ai.fate.serving.model;

import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.model.ModelProcessor;

public interface ModelLoader {

    public ModelProcessor loadModel(Context context, ModelLoaderParam modelLoaderParam);

    public ModelProcessor restoreModel(Context context, ModelLoaderParam modelLoaderParam);

    public enum LoadModelType {
        FATEFLOW,
        FILE,
        PB
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
