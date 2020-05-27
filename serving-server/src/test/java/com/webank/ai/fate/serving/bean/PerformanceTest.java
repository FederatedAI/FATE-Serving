package com.webank.ai.fate.serving.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
import com.webank.ai.fate.api.serving.InferenceServiceProto;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.GrpcConnectionPool;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import io.grpc.ManagedChannel;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceTest {

    private static int PROCESS_NUM = 100;// 并发线程数
    private static int BATCH_DATA_SIZE = 300;// 单批次数量
    private static String HOST = "localhost";
    private static int PORT = 8000;
    private static File INFO_OUTPUT_FILE = new File("logs/performance-info.log");
    private static File INFO_OUTPUT_FILE_SAMPLE = new File("logs/performance-info-sample.log");
    private static File ERROR_OUTPUT_FILE = new File("logs/performance-error.log");
    private static Random random = new Random();
    private static GrpcConnectionPool GRPC_CONNECTION_POOL = GrpcConnectionPool.getPool();
    private static ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(), 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.AbortPolicy());
    private static ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
    private static AtomicLong atomicLong = new AtomicLong(0);

    public static void main(String[] args) throws IOException {
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
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            for (int i = 0; i < PROCESS_NUM; i++) {
                call();
            }
        }, 1000, 1000, TimeUnit.MILLISECONDS);

//        scheduledExecutorService.scheduleAtFixedRate(() -> {
//            System.out.println("==============" + atomicLong.get() + " times");
//        }, 1000, 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * 单次调用
     */
    private static void call() {
        THREAD_POOL_EXECUTOR.submit(() -> {
            long startTime = System.currentTimeMillis();
            BatchInferenceRequest batchInferenceRequest = new BatchInferenceRequest();
            batchInferenceRequest.setServiceId("fm");
            batchInferenceRequest.setCaseId(Long.toString(System.currentTimeMillis()));
            List<BatchInferenceRequest.SingleInferenceData> singleInferenceDataList = Lists.newArrayList();
            for (int i = 0; i < BATCH_DATA_SIZE; i++) {
                BatchInferenceRequest.SingleInferenceData singleInferenceData = new BatchInferenceRequest.SingleInferenceData();

                singleInferenceData.getFeatureData().put("x0", random.nextDouble());
                singleInferenceData.getFeatureData().put("x1", random.nextDouble());
                singleInferenceData.getFeatureData().put("x2", random.nextDouble());
                singleInferenceData.getFeatureData().put("x3", random.nextDouble());
                singleInferenceData.getFeatureData().put("x4", random.nextDouble());
                singleInferenceData.getFeatureData().put("x5", random.nextDouble());
                singleInferenceData.getFeatureData().put("x6", random.nextDouble());
                singleInferenceData.getFeatureData().put("x7", random.nextDouble());

                singleInferenceData.getSendToRemoteFeatureData().putAll(singleInferenceData.getFeatureData());
                singleInferenceData.setIndex(i);
                singleInferenceDataList.add(singleInferenceData);
            }

            batchInferenceRequest.setBatchDataList(singleInferenceDataList);

            try {
                ManagedChannel managedChannel = GRPC_CONNECTION_POOL.getManagedChannel(HOST, PORT);

                InferenceServiceProto.InferenceMessage.Builder inferenceMessageBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
                inferenceMessageBuilder.setBody(ByteString.copyFrom(JSON.toJSONString(batchInferenceRequest), "UTF-8"));

                inferenceMessageBuilder.setBody(ByteString.copyFrom(inferenceMessageBuilder.build().toByteArray()));

                InferenceServiceGrpc.InferenceServiceBlockingStub blockingStub = InferenceServiceGrpc.newBlockingStub(managedChannel);
                InferenceServiceProto.InferenceMessage inferenceMessage = blockingStub.batchInference(inferenceMessageBuilder.build());

                long endTime = System.currentTimeMillis();
                String result = new String(inferenceMessage.getBody().toByteArray());
                JSONObject resultObject = JSONObject.parseObject(result);
                // startTime | endTime | cost | result
                String content = StringUtils.join(Arrays.asList(startTime, endTime, endTime - startTime, result), " | ");

                // print info log
                output(INFO_OUTPUT_FILE, content);

                // print error log
                if (resultObject.get("retcode") == null || !resultObject.getString("retcode").equals(StatusCode.SUCCESS)) {
                    output(ERROR_OUTPUT_FILE, content);
                }

//                atomicLong.addAndGet(1);

                // print sample log
//                resultObject.remove("batchDataList");
//                resultObject.remove("singleInferenceResultMap");
//                output(INFO_OUTPUT_FILE_SAMPLE, StringUtils.join(Arrays.asList(startTime, endTime, endTime - startTime, JSONObject.toJSONString(resultObject)), " | "));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
