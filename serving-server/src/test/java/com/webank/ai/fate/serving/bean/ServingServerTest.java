package com.webank.ai.fate.serving.bean;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;

//@RunWith(SpringRunner.class)
//@SpringBootTest
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServingServerTest {

    InferenceClient inferenceClient = new InferenceClient("localhost", 8000);

    @BeforeClass
    public static void init() {

    }

    @Test
    public void test_01_guest_load() {

        URL resource = ServingServerTest.class.getClassLoader().getResource("model_2020030314574740594812_guest#9999#arbiter-10000#guest-9999#host-10000#model_cache");
        String filePath = resource.getPath();
        filePath = filePath.replaceAll("%23", "#");
        System.err.println("filePath " + filePath);
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().setRole("guest").setPartyId("9999").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("202004201033174706226").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("202004201033174706226").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("202004201033174706226").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setFilePath(filePath)
                .build();
        // inboundPackage.setBody(publishRequest);

        inferenceClient.load(publishRequest);


    }


    @Test
    public void test_01_guest_load_fm() {

        URL resource = ServingServerTest.class.getClassLoader().getResource("model_2020030314574740594812_guest#9999#arbiter-10000#guest-9999#host-10000#model_cache");
        String filePath = resource.getPath();
//        String filePath = "/Users/kaideng/work/webank/newLocationFateserving/feature-1.2/FATE-Serving/serving-server/src/main/resources/model_2020030314574740594812_guest#9999#arbiter-10000#guest-9999#host-10000#model_cache";
        filePath = filePath.replaceAll("%23", "#");
        System.err.println("filePath " + filePath);
        // String  filepath =       "/Users/kaideng/work/webank/test/model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";
//        Context  context =  new ServingServerContext();
//        InboundPackage   inboundPackage = new InboundPackage();
//        OutboundPackage  outboundPackage = new OutboundPackage();
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().setRole("guest").setPartyId("9999").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setFilePath(filePath)
                .build();
        // inboundPackage.setBody(publishRequest);

        inferenceClient.load(publishRequest);


    }

    @Test
    public void test_01_host_load_fm() {


        URL resource = ServingServerTest.class.getClassLoader().getResource("model_2020030314574740594812_host#10000#arbiter-10000#guest-9999#host-10000#model_cache");
        String filePath = resource.getPath();
        filePath = filePath.replaceAll("%23", "#");

//        String filePath = "/Users/kaideng/work/webank/newLocationFateserving/feature-1.2/FATE-Serving/serving-server/src/main/resources/model_2020030314574740594812_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";

        System.err.println("filePath " + filePath);
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().
                setRole("host").setPartyId("10000").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setFilePath(filePath)
                .build();
        // inboundPackage.setBody(publishRequest);

        inferenceClient.load(publishRequest);


    }


    @Test
    public void test_01_host_load() {

        URL resource = ServingServerTest.class.getClassLoader().getResource("model_202004201033174706226_host#10000#guest-9999#host-10000#model_cache");
        String filePath = resource.getPath();
        filePath = filePath.replaceAll("%23", "#");
        System.err.println("filePath " + filePath);
        // String  filepath =       "/Users/kaideng/work/webank/test/model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";
//        Context  context =  new ServingServerContext();
//        InboundPackage   inboundPackage = new InboundPackage();
//        OutboundPackage  outboundPackage = new OutboundPackage();
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().
                setRole("host").setPartyId("10000").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("202004201033174706226").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("202004201033174706226").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("202004201033174706226").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setFilePath(filePath)
                .build();
        // inboundPackage.setBody(publishRequest);

        inferenceClient.load(publishRequest);


    }


    @Test
    public void test_02_guest_Bind() {
//        URL resource = ServingServerTest.class.getClassLoader().getResource("model_2020040111152695637611_guest#9999#arbiter-10000#guest-9999#host-10000#model_cache");
//        String  filePath = resource.getPath();
//        filePath = filePath.replaceAll("%23","#");
//        System.err.println("filePath "+ filePath);
        // String  filepath =       "/Users/kaideng/work/webank/test/model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";
//        Context  context =  new ServingServerContext();
//        InboundPackage   inboundPackage = new InboundPackage();
//        OutboundPackage  outboundPackage = new OutboundPackage();
        ModelServiceProto.PublishRequest.Builder publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().setRole("guest").setPartyId("9999").build())
                .putRole("guest", ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host", ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter", ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020030314574740594812").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setServiceId("fm")
                //   .setFilePath(filePath)
                .build();
        //   inboundPackage.setBody(publishRequest);

        inferenceClient.bind(publishRequest);

//        ReturnResult  returnResult =(ReturnResult) modelServiceProvider.bind(context,inboundPackage,outboundPackage);
//        Assert.assertEquals(returnResult.getRetcode(),"0");

    }


    @Test
    public void test_03_Inference() {

        InferenceRequest inferenceRequest = new InferenceRequest();

        inferenceRequest.setServiceId("fm");

        inferenceRequest.getFeatureData().put("x0", 0.100016);
        inferenceRequest.getFeatureData().put("x1", 1.210);
        inferenceRequest.getFeatureData().put("x2", 2.321);
        inferenceRequest.getFeatureData().put("x3", 3.432);
        inferenceRequest.getFeatureData().put("x4", 4.543);
        inferenceRequest.getFeatureData().put("x5", 5.654);
        inferenceRequest.getFeatureData().put("x6", 5.654);
        inferenceRequest.getFeatureData().put("x7", 0.102345);

        inferenceRequest.getSendToRemoteFeatureData().putAll(inferenceRequest.getFeatureData());

        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();
        String contentString = JSON.toJSONString(inferenceRequest);
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
            batchInferenceRequest.setServiceId("my_test_service_id");
            InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                    InferenceServiceProto.InferenceMessage.newBuilder();
            String contentString = JSON.toJSONString(batchInferenceRequest);
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

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}