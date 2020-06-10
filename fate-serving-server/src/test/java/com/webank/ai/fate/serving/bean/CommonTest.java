package com.webank.ai.fate.serving.bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.core.flow.MetricNode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.util.List;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CommonTest {

    InferenceClient inferenceClient = new InferenceClient("localhost", 8000);

    @BeforeClass
    public static void init() {

    }

    @Test
    public void test_05_query_metric() {
        CommonServiceProto.QueryMetricRequest.Builder builder = CommonServiceProto.QueryMetricRequest.newBuilder();

        long now = System.currentTimeMillis();
        long begin = now - 5000;
        builder.setBeginMs(begin);
        builder.setEndMs(now);
        builder.setType(CommonServiceProto.MetricType.INTERFACE);
      //  builder.setSource("commonService");
        CommonServiceProto.CommonResponse resultMessage = inferenceClient.queryMetric(builder.build());

        List<MetricNode> list = JsonUtil.json2Object(resultMessage.getData().toStringUtf8(), new TypeReference<List<MetricNode>>(){});
        System.err.println("StatusCode ==================" + resultMessage.getStatusCode());
        System.err.println("Message ==================" + resultMessage.getMessage());
        System.err.println("result ==================" + JsonUtil.object2Json(list));
    }

    @Test
    public void test_06_update_rule() {
        CommonServiceProto.UpdateFlowRuleRequest.Builder builder = CommonServiceProto.UpdateFlowRuleRequest.newBuilder();
        builder.setSource("commonService");
        builder.setAllowQps(100);
        CommonServiceProto.CommonResponse resultMessage = inferenceClient.updateRule(builder.build());

        System.err.println("StatusCode ==================" + resultMessage.getStatusCode());
        System.err.println("Message ==================" + resultMessage.getMessage());
        System.err.println("result ==================" + resultMessage.getData().toStringUtf8());
    }

    @Test
    public void test_07_list_props() {
        CommonServiceProto.QueryPropsRequest.Builder builder = CommonServiceProto.QueryPropsRequest.newBuilder();
        builder.setKeyword("port");
        CommonServiceProto.CommonResponse resultMessage = inferenceClient.listProps(builder.build());

        System.err.println("StatusCode ==================" + resultMessage.getStatusCode());
        System.err.println("Message ==================" + resultMessage.getMessage());
        System.err.println("result ==================" + resultMessage.getData().toStringUtf8());
    }

    @Test
    public void test_08_query_models() {
        ModelServiceProto.QueryModelRequest.Builder builder = ModelServiceProto.QueryModelRequest.newBuilder();

        ModelServiceProto.QueryModelResponse response = inferenceClient.queryModels(builder.build());

        System.err.println("StatusCode ==================" + response.getRetcode());
        System.err.println("Message ==================" + response.getMessage());
        System.err.println("result ==================" + JsonUtil.object2Json(response.getModelInfosList()));
    }


    @Test
    public void test_09_query_jvm() {

        CommonServiceProto.QueryJvmInfoRequest.Builder builder = CommonServiceProto.QueryJvmInfoRequest.newBuilder();
        builder.setKeyword("port");
        CommonServiceProto.CommonResponse resultMessage = inferenceClient.queryJvmInfo(builder.build());

        System.err.println("StatusCode ==================" + resultMessage.getStatusCode());
        System.err.println("Message ==================" + resultMessage.getMessage());
        System.err.println("result ==================" + resultMessage.getData().toStringUtf8());
    }

    @Test
    public void test_10_query_model_metric() {

        CommonServiceProto.QueryMetricRequest.Builder builder = CommonServiceProto.QueryMetricRequest.newBuilder();

        long now = System.currentTimeMillis();
        long begin = now - 5000;
        builder.setBeginMs(begin);
        builder.setEndMs(now);
        builder.setType(CommonServiceProto.MetricType.MODEL);
        CommonServiceProto.CommonResponse resultMessage = inferenceClient.queryMetric(builder.build());

        List<MetricNode> list = JsonUtil.json2List(resultMessage.getData().toStringUtf8(), new TypeReference<List<MetricNode>>(){});
        System.err.println("StatusCode ==================" + resultMessage.getStatusCode());
        System.err.println("Message ==================" + resultMessage.getMessage());
        System.err.println("result ==================" + JsonUtil.object2Json(list));

    }

}