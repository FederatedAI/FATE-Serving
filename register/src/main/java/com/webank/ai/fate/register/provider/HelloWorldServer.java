//package com.webank.ai.fate.register.provider;
///**
// * Created by Darren on 2016/11/11.
// */
//
//import com.alibaba.fastjson.JSON;
//import com.google.common.collect.Maps;
//import com.google.protobuf.ByteString;
//import com.webank.ai.fate.api.serving.InferenceServiceGrpc;
//import com.webank.ai.fate.api.serving.InferenceServiceProto;
//import com.webank.ai.fate.register.annotions.RegisterService;
//import io.grpc.Server;
//import io.grpc.ServerBuilder;
//import io.grpc.stub.StreamObserver;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//
//
//public class HelloWorldServer {
//    private static final Logger logger = LogManager.getLogger();
//
//    /* The port on which the server should run */
//    private int port = 50051;
//    private Server server;
//
//    private void start() throws IOException {
//        server = ServerBuilder.forPort(port)
//                .addService(new GreeterImpl())
//                .build()
//                .start();
//        logger.info("Server started, listening on " + port);
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
//                System.err.println("*** shutting down gRPC server since JVM is shutting down");
//                HelloWorldServer.this.stop();
//                System.err.println("*** server shut down");
//            }
//        });
//    }
//
//    private void stop() {
//        if (server != null) {
//            server.shutdown();
//        }
//    }
//
//    /**
//     * Await termination on the main thread since the grpc library uses daemon threads.
//     */
//    private void blockUntilShutdown() throws InterruptedException {
//        if (server != null) {
//            server.awaitTermination();
//        }
//    }
//
//    /**
//     * Main launches the server from the command line.
//     */
//    public static void main(String[] args) throws IOException, InterruptedException {
//        final HelloWorldServer server = new HelloWorldServer();
//        server.start();
//        server.blockUntilShutdown();
//    }
//
//    private class GreeterImpl extends InferenceServiceGrpc.InferenceServiceImplBase {
//        /**
//         * 原子Integer
//         */
//        public AtomicInteger count = new AtomicInteger(0);
//        @RegisterService(groupKey="fate-test",commandKey="fate-cmd")
//        @Override
//        public void startInferenceJob(InferenceServiceProto.InferenceMessage request,
//                                      StreamObserver<InferenceServiceProto.InferenceMessage> responseObserver) {
//            InferenceServiceProto.InferenceMessage.Builder responseBuilder = InferenceServiceProto.InferenceMessage.newBuilder();
//
//            Map result = Maps.newHashMap();
//
//            result.put("name", "god");
//
//            responseBuilder.setBody(ByteString.copyFrom(JSON.toJSONString(result).getBytes()));
////
////            try {
////                Thread.sleep(100);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//            System.err.println("receive " + request.getBody().toStringUtf8());
//
//
//            responseObserver.onNext(responseBuilder.build());
//            responseObserver.onCompleted();
//
//
//        }
//
//
//    }
//}