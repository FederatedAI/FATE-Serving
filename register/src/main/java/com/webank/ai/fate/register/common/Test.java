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

package com.webank.ai.fate.register.common;

import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistryFactory;

/**
 * @Description TODO
 * @Author kaideng
 **/
public class Test {


    public static void main(String[] args) {

        URL registryUrl = URL.valueOf("zookeeper://localhost:" + 2181);

        URL serviceUrl = URL.valueOf("zookeeper://zookeeper/" + "mytest" + "?notify=false&methods=test1,test2");

        ZookeeperRegistryFactory zookeeperRegistryFactory = new ZookeeperRegistryFactory();
        zookeeperRegistryFactory.setZookeeperTransporter(new CuratorZookeeperTransporter());
        ZookeeperRegistry zookeeperRegistry = (ZookeeperRegistry) zookeeperRegistryFactory.createRegistry(registryUrl);

        //  zookeeperRegistry.register(serviceUrl);

    }
}
