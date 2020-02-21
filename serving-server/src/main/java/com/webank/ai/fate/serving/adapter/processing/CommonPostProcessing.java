package com.webank.ai.fate.serving.adapter.processing;


import com.webank.ai.fate.serving.bean.PostProcessingResult;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.InferenceRetCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CommonPostProcessing implements PostProcessing {


    private static final Logger logger = LoggerFactory.getLogger(CommonPostProcessing.class);

    @Override
    public PostProcessingResult getResult(Context context, Map<String, Object> featureData, Map<String, Object> modelResult) {
        PostProcessingResult postProcessingResult = new PostProcessingResult();
        Integer rcode=0;
        Map<String, Object> data = new HashMap<>();
        if(modelResult!=null) {
            if (modelResult.get(Dict.RET_CODE) != null) {
                rcode = (Integer) modelResult.get(Dict.RET_CODE);
            }
            else {
                rcode= InferenceRetCode.NO_RESULT;
            }

            Double prob = Double.parseDouble(modelResult.get("prob").toString());
            data.put(Dict.SCORE, prob);
        }else{
            rcode= InferenceRetCode.NO_RESULT;
        }
        ReturnResult result = new ReturnResult();
        result.setData(data);
        result.setRetcode(rcode);
        postProcessingResult.setProcessingResult(result);
        return postProcessingResult;
    }

}



