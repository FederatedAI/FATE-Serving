package com.webank.ai.fate.serving.federatedml.model;

import com.webank.ai.fate.core.constant.StatusCode;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningMetaProto;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningMetaProto.FeatureBinningMeta;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningMetaProto.TransformMeta;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningParamProto;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningParamProto.FeatureBinningParam;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningParamProto.FeatureBinningResult;
import com.webank.ai.fate.core.mlmodel.buffer.FeatureBinningParamProto.IVParam;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.FederatedParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HeteroFeatureBinning extends BaseModel {
    private static final Logger LOGGER = LogManager.getLogger();
    private Map<String, List<Double>> splitPoints;
    private List<Long> transformCols;
    private List<String> header;
    private boolean needRun;

    @Override
    public int initModel(byte[] protoMeta, byte[] protoParam) {
        LOGGER.info("start init Feature Binning class");
        this.needRun = false;
        this.splitPoints = new HashMap<>();

        try {
            FeatureBinningMeta featureBinningMeta = this.parseModel(FeatureBinningMeta.parser(), protoMeta);
            this.needRun = featureBinningMeta.getNeedRun();
            TransformMeta transformMeta = featureBinningMeta.getTransformParam();
            this.transformCols = transformMeta.getTransformColsList();

            FeatureBinningParam featureBinningParam = this.parseModel(FeatureBinningParam.parser(), protoParam);
            this.header = featureBinningParam.getHeaderList();
            FeatureBinningResult featureBinningResult = featureBinningParam.getBinningResult();
            Map<String, IVParam> binningResult = featureBinningResult.getBinningResultMap();
            for (String key : binningResult.keySet()) {
                IVParam oneColResult = binningResult.get(key);
                List<Double> splitPoints = oneColResult.getSplitPointsList();
                this.splitPoints.put(key, splitPoints);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return StatusCode.ILLEGALDATA;
        }
        LOGGER.info("Finish init Feature Binning class");
        return StatusCode.OK;
    }

    @Override
    public Map<String, Object> handlePredict(Context context, List<Map<String, Object>> inputData, FederatedParams predictParams) {
        LOGGER.info("Start Feature Binning predict");
        HashMap<String, Object> outputData = new HashMap<>();
        Map<String, Object> firstData = inputData.get(0);
        if (!this.needRun) {
            return firstData;
        }

        for (String colName : firstData.keySet()) {
		    try{
		        if (! this.splitPoints.containsKey(colName)) {
                    outputData.put(colName, firstData.get(colName));
		            continue;
                }
		        Long thisColIndex = (long) this.header.indexOf(colName);
		        if (! this.transformCols.contains(thisColIndex)) {
                    outputData.put(colName, firstData.get(colName));
                    continue;
                }
                List<Double> splitPoint = this.splitPoints.get(colName);
                Double colValue = Double.valueOf(firstData.get(colName).toString());
                int colIndex = 0;
                for (colIndex = 0; colIndex < splitPoint.size(); colIndex ++) {
                    if (colValue <= splitPoint.get(colIndex)) {
                        break;
                    }
            }
            outputData.put(colName, colIndex);
		    }catch(Throwable e){
		        LOGGER.error("HeteroFeatureBinning error" ,e);
            }
        }

        return outputData;
    }

}
