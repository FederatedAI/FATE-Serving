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

package com.webank.ai.fate.jmx.server;

import com.webank.ai.fate.jmx.mbean.Sample;
import com.webank.ai.fate.jmx.util.GetSystemInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.management.*;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;

public class FateMBeanServer {

    private static final Logger LOGGER = LogManager.getLogger(FateMBeanServer.class);

    private MBeanServer mBeanServer;

    private JMXConnectorServer jmxConnectorServer;

    /**
     * FateMBeanServer Constructor
     *
     * @param mBeanServer
     * @param initMBeans  auto register define mbeans
     */
    public FateMBeanServer(MBeanServer mBeanServer, boolean initMBeans) {
        this.mBeanServer = mBeanServer;
        try {
            if (initMBeans) {
                init();
            }
        } catch (Exception e) {
            LOGGER.error("Fate MBean server init fail");
            e.printStackTrace();
        }
    }

    private void init() throws MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
        LOGGER.info("Init register all MBeans");

        // register any mbeans
        registerMBean(new Sample(), new ObjectName("com.webank.ai.fate.jmx:name=sample"));
        // TODO: add mbeans
        // ...
    }

    public void registerMBean(Object object, ObjectName objectName)
            throws NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        this.mBeanServer.registerMBean(object, objectName);

        LOGGER.info("Server registerMBean {}", objectName);
    }

    public String openJMXServer(String serverName, int port) throws IOException {
        LocateRegistry.createRegistry(port);
        String url = "service:jmx:rmi:///jndi/rmi://" + GetSystemInfo.getLocalIp() + ":" + port + "/" + serverName;
        JMXServiceURL jmxServiceURL = new JMXServiceURL(url);
        jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceURL, null, mBeanServer);
        jmxConnectorServer.start();
        LOGGER.info("JMX Server started listening on port: {}, url: {}", port, url);
        return url;
    }

    public void stopJMXServer() throws IOException {

        if (jmxConnectorServer != null) {
            jmxConnectorServer.stop();
        }
    }

}
