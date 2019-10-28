package com.webank.ai.fate.serving.federatedml.model;

import com.webank.ai.fate.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.bean.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.exp;

public class HeteroLRGuest extends HeteroLR {
    private static final Logger LOGGER = LogManager.getLogger();

    private double sigmod(double x) {
        return 1. / (1. + exp(-x));
    }

    @Override
    public Map<String, Object> predict(Context context , List<Map<String, Object>> inputData, Map<String, Object> predictParams) {
        Map<String, Object> result = new HashMap<>();
        Map<String, Double> forwardRet = forward(inputData);
        double score = forwardRet.get("score");
        double guestScore = score;
        double hostScore = 0;
        try {
            ReturnResult hostPredictResponse = this.getFederatedPredict(context,(Map<String, Object>) predictParams.get("federatedParams"));
            context.setFederatedResult(hostPredictResponse);
            LOGGER.info("caseid {} host response is {}",context.getCaseId(), hostPredictResponse.getData());
            if(hostPredictResponse.getData()!=null&&hostPredictResponse.getData().get("score")!=null) {
                 hostScore = ((Number) hostPredictResponse.getData().get("score")).doubleValue();
                score += hostScore;
            }
        } catch (Exception ex) {
            LOGGER.error("get host predict failed:", ex);
        }

        double prob = sigmod(score);
        LOGGER.info("caseid {} guest score {} host score {} prob {}",context.getCaseId(),guestScore, hostScore,prob);
        result.put("prob", prob);
        result.put("guestModelWeightHitRate:{}", forwardRet.get("modelWrightHitRate"));
        result.put("guestInputDataHitRate:{}", forwardRet.get("inputDataHitRate"));
        return result;
    }
}
