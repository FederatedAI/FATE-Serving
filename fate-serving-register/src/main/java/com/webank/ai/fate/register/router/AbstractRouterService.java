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

package com.webank.ai.fate.register.router;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.webank.ai.fate.register.common.AbstractRegistry;
import com.webank.ai.fate.register.common.Constants;
import com.webank.ai.fate.register.common.RouterMode;
import com.webank.ai.fate.register.interfaces.Registry;
import com.webank.ai.fate.register.loadbalance.DefaultLoadBalanceFactory;
import com.webank.ai.fate.register.loadbalance.LoadBalanceModel;
import com.webank.ai.fate.register.loadbalance.LoadBalancer;
import com.webank.ai.fate.register.loadbalance.LoadBalancerFactory;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractRouterService implements RouterService {

    protected LoadBalancerFactory loadBalancerFactory = new DefaultLoadBalanceFactory();
    protected AbstractRegistry registry;
    Logger logger = LoggerFactory.getLogger(AbstractRouterService.class);

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(AbstractRegistry registry) {
        this.registry = registry;
    }


    @Override
    public List<URL> router(URL url, LoadBalanceModel loadBalanceModel) {

        LoadBalancer loadBalancer = loadBalancerFactory.getLoaderBalancer(loadBalanceModel);
        return doRouter(url, loadBalancer);
    }

    @Override
    public List<URL> router(String project, String environment, String serviceName) {

        Preconditions.checkArgument(StringUtils.isNotEmpty(project));
        Preconditions.checkArgument(StringUtils.isNotEmpty(environment));
        Preconditions.checkArgument(StringUtils.isNotEmpty(serviceName));
        LoadBalancer loadBalancer = loadBalancerFactory.getLoaderBalancer(LoadBalanceModel.random_with_weight);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(project).append("/").append(environment).append("/").append(serviceName);
        URL paramUrl = URL.valueOf(stringBuilder.toString());
        return doRouter(paramUrl, loadBalancer);
    }

    public abstract List<URL> doRouter(URL url, LoadBalancer loadBalancer);

    @Override
    public List<URL> router(URL url) {
        return this.router(url, LoadBalanceModel.random);
    }

    protected List<URL> filterVersion(List<URL> urls, long version) {
        /*if (StringUtils.isEmpty(version)) {
            return urls;
        }*/
        final List<URL> resultUrls = Lists.newArrayList();

        if (CollectionUtils.isNotEmpty(urls)) {

            urls.forEach(url -> {

                String routerMode = url.getParameter(Constants.ROUTER_MODE);
                try {
                    if (RouterMode.ALL_ALLOWED.name().equals(routerMode)) {
                        resultUrls.add(url);
                        return;
                    }

                    String targetVersion = url.getParameter(Constants.VERSION_KEY);
                    if (StringUtils.isBlank(targetVersion)) {
                        return;
                    }

                    long targetVersionValue = Long.parseLong(targetVersion);
                    long versionValue = version;

                    if (targetVersionValue != 0 && versionValue != 0) {
                        if (String.valueOf(RouterMode.VERSION_BIGER).equalsIgnoreCase(routerMode)) {
                            if (versionValue > targetVersionValue) {
                                resultUrls.add(url);
                            }
                        } else if (String.valueOf(RouterMode.VERSION_BIGTHAN_OR_EQUAL).equalsIgnoreCase(routerMode)) {
                            if (versionValue >= targetVersionValue) {
                                resultUrls.add(url);
                            }
                        } else if (String.valueOf(RouterMode.VERSION_SMALLER).equalsIgnoreCase(routerMode)) {
                            if (versionValue < targetVersionValue) {
                                resultUrls.add(url);
                            }
                        } else if (String.valueOf(RouterMode.VERSION_EQUALS).equalsIgnoreCase(routerMode)) {
                            if (versionValue == targetVersionValue) {
                                resultUrls.add(url);
                            }
                        } else {
                            resultUrls.add(url);
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("parse version error");

                }
            });
        }
        return resultUrls;
    }

}
