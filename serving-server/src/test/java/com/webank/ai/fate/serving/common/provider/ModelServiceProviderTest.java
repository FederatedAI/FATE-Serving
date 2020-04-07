package com.webank.ai.fate.serving.common.provider;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.bean.InferenceClient;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.bean.ServingServerContext;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.net.URL;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelServiceProviderTest {
    @Autowired
     ModelServiceProvider  modelServiceProvider;
    @Autowired
    Environment  environment;

    @BeforeClass
    public static  void  init(){



    }
    @Test
    public void test_01_Load() {

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


        URL resource = ModelServiceProviderTest.class.getClassLoader().getResource("model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache");
        String  filePath = resource.getPath();
        filePath = filePath.replaceAll("%23","#");
        System.err.println("filePath "+ filePath);
        // String  filepath =       "/Users/kaideng/work/webank/test/model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";
        Context  context =  new ServingServerContext();
        InboundPackage   inboundPackage = new InboundPackage();
        OutboundPackage  outboundPackage = new OutboundPackage();
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
        inboundPackage.setBody(publishRequest);


        ReturnResult  returnResult =(ReturnResult) modelServiceProvider.load(context,inboundPackage,outboundPackage);

        Assert.assertEquals(returnResult.getRetcode(),"0");
    }


    @Test
    public void test_02_Bind(){
        URL resource = ModelServiceProviderTest.class.getClassLoader().getResource("model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache");
        String  filePath = resource.getPath();
        filePath = filePath.replaceAll("%23","#");
        System.err.println("filePath "+ filePath);
        // String  filepath =       "/Users/kaideng/work/webank/test/model_2020040111152695637611_host#10000#arbiter-10000#guest-9999#host-10000#model_cache";
        Context  context =  new ServingServerContext();
        InboundPackage   inboundPackage = new InboundPackage();
        OutboundPackage  outboundPackage = new OutboundPackage();
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
                .setFilePath(filePath)
                .build();
        inboundPackage.setBody(publishRequest);
        ReturnResult  returnResult =(ReturnResult) modelServiceProvider.bind(context,inboundPackage,outboundPackage);
        Assert.assertEquals(returnResult.getRetcode(),"0");

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

        InferenceClient.inference("localhost",8000,inferenceMessage.toByteArray());

    }



}