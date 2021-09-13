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

package com.webank.ai.fate.serving.bean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.serving.common.flow.MetricNode;
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
public class CommonServiceTest {

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

        List<MetricNode> list = JsonUtil.json2Object(resultMessage.getData().toStringUtf8(), new TypeReference<List<MetricNode>>() {
        });
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
        List<ModelServiceProto.ModelInfoEx> modelInfosList = response.getModelInfosList();
        for (ModelServiceProto.ModelInfoEx modelInfoEx : modelInfosList) {
            System.err.println("result ==================" + JsonUtil.object2Json(modelInfoEx.getContent()));
        }
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

        List<MetricNode> list = JsonUtil.json2List(resultMessage.getData().toStringUtf8(), new TypeReference<List<MetricNode>>() {
        });
        System.err.println("StatusCode ==================" + resultMessage.getStatusCode());
        System.err.println("Message ==================" + resultMessage.getMessage());
        System.err.println("result ==================" + JsonUtil.object2Json(list));

    }

    @Test
    public void test_11_check_health() {
        CommonServiceProto.HealthCheckRequest.Builder builder = CommonServiceProto.HealthCheckRequest.newBuilder();
        builder.setType("test");
        builder.setMode("test");
        builder.setVersion("0.0");
        CommonServiceProto.CommonResponse resultMessage = inferenceClient.checkHealth(builder.build());
        System.err.println("StatusCode ==================" + resultMessage.getStatusCode());
        System.err.println("Message ==================" + resultMessage.getMessage());
        System.err.println("result ==================" + resultMessage.getData().toStringUtf8());
    }
}