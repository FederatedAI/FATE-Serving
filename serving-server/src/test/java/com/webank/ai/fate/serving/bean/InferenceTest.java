package com.webank.ai.fate.serving.bean;

import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import io.grpc.ManagedChannel;

/**
 * @Description TODOj
 **/
public class InferenceTest {
    public static void main(String[] args) {

        TestInference();


    }

    public static void TestInference() {


        String address = "localhost:8000";

        String content = "";

        GrpcConnectionPool grpcConnectionPool = GrpcConnectionPool.getPool();


        int count = 0;

        for (int t = 0; t < 20; t++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 1000000; i++) {
                        try {


                            ManagedChannel managedChannel = grpcConnectionPool.getManagedChannel(address);


                            InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
                            /**
                             *
                             */

                            InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder =
                                    InferenceServiceProto.InferenceMessage.newBuilder();

                            inferenceMessageBuilder.setBody(ByteString.copyFrom(content.getBytes()));


                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }


                            try {


                                InferenceServiceProto.InferenceMessage inferenceMessage = blockingStub.inference(inferenceMessageBuilder.build());
                                System.err.println(inferenceMessage);


                            } catch (Exception e) {
                                //   e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {

                        }
                    }
                }
            }).start();

        }


    }
}
