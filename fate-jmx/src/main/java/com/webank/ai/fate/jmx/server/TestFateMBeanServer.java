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

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class TestFateMBeanServer {

    public static void main(String[] args) {
        try {
            FateMBeanServer fateMBeanServer = new FateMBeanServer(ManagementFactory.getPlatformMBeanServer(), false);
            fateMBeanServer.registerMBean(new Sample(), new ObjectName("com.webank.ai.fate.jmx:name=sample,type=standard"));

            // use default domain
//            FateMBeanServer fateMBeanServer = new FateMBeanServer(MBeanServerFactory.createMBeanServer("com.webank.ai.fate.jmx"), false);
//            fateMBeanServer.registerMBean(new Sample(), new ObjectName(":name=sample,type=standard"));
            fateMBeanServer.openJMXServer("fate", 9999);

            // service:jmx:rmi:///jndi/rmi://10.56.224.80:9999/fate
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
