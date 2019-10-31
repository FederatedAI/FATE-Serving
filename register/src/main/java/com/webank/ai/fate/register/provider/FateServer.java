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

package com.webank.ai.fate.register.provider;

import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.register.interfaces.Registry;
import io.grpc.Server;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class FateServer extends Server {


    private static final Logger logger = LogManager.getLogger(FateServer.class);
    public static Set<RegisterService> serviceSets = new HashSet<>();
    public String project;
    Server server;
    private String environment;
    private Registry registry;


    public FateServer() {
    }

    public FateServer(Server server) {
        this();

        this.server = server;

    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public Server start() throws IOException {
        this.server.start();
        // register();
        return this;
    }

    @Override
    public Server shutdown() {
        logger.info("grpc server prepare shutdown");
//            registry.destroy();
        this.server.shutdown();
        logger.info("grpc server shutdown!!!!!!!");
        return this;
    }

    @Override
    public Server shutdownNow() {
        this.server.shutdownNow();
        return this;
    }

    @Override
    public boolean isShutdown() {
        return this.server.isShutdown();

    }

    @Override
    public boolean isTerminated() {
        return this.server.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return this.server.awaitTermination(l, timeUnit);
    }

    @Override
    public void awaitTermination() throws InterruptedException {
        this.server.awaitTermination();
    }


//        public  static  void  main(String[]  args){
//
//            FateServer fateServer = new FateServer();
//
//            FateServer.serviceRegister.put("test1","");
//            FateServer.serviceRegister.put("test2","");
//
//            FateServer.register();
//            FateServer.lookup("test2");
//
//            List<URL>  cacheUrls =  ((AbstractRegistry)registry).getCacheUrls(URL.valueOf("/test1"));
//
//            System.err.println("cacheUrls==================="+cacheUrls);
//
//
//            while(true){
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//
//        }
}