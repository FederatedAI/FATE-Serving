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

package com.webank.ai.fate.jmx.client;

import com.webank.ai.fate.jmx.mbean.SampleMBean;
import com.webank.ai.fate.jmx.util.GetSystemInfo;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Set;

public class FateJmxClient {
    public static void main(String[] args) throws IOException, MalformedObjectNameException {
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + GetSystemInfo.getLocalIp() + ":9999/fate");
        JMXConnector connector = JMXConnectorFactory.connect(url, null);

        MBeanServerConnection mbsc = connector.getMBeanServerConnection();

        for (String domain : mbsc.getDomains()) {
            System.out.println("domain = " + domain);
        }

        System.out.println("MBeanServer default domain = " + mbsc.getDefaultDomain());

        Set<ObjectName> objectNames = mbsc.queryNames(null, null);
        for (ObjectName objectName : objectNames) {
            System.out.println("objectName = " + objectName);
        }

        SampleMBean proxy = JMX.newMBeanProxy(mbsc, new ObjectName("com.webank.ai.fate.jmx:name=sample,type=standard"), SampleMBean.class, true);
        proxy.call();
    }
}
