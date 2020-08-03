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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.networking.common.CommonServiceGrpc;
import com.webank.ai.fate.api.networking.common.CommonServiceProto;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.common.utils.HttpClientPool;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.utils.JsonUtil;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceTest {

    private static int PROCESS_NUM = 30;// 并发线程数
    private static int BATCH_DATA_SIZE = 300;// 单批次数量
    private static String HOST = "localhost";
    private static int PORT = 8000;
    private static File INFO_OUTPUT_FILE = new File("logs/performance-info.log");
    private static File INFO_OUTPUT_FILE_SAMPLE = new File("logs/performance-info-sample.log");
    private static File ERROR_OUTPUT_FILE = new File("logs/performance-error.log");
    private static Random random = new Random();
    private static GrpcConnectionPool GRPC_CONNECTION_POOL = GrpcConnectionPool.getPool();
    private static ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, 1000, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
    private static AtomicLong atomicLong = new AtomicLong(0);
    private static Map<String, Object> params = Maps.newHashMap();

    static {
        Map featureMap = Maps.newHashMap();
        for (int j = 0; j < 300; j++) {
            String a = String.format("x%s", j);
            featureMap.put(a, random.nextDouble());
        }

        BatchInferenceRequest batchInferenceRequest = new BatchInferenceRequest();
        batchInferenceRequest.setServiceId("lr-001");
        batchInferenceRequest.setCaseId(Long.toString(System.currentTimeMillis()));
        List<BatchInferenceRequest.SingleInferenceData> singleInferenceDataList = Lists.newArrayList();

        for (int i = 0; i < BATCH_DATA_SIZE; i++) {
            BatchInferenceRequest.SingleInferenceData singleInferenceData = new BatchInferenceRequest.SingleInferenceData();
            singleInferenceData.getFeatureData().putAll(featureMap);

            singleInferenceData.getSendToRemoteFeatureData().put("device_id", String.valueOf(i + 1));
            singleInferenceData.setIndex(i);
            singleInferenceDataList.add(singleInferenceData);
        }

        batchInferenceRequest.setBatchDataList(singleInferenceDataList);

        params.put("head", Maps.newHashMap());
        params.put("body", Maps.newHashMap());

        ((Map) params.get("head")).put("serviceId", batchInferenceRequest.getServiceId());
        ((Map) params.get("body")).put("batchDataList", JsonUtil.json2Object(JsonUtil.object2Json(batchInferenceRequest.getBatchDataList()), new TypeReference<Object>() {
        }));

    }

    public static void main(String[] args) throws IOException {
        HttpClientPool.initPool();
        init();
        concurrentCall();
//        call();
    }

    private static void init() throws IOException {
        if (!INFO_OUTPUT_FILE.exists()) {
            INFO_OUTPUT_FILE.createNewFile();
        }
        if (!INFO_OUTPUT_FILE_SAMPLE.exists()) {
            INFO_OUTPUT_FILE_SAMPLE.createNewFile();
        }
        if (!ERROR_OUTPUT_FILE.exists()) {
            ERROR_OUTPUT_FILE.createNewFile();
        }
    }

    /**
     * 并发调用
     */
    private static void concurrentCall() {
//        scheduledExecutorService.scheduleAtFixedRate(() -> {
        for (int i = 0; i < PROCESS_NUM; i++) {
            call();
        }
//        }, 1000, 1000, TimeUnit.MILLISECONDS);

//        scheduledExecutorService.scheduleAtFixedRate(() -> {
//            System.out.println("==============" + atomicLong.get() + " times");
//        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 单次调用
     */
    private static void call() {
        THREAD_POOL_EXECUTOR.submit(() -> {
//            batchInference();
            batchInferenceByHttp();
//            listProps();
        });
    }

    private static void listProps() {
        CommonServiceProto.QueryPropsRequest.Builder builder = CommonServiceProto.QueryPropsRequest.newBuilder();
        ManagedChannel managedChannel = GRPC_CONNECTION_POOL.getManagedChannel(HOST, PORT);
        CommonServiceGrpc.CommonServiceBlockingStub blockingStub = CommonServiceGrpc.newBlockingStub(managedChannel);
        CommonServiceProto.CommonResponse response = blockingStub.listProps(builder.build());

        System.err.println("StatusCode ==================" + response.getStatusCode());
    }

    private static void batchInferenceByHttp() {
//        Map requestData = JsonUtil.json2Object(JsonUtil.object2Json(batchInferenceRequest), Map.class);
        try {
            while (true) {
                long startTime = System.currentTimeMillis();
                String result = HttpClientPool.post("http://127.0.0.1:8059/federation/v1/batchInference", params);
                long endTime = System.currentTimeMillis();
                Map resultObject = JsonUtil.json2Object(result, Map.class);
                // startTime | endTime | cost | result
                String content = StringUtils.join(Arrays.asList(startTime, endTime, endTime - startTime, result), " | ");

                // print info log
//                output(INFO_OUTPUT_FILE, content);

                // print error log
                if (resultObject.get("retcode") == null || (int) resultObject.get("retcode") != StatusCode.SUCCESS) {
                    output(ERROR_OUTPUT_FILE, content);
                }

//                atomicLong.addAndGet(1);

                // print sample log
//                resultObject.remove("batchDataList");
//                resultObject.remove("singleInferenceResultMap");
//                output(INFO_OUTPUT_FILE_SAMPLE, StringUtils.join(Arrays.asList(startTime, endTime, endTime - startTime, JSONObject.toJSONString(resultObject)), " | "));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void batchInference() {
        Map featureMap = Maps.newHashMap();
        for (int j = 0; j < 250; j++) {
            String a = String.format("x%s", j);
            featureMap.put(a, random.nextDouble());
        }

        long startTime = System.currentTimeMillis();
        BatchInferenceRequest batchInferenceRequest = new BatchInferenceRequest();
        batchInferenceRequest.setServiceId("2020040111152695637611");
        batchInferenceRequest.setCaseId(Long.toString(System.currentTimeMillis()));
        List<BatchInferenceRequest.SingleInferenceData> singleInferenceDataList = Lists.newArrayList();

        for (int i = 0; i < BATCH_DATA_SIZE; i++) {
            BatchInferenceRequest.SingleInferenceData singleInferenceData = new BatchInferenceRequest.SingleInferenceData();
            singleInferenceData.getFeatureData().putAll(featureMap);

            singleInferenceData.getSendToRemoteFeatureData().put("device_id", String.valueOf(i + 1));
            singleInferenceData.setIndex(i);
            singleInferenceDataList.add(singleInferenceData);
        }

        batchInferenceRequest.setBatchDataList(singleInferenceDataList);

        try {
            while (true) {
                ManagedChannel managedChannel = GRPC_CONNECTION_POOL.getManagedChannel(HOST, PORT);

                InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
                inferenceMessageBuilder.setBody(ByteString.copyFrom(JsonUtil.object2Json(batchInferenceRequest), "UTF-8"));

                InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
                InferenceServiceProto.InferenceMessage inferenceMessage = blockingStub.batchInference(inferenceMessageBuilder.build());

                long endTime = System.currentTimeMillis();
                String result = new String(inferenceMessage.getBody().toByteArray());
                Map resultObject = JsonUtil.json2Object(result, Map.class);
                // startTime | endTime | cost | result
                String content = StringUtils.join(Arrays.asList(startTime, endTime, endTime - startTime, result), " | ");

                // print info log
//                output(INFO_OUTPUT_FILE, content);

                // print error log
                if (resultObject.get("retcode") == null || (int) resultObject.get("retcode") != StatusCode.SUCCESS) {
                    output(ERROR_OUTPUT_FILE, content);
                }

//                atomicLong.addAndGet(1);

                // print sample log
//                resultObject.remove("batchDataList");
//                resultObject.remove("singleInferenceResultMap");
//                output(INFO_OUTPUT_FILE_SAMPLE, StringUtils.join(Arrays.asList(startTime, endTime, endTime - startTime, JSONObject.toJSONString(resultObject)), " | "));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void output(File file, String content) {
        try {
            try (RandomAccessFile randomFile = new RandomAccessFile(file, "rw")) {
                long fileLength = randomFile.length();
                randomFile.seek(fileLength);
                randomFile.writeBytes(content + "\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
