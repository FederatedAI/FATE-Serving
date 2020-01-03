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
import com.webank.ai.fate.register.zookeeper.ZookeeperClient;
import com.webank.ai.fate.register.zookeeper.ZookeeperTransporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.webank.ai.fate.register.common.Constants.BACKUP_KEY;
import static com.webank.ai.fate.register.common.Constants.TIMEOUT_KEY;


public abstract class AbstractZookeeperTransporter implements ZookeeperTransporter {
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, ZookeeperClient> zookeeperClientMap = new ConcurrentHashMap<>();

    /**
     * share connnect for registry, metadata, etc..
     * <p>
     * Make sure the connection is connected.
     *
     * @param url
     * @return
     */
    @Override
    public ZookeeperClient connect(URL url) {
        ZookeeperClient zookeeperClient;
        List<String> addressList = getURLBackupAddress(url);
        // The field define the zookeeper server , including protocol, host, port, username, password
        if ((zookeeperClient = fetchAndUpdateZookeeperClientCache(addressList)) != null && zookeeperClient.isConnected()) {
            logger.info("find valid zookeeper client from the cache for address: " + url);
            return zookeeperClient;
        }
        // avoid creating too many connections， so add lock
        synchronized (zookeeperClientMap) {
            if ((zookeeperClient = fetchAndUpdateZookeeperClientCache(addressList)) != null && zookeeperClient.isConnected()) {
                logger.info("find valid zookeeper client from the cache for address: " + url);
                return zookeeperClient;
            }

            zookeeperClient = createZookeeperClient(toClientURL(url));
            logger.info("No valid zookeeper client found from cache, therefore create a new client for url. " + url);
            writeToClientMap(addressList, zookeeperClient);
        }
        return zookeeperClient;
    }

    protected abstract ZookeeperClient createZookeeperClient(URL url);


    ZookeeperClient fetchAndUpdateZookeeperClientCache(List<String> addressList) {

        ZookeeperClient zookeeperClient = null;
        for (String address : addressList) {
            if ((zookeeperClient = zookeeperClientMap.get(address)) != null && zookeeperClient.isConnected()) {
                break;
            }
        }
        if (zookeeperClient != null && zookeeperClient.isConnected()) {
            writeToClientMap(addressList, zookeeperClient);
        }
        return zookeeperClient;
    }

    /**
     * get all zookeeper urls (such as :zookeeper://127.0.0.1:2181?127.0.0.1:8989,127.0.0.1:9999)
     *
     * @param url such as:zookeeper://127.0.0.1:2181?127.0.0.1:8989,127.0.0.1:9999
     * @return such as 127.0.0.1:2181,127.0.0.1:8989,127.0.0.1:9999
     */
    List<String> getURLBackupAddress(URL url) {
        List<String> addressList = new ArrayList<String>();
        addressList.add(url.getAddress());

        addressList.addAll(url.getParameter(BACKUP_KEY, Collections.EMPTY_LIST));
        return addressList;
    }

    /**
     * write address-ZookeeperClient relationship to Map
     *
     * @param addressList
     * @param zookeeperClient
     */
    void writeToClientMap(List<String> addressList, ZookeeperClient zookeeperClient) {
        for (String address : addressList) {
            zookeeperClientMap.put(address, zookeeperClient);
        }
    }

    /**
     * redefine the url for zookeeper. just keep protocol, username, password, host, port, and individual parameter.
     *
     * @param url
     * @return
     */
    URL toClientURL(URL url) {
        Map<String, String> parameterMap = new HashMap<>();
        // for CuratorZookeeperClient
        if (url.getParameter(TIMEOUT_KEY) != null) {
            parameterMap.put(TIMEOUT_KEY, url.getParameter(TIMEOUT_KEY));
        }
        if (url.getParameter(BACKUP_KEY) != null) {
            parameterMap.put(BACKUP_KEY, url.getParameter(BACKUP_KEY));
        }
        return new URL(url.getProtocol(), url.getProject(), url.getEnvironment(), url.getHost(), url.getPort(),
                ZookeeperTransporter.class.getName(), parameterMap);
    }


    Map<String, ZookeeperClient> getZookeeperClientMap() {
        return zookeeperClientMap;
    }
}
