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

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.Dict;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServingServerTest {

    InferenceClient inferenceClient = new InferenceClient("localhost", 8000);

    @BeforeClass
    public static void init() {

    }

    @Test
    public void testLoadCacheModel() {
        test_model_load_cache("model_2020080416153238910324_guest#9999#arbiter-10000#guest-9999#host-10000#model_cache", "guest", "2020080416153238910324");
        test_model_load_cache("model_2020080416153238910324_host#10000#arbiter-10000#guest-9999#host-10000#model_cache", "host", "2020080416153238910324");
        test_model_Bind("lr-test", "2020080416153238910324");
    }

    @Test
    public void testLoadFateFlowModel() {
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder()
                .setRole("guest").setPartyId("guest".equalsIgnoreCase("guest") ? "9999" : "10000").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020080416153238910324").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020080416153238910324").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020080416153238910324").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FATEFLOW")
                .setFilePath("http%3A%2F%2F127.0.0.1%3A9380%2Fv1%2Fmodel%2Ftransfer")
//                .setFilePath("http://127.0.0.1:9380/v1/model/transfer")
                .build();

        inferenceClient.load(publishRequest);
    }

    @Test
    public void testLoadFileModel() {
        test_model_load_file("guest#9999#guest-9999#host-10000#model_2020061215063879320146.zip", "guest", "2020061215063879320146");
    }

    public void test_model_load_file(String filename, String currentRole, String modelVersion) {
        URL resource = ServingServerTest.class.getClassLoader().getResource(filename);
        String filePath = resource.getPath().replaceAll("%23", "#");
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder()
                .setRole(currentRole).setPartyId(currentRole.equalsIgnoreCase("guest") ? "9999" : "10000").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(modelVersion).setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(modelVersion).setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(modelVersion).setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setFilePath(filePath)
                .build();

        inferenceClient.load(publishRequest);
    }

    public void test_model_load_cache(String filename, String currentRole, String tablename) {
        URL resource = ServingServerTest.class.getClassLoader().getResource(filename);
        String filePath = resource.getPath().replaceAll("%23", "#");
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder()
                .setRole(currentRole).setPartyId(currentRole.equalsIgnoreCase("guest") ? "9999" : "10000").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(tablename).setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(tablename).setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(tablename).setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("CACHE")
                .setFilePath(filePath)
                .build();

        inferenceClient.load(publishRequest);
    }

    public void test_model_Bind(String serviceId, String tableName) {
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().setRole("guest").setPartyId("9999").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(tableName).setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(tableName).setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName(tableName).setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setServiceId(serviceId)
                .build();

        inferenceClient.bind(publishRequest);
    }

    @Test
    public void test_03_Inference() {

        InferenceRequest inferenceRequest = new InferenceRequest();

        inferenceRequest.setServiceId("lr-test");

        inferenceRequest.getFeatureData().put("x0", 0.100016);
        inferenceRequest.getFeatureData().put("x1", 1.210);
        inferenceRequest.getFeatureData().put("x2", 2.321);
        inferenceRequest.getFeatureData().put("x3", 3.432);
        inferenceRequest.getFeatureData().put("x4", 4.543);
        inferenceRequest.getFeatureData().put("x5", 5.654);
        inferenceRequest.getFeatureData().put("x6", 5.654);
        inferenceRequest.getFeatureData().put("x7", 0.102345);

        inferenceRequest.getSendToRemoteFeatureData().put(Dict.DEVICE_ID, "8");

        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
        String contentString = JsonUtil.object2Json(inferenceRequest);
        System.err.println("send data ===" + contentString);
        try {
            inferenceMessageBuilder.setBody(ByteString.copyFrom(contentString, "UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();

        System.err.println(inferenceMessage.getBody());

        InferenceServiceProto.InferenceMessage resultMessage = inferenceClient.inference(inferenceMessage);

        System.err.println("result ==================" + new String(resultMessage.getBody().toByteArray()));
    }

    @Test
    public void test_04_BatchInference() {
        while (true) {
            try {
                BatchInferenceRequest batchInferenceRequest = new BatchInferenceRequest();
                batchInferenceRequest.setCaseId(Long.toString(System.currentTimeMillis()));
                List<BatchInferenceRequest.SingleInferenceData> singleInferenceDataList = Lists.newArrayList();
                for (int i = 0; i < 10; i++) {
                    BatchInferenceRequest.SingleInferenceData singleInferenceData = new BatchInferenceRequest.SingleInferenceData();

                    singleInferenceData.getFeatureData().put("x0", 0.100016);
                    singleInferenceData.getFeatureData().put("x1", 1.210);
                    singleInferenceData.getFeatureData().put("x2", 2.321);
                    singleInferenceData.getFeatureData().put("x3", 3.432);
                    singleInferenceData.getFeatureData().put("x4", 4.543);
                    singleInferenceData.getFeatureData().put("x5", 5.654);
                    singleInferenceData.getFeatureData().put("x6", 5.654);
                    singleInferenceData.getFeatureData().put("x7", 0.102345);

                    singleInferenceData.getSendToRemoteFeatureData().putAll(singleInferenceData.getFeatureData());
                    singleInferenceData.setIndex(i);
                    singleInferenceDataList.add(singleInferenceData);

                }
                batchInferenceRequest.setBatchDataList(singleInferenceDataList);
                batchInferenceRequest.setServiceId("lr-test");
                InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                        InferenceServiceProto.InferenceMessage.newBuilder();
                String contentString = JsonUtil.object2Json(batchInferenceRequest);
                System.err.println("send data ===" + contentString);
                try {
                    inferenceMessageBuilder.setBody(ByteString.copyFrom(contentString, "UTF-8"));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                InferenceServiceProto.InferenceMessage inferenceMessage = inferenceMessageBuilder.build();

                System.err.println(inferenceMessage.getBody());

                InferenceServiceProto.InferenceMessage resultMessage = inferenceClient.batchInference(inferenceMessage);

                System.err.println("result ==================" + new String(resultMessage.getBody().toByteArray()));
                Thread.sleep(300);
            } catch (Exception e) {

            }
        }
    }

}