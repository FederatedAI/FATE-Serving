package com.webank.ai.fate.serving.bean;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import org.assertj.core.util.Lists;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Map;

//@RunWith(SpringRunner.class)
//@SpringBootTest
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServingServerTest {

    InferenceClient  inferenceClient = new   InferenceClient("localhost",8000);
    @BeforeClass
    public static  void  init(){

    }
    @Test
    public void test_01_guest_load() {

//        message PublishRequest{
//            LocalInfo local = 1;
//            map<string, Party> role = 2;
//            map<string, RoleModelInfo> model = 3;
//            string serviceId = 4;
//            string tableName = 5;
//            string namespace = 6;
//            string loadType = 7;
//            string filePath = 8;
//        }



        //Context context, InboundPackage data, OutboundPackage outboundPackage


        //        {
//            role: "guest"
//            partyId: "9999"
//        }
//        role {
//            key: "guest"
//            value {
//                partyId: "9999"
//            }
//        }
//        role {
//            key: "arbiter"
//            value {
//                partyId: "10000"
//            }
//        }
//        role {
//            key: "host"
//            value {
//                partyId: "10000"
//            }
//        }
//        model {
//            key: "host"
//            value {
//                roleModelInfo {
//                    key: "10000"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "host#10000#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }
//        model {
//            key: "guest"
//            value {
//                roleModelInfo {
//                    key: "9999"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "guest#9999#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }
//        model {
//            key: "arbiter"
//            value {
//                roleModelInfo {
//                    key: "10000"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "arbiter#10000#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }

        ///Users/kaideng/work/webank/test


        URL resource = ServingServerTest.class.getClassLoader().getResource("model_2020040111152695637611_guest#9999#arbiter-10000#guest-9999#host-10000#model_cache");
        String  filePath = resource.getPath();
        filePath = filePath.replaceAll("%23","#");
        System.err.println("filePath "+ filePath);
        // String  filepath =       "/Users/kaideng/work/webank/test/model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";
//        Context  context =  new ServingServerContext();
//        InboundPackage   inboundPackage = new InboundPackage();
//        OutboundPackage  outboundPackage = new OutboundPackage();
        ModelServiceProto.PublishRequest.Builder  publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest  publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().setRole("guest").setPartyId("9999").build())
                .putRole("guest",ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter",ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host",ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setFilePath(filePath)
        .build();
       // inboundPackage.setBody(publishRequest);

        inferenceClient.load(publishRequest);



    }



    @Test
    public void test_01_host_load() {

//        message PublishRequest{
//            LocalInfo local = 1;
//            map<string, Party> role = 2;
//            map<string, RoleModelInfo> model = 3;
//            string serviceId = 4;
//            string tableName = 5;
//            string namespace = 6;
//            string loadType = 7;
//            string filePath = 8;
//        }



        //Context context, InboundPackage data, OutboundPackage outboundPackage


        //        {
//            role: "guest"
//            partyId: "9999"
//        }
//        role {
//            key: "guest"
//            value {
//                partyId: "9999"
//            }
//        }
//        role {
//            key: "arbiter"
//            value {
//                partyId: "10000"
//            }
//        }
//        role {
//            key: "host"
//            value {
//                partyId: "10000"
//            }
//        }
//        model {
//            key: "host"
//            value {
//                roleModelInfo {
//                    key: "10000"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "host#10000#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }
//        model {
//            key: "guest"
//            value {
//                roleModelInfo {
//                    key: "9999"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "guest#9999#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }
//        model {
//            key: "arbiter"
//            value {
//                roleModelInfo {
//                    key: "10000"
//                    value {
//                        tableName: "2020022715571644961011"
//                        namespace: "arbiter#10000#arbiter-10000#guest-9999#host-10000#model"
//                    }
//                }
//            }
//        }

        ///Users/kaideng/work/webank/test


        URL resource = ServingServerTest.class.getClassLoader().getResource("model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache");
        String  filePath = resource.getPath();
        filePath = filePath.replaceAll("%23","#");
        System.err.println("filePath "+ filePath);
        // String  filepath =       "/Users/kaideng/work/webank/test/model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";
//        Context  context =  new ServingServerContext();
//        InboundPackage   inboundPackage = new InboundPackage();
//        OutboundPackage  outboundPackage = new OutboundPackage();
        ModelServiceProto.PublishRequest.Builder  publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest  publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().
                setRole("host").setPartyId("10000").build())
                .putRole("guest",ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter",ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host",ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setFilePath(filePath)
                .build();
        // inboundPackage.setBody(publishRequest);

        inferenceClient.load(publishRequest);



    }





    @Test
    public void test_02_guest_Bind(){
//        URL resource = ServingServerTest.class.getClassLoader().getResource("model_2020040111152695637611_guest#9999#arbiter-10000#guest-9999#host-10000#model_cache");
//        String  filePath = resource.getPath();
//        filePath = filePath.replaceAll("%23","#");
//        System.err.println("filePath "+ filePath);
        // String  filepath =       "/Users/kaideng/work/webank/test/model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";
//        Context  context =  new ServingServerContext();
//        InboundPackage   inboundPackage = new InboundPackage();
//        OutboundPackage  outboundPackage = new OutboundPackage();
        ModelServiceProto.PublishRequest.Builder  publicRequestBuilder = ModelServiceProto.PublishRequest.newBuilder();
        ModelServiceProto.PublishRequest  publishRequest = publicRequestBuilder.setLocal(ModelServiceProto.LocalInfo.newBuilder().setRole("guest").setPartyId("9999").build())
                .putRole("guest",ModelServiceProto.Party.newBuilder().addPartyId("9999").build())
                .putRole("arbiter",ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putRole("host",ModelServiceProto.Party.newBuilder().addPartyId("10000").build())
                .putModel("guest",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("9999",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("guest#9999#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("host",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("host#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .putModel("arbiter",ModelServiceProto.RoleModelInfo.newBuilder().putRoleModelInfo("10000",
                        ModelServiceProto.ModelInfo.newBuilder().setTableName("2020022715571644961011").setNamespace("arbiter#10000#arbiter-10000#guest-9999#host-10000#model").build()).build())
                .setLoadType("FILE")
                .setServiceId("my_test_service_id")
             //   .setFilePath(filePath)
                .build();
     //   inboundPackage.setBody(publishRequest);

        inferenceClient.bind(publishRequest);

//        ReturnResult  returnResult =(ReturnResult) modelServiceProvider.bind(context,inboundPackage,outboundPackage);
//        Assert.assertEquals(returnResult.getRetcode(),"0");

    }


    @Test
    public  void  test_03_Inference(){

        InferenceRequest inferenceRequest  = new  InferenceRequest();

        inferenceRequest.setServiceId("my_test_service_id");

        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();
        String  contentString = JSON.toJSONString(inferenceRequest);
        System.err.println("send data ==="+contentString);
        try {
            inferenceMessageBuilder.setBody(ByteString.copyFrom(contentString,"UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        InferenceServiceProto.InferenceMessage  inferenceMessage = inferenceMessageBuilder.build();

        System.err.println(inferenceMessage.getBody());

        InferenceServiceProto.InferenceMessage     resultMessage = inferenceClient.inference(inferenceMessage.toByteArray());

        System.err.println( "result =================="+new String(resultMessage.getBody().toByteArray()));

    }

    @Test
    public  void  test_04_BatchInference(){

//        InferenceRequest inferenceRequest  = new  InferenceRequest();
//
//        inferenceRequest.setServiceId("my_test_service_id");

        BatchInferenceRequest   batchInferenceRequest = new  BatchInferenceRequest();
        batchInferenceRequest.setCaseId(Long.toString(System.currentTimeMillis()));
        List<BatchInferenceRequest.SingleInferenceData> singleInferenceDataList = Lists.newArrayList();
        for(int i=0;i<10;i++){
            BatchInferenceRequest.SingleInferenceData  singleInferenceData = new  BatchInferenceRequest.SingleInferenceData();
            Map temp =Maps.newHashMap();
            temp.put("phone_test","1399987933");
            singleInferenceData.setSendToRemoteFeatureData(temp);
            singleInferenceData.setIndex(i);
            singleInferenceDataList.add(singleInferenceData);

        }
        batchInferenceRequest.setBatchDataList(singleInferenceDataList);
        batchInferenceRequest.setServiceId("my_test_service_id");
        InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                InferenceServiceProto.InferenceMessage.newBuilder();
        String  contentString = JSON.toJSONString(batchInferenceRequest);
        System.err.println("send data ==="+contentString);
        try {
            inferenceMessageBuilder.setBody(ByteString.copyFrom(contentString,"UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        InferenceServiceProto.InferenceMessage  inferenceMessage = inferenceMessageBuilder.build();

        System.err.println(inferenceMessage.getBody());

        InferenceServiceProto.InferenceMessage     resultMessage = inferenceClient.batchInference(inferenceMessage.toByteArray());

        System.err.println( "result =================="+new String(resultMessage.getBody().toByteArray()));

    }



}