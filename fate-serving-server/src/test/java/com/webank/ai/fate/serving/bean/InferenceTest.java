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
