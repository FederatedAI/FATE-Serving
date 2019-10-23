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

package com.webank.ai.fate.register.zookeeper;


import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.webank.ai.fate.register.annotions.RegisterService;
import com.webank.ai.fate.register.common.*;
import com.webank.ai.fate.register.interfaces.NotifyListener;
import com.webank.ai.fate.register.url.CollectionUtils;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.url.UrlUtils;
import com.webank.ai.fate.register.utils.NetUtils;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.register.utils.URLBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.webank.ai.fate.register.common.Constants.*;
import static org.apache.curator.utils.ZKPaths.PATH_SEPARATOR;


public class ZookeeperRegistry extends FailbackRegistry {


    private static final Logger logger = LogManager.getLogger();
    private final static int DEFAULT_ZOOKEEPER_PORT = 2181;
    private final static String DEFAULT_ROOT = "FATE-SERVICES";
    private final static String ROOT_KEY = "root";
    public static ConcurrentMap<URL, ZookeeperRegistry> registeryMap = new ConcurrentHashMap();
    private final String root;
    private final ConcurrentMap<URL, ConcurrentMap<NotifyListener, ChildListener>> zkListeners = new ConcurrentHashMap<>();

    ;
    private final ZookeeperClient zkClient;
    Set<String> registedString = Sets.newHashSet();
    String DYNAMIC_KEY = "dynamic";
    Set<String> anyServices = new HashSet<String>();
    private String environment;
    private Set<String> dynamicEnvironments = new HashSet<String>();
    private String project;
    private int port;
    public ZookeeperRegistry(URL url, ZookeeperTransporter zookeeperTransporter) {
        super(url);
//
        String group = url.getParameter(ROOT_KEY, DEFAULT_ROOT);
        if (!group.startsWith(PATH_SEPARATOR)) {
            group = PATH_SEPARATOR + group;
        }
        this.environment = url.getParameter(ENVIRONMENT_KEY, "online");
        project = url.getParameter(PROJECT_KEY);
        port = url.getParameter(SERVER_PORT) != null ? new Integer(url.getParameter(SERVER_PORT)) : 0;

        this.root = group;
        zkClient = zookeeperTransporter.connect(url);
        zkClient.addStateListener(state -> {

            if (state == StateListener.RECONNECTED) {
                logger.error("state listenner reconnected");
                try {
                    recover();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    public static synchronized ZookeeperRegistry getRegistery(String url, String project, String environment, int port) {

        if (url == null) {
            return null;
        }
        URL registryUrl = URL.valueOf(url);
        registryUrl = registryUrl.addParameter(Constants.ENVIRONMENT_KEY, environment);
        registryUrl = registryUrl.addParameter(Constants.SERVER_PORT, port);
        registryUrl = registryUrl.addParameter(Constants.PROJECT_KEY, project);
        List<URL> backups = registryUrl.getBackupUrls();

        if (registeryMap.get(registryUrl) == null) {
            URL finalRegistryUrl = registryUrl;
            registeryMap.computeIfAbsent(registryUrl, n -> {
                CuratorZookeeperTransporter curatorZookeeperTransporter = new CuratorZookeeperTransporter();
                ZookeeperRegistryFactory zookeeperRegistryFactory = new ZookeeperRegistryFactory();
                zookeeperRegistryFactory.setZookeeperTransporter(curatorZookeeperTransporter);
                ZookeeperRegistry zookeeperRegistry = (ZookeeperRegistry) zookeeperRegistryFactory.createRegistry(finalRegistryUrl);
                return zookeeperRegistry;
            });

        }
        return registeryMap.get(registryUrl);

    }

    @Override
    public void doSubProject(String project) {

        String path = root + Constants.PATH_SEPARATOR + project;


        List<String> environments = zkClient.addChildListener(path, (parent, childrens) -> {
            if (StringUtils.isNotEmpty(parent)) {
                logger.info("fire environments changes {}", childrens);
                subEnvironments(path, project, childrens);
            }
        });
        logger.info("environments {}", environments);
        if (environments == null) {
            logger.info("environment is null,maybe zk is not started");
            throw new RuntimeException("environment is null");
        }

        subEnvironments(path, project, environments);
    }

    private void subEnvironments(String path, String project, List<String> environments) {

        if (environments != null) {

            for (String environment : environments) {

                String tempPath = path + Constants.PATH_SEPARATOR + environment;


                List<String> services = zkClient.addChildListener(tempPath, (parent, childrens) -> {

                    if (StringUtils.isNotEmpty(parent)) {
                        logger.info("fire services changes {}", childrens);

                        subServices(project, environment, childrens);
                    }

                });


                subServices(project, environment, services);
            }
        }
    }

    private void subServices(String project, String environment, List<String> services) {

        if (services != null) {
            for (String service : services) {

                String subString = project + Constants.PATH_SEPARATOR + environment + Constants.PATH_SEPARATOR + service;
                logger.info("subServices sub {}", subString);
                subscribe(URL.valueOf(subString), urls -> {

                    logger.info("change services urls =" + urls);
                });
            }
        }

    }

    private String parseRegisterService(RegisterService registerService) {

        String serviceName = registerService.serviceName();
        String version = registerService.version();
        String param = "?";
        RouterModel routerModel = registerService.routerModel();
        param = param + Constants.ROUTER_MODEL + "=" + routerModel.name();
        param = param + "&";
        param = param + Constants.TIMESTAMP_KEY + "=" + System.currentTimeMillis();
        String key = serviceName;
        boolean appendParam = false;
        if (StringUtils.isNotEmpty(version)) {
            param = param + "&" + Constants.VERSION + "=" + version;
        }
        if (this.getServieWeightMap().containsKey(serviceName + ".weight")) {
            int weight = this.getServieWeightMap().get(serviceName + ".weight");
            param = param + "&" + Constants.WEIGHT_KEY + "=" + weight;
        }
        key = key + param;
        return key;
    }

    public synchronized void register(Set<RegisterService> sets) {

        String hostAddress = NetUtils.getLocalIp();
        Preconditions.checkArgument(port != 0);
        Preconditions.checkArgument(StringUtils.isNotEmpty(environment));

        Set<URL> registered = this.getRegistered();
        for (RegisterService service : sets) {
            URL serviceUrl = URL.valueOf("grpc://" + hostAddress + ":" + port + Constants.PATH_SEPARATOR + parseRegisterService(service));
            if (service.useDynamicEnvironment()) {

                if (CollectionUtils.isNotEmpty(dynamicEnvironments)) {
                    dynamicEnvironments.forEach(environment -> {
                        URL newServiceUrl = serviceUrl.setEnvironment(environment);
                        String serviceName = service.serviceName() + environment;
                        if (!registedString.contains(serviceName)) {
                            this.register(newServiceUrl);
                            this.registedString.add(serviceName);
                        } else {
                            logger.info("url {} is already registed,will not do anything ", newServiceUrl);
                        }
                    });
                }
            } else {
                if (!registedString.contains(service.serviceName())) {
                    this.register(serviceUrl);
                    this.registedString.add(service.serviceName());
                }
            }
        }
        logger.info("registed urls {}", registered);
    }

    public void addDynamicEnvironment(String environment) {
        dynamicEnvironments.add(environment);
    }

    @Override
    public boolean isAvailable() {
        return zkClient.isConnected();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            zkClient.close();
        } catch (Exception e) {
            logger.warn("Failed to close zookeeper client " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void doRegister(URL url) {
        try {

            String urlPath = toUrlPath(url);
            logger.info("create urlpath {} ", urlPath);
            zkClient.create(urlPath, true);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to register " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void doUnregister(URL url) {
        try {
            zkClient.delete(toUrlPath(url));
        } catch (Throwable e) {
            throw new RuntimeException("Failed to unregister " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void doSubscribe(final URL url, final NotifyListener listener) {
        try {


            List<URL> urls = new ArrayList<>();
            if (ANY_VALUE.equals(url.getEnvironment())) {

                String root = toRootPath();
                ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
                if (listeners == null) {
                    zkListeners.putIfAbsent(url, new ConcurrentHashMap<>());
                    listeners = zkListeners.get(url);
                }
                ChildListener zkListener = listeners.get(listener);
                if (zkListener == null) {
                    listeners.putIfAbsent(listener, (parentPath, currentChilds) -> {

                        if (parentPath.equals(Constants.PROVIDERS_CATEGORY)) {
                            for (String child : currentChilds) {
                                child = URL.decode(child);
                                if (!anyServices.contains(child)) {
                                    anyServices.add(child);
                                    subscribe(url.setPath(child).addParameters(INTERFACE_KEY, child,
                                            Constants.CHECK_KEY, String.valueOf(false)), listener);
                                }
                            }


                        }


                    });
                    zkListener = listeners.get(listener);

                }

                String xx = root + "/" + url.getProject();
                List<String> children = zkClient.addChildListener(xx, zkListener);


                for (String environment : children) {

                    //URL  childUrl =  url.setEnvironment(environment);
                    //urls.add(childUrl);

                    xx = xx + "/" + environment;
                    List<String> interfaces = zkClient.addChildListener(xx, zkListener);

                    if (interfaces != null) {
                        for (String inter : interfaces) {
                            xx = xx + "/" + inter + "/" + Constants.PROVIDERS_CATEGORY;

                            List<String> services = zkClient.addChildListener(xx, zkListener);

                            if (services != null) {
                                urls.addAll(toUrlsWithEmpty(url, xx, services));
                            }


                        }

                    }
                }
                notify(url, listener, urls);


            }


//            if (ANY_VALUE.equals(url.getServiceInterface())) {
//                String root = toRootPath();
//                ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
//                if (listeners == null) {
//                    zkListeners.putIfAbsent(url, new ConcurrentHashMap<>());
//                    listeners = zkListeners.get(url);
//                }
//                ChildListener zkListener = listeners.get(listener);
//                if (zkListener == null) {
//                    listeners.putIfAbsent(listener, (parentPath, currentChilds) -> {
//                        for (String child : currentChilds) {
//                            child = URL.decode(child);
//                            if (!anyServices.contains(child)) {
//                                anyServices.add(child);
//                                subscribe(url.setPath(child).addParameters(INTERFACE_KEY, child,
//                                        Constants.CHECK_KEY, String.valueOf(false)), listener);
//                            }
//                        }
//                    });
//                    zkListener = listeners.get(listener);
//                }
//                zkClient.create(root, false);
//                List<String> services = zkClient.addChildListener(root, zkListener);
//                if (CollectionUtils.isNotEmpty(services)) {
//                    for (String service : services) {
//                        service = URL.decode(service);
//                        anyServices.add(service);
//                        subscribe(url.setPath(service).addParameters(INTERFACE_KEY, service,
//                                Constants.CHECK_KEY, String.valueOf(false)), listener);
//                    }
//                }
//            }
            else {

                for (String path : toCategoriesPath(url)) {
                    ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
                    if (listeners == null) {
                        zkListeners.putIfAbsent(url, new ConcurrentHashMap<>());
                        listeners = zkListeners.get(url);
                    }
                    ChildListener zkListener = listeners.get(listener);
                    if (zkListener == null) {
                        listeners.putIfAbsent(listener, (parentPath, currentChilds) -> {
                                    if (StringUtils.isNotEmpty(parentPath)) {
                                        ZookeeperRegistry.this.notify(url, listener,
                                                toUrlsWithEmpty(url, parentPath, currentChilds));
                                    }

                                }
                        );
                        zkListener = listeners.get(listener);
                    }
                    zkClient.create(path, false);
                    List<String> children = zkClient.addChildListener(path, zkListener);
                    if (children != null) {
                        urls.addAll(toUrlsWithEmpty(url, path, children));
                    }
                }
                notify(url, listener, urls);

                // }
            }


        } catch (Throwable e) {
            throw new RuntimeException("Failed to subscribe " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {
        ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
        if (listeners != null) {
            ChildListener zkListener = listeners.get(listener);
            if (zkListener != null) {
                for (String path : toCategoriesPath(url)) {
                    zkClient.removeChildListener(path, zkListener);
                }

            }
        }
    }

    @Override
    public List<URL> lookup(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("lookup url == null");
        }
        try {
            List<String> providers = new ArrayList<>();
            for (String path : toCategoriesPath(url)) {
                List<String> children = zkClient.getChildren(path);
                if (children != null) {
                    providers.addAll(children);
                }
            }
            return toUrlsWithoutEmpty(url, providers);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to lookup " + url + " from zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    private String toRootDir() {
        if (root.equals(PATH_SEPARATOR)) {
            return root;
        }
        return root + PATH_SEPARATOR;


    }

    private String toRootPath() {
        return root;
    }

    private String toServicePath(URL url) {
        String project = url.getProject() != null ? url.getProject() : this.project;
        String environment = url.getEnvironment() != null ? url.getEnvironment() : this.environment;
        String name = url.getServiceInterface();
        if (ANY_VALUE.equals(name)) {
            return toRootPath();
        }

        String result = toRootDir() + project + Constants.PATH_SEPARATOR + environment + Constants.PATH_SEPARATOR + URL.encode(name);
        return result;
    }

    private String[] toCategoriesPath(URL url) {
        String[] categories;
        if (ANY_VALUE.equals(url.getParameter(CATEGORY_KEY))) {
            categories = new String[]{PROVIDERS_CATEGORY, CONSUMERS_CATEGORY, ROUTERS_CATEGORY, CONFIGURATORS_CATEGORY};
        } else {
            categories = url.getParameter(CATEGORY_KEY, new String[]{DEFAULT_CATEGORY});
        }
        String[] paths = new String[categories.length];
        for (int i = 0; i < categories.length; i++) {
            paths[i] = toServicePath(url) + PATH_SEPARATOR + categories[i];
        }
        return paths;
    }

    private String toCategoryPath(URL url) {

        String servicePath = toServicePath(url);
        String category = url.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
        return servicePath + PATH_SEPARATOR + category;
    }

    private String toUrlPath(URL url) {
        return toCategoryPath(url) + PATH_SEPARATOR + URL.encode(url.toFullString());
    }

    private List<URL> toUrlsWithoutEmpty(URL consumer, List<String> providers) {
        List<URL> urls = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(providers)) {
            for (String provider : providers) {
                provider = URL.decode(provider);
                if (provider.contains(PROTOCOL_SEPARATOR)) {
                    URL url;
                    // for jmx url
                    if (provider.startsWith(JMX_PROTOCOL_KEY)) {
                        url = URL.parseJMXServiceUrl(provider);
                    } else {
                        url = URL.valueOf(provider);
                    }
                    if (UrlUtils.isMatch(consumer, url)) {
                        urls.add(url);
                    }
                }
            }
        }
        return urls;
    }

    private List<URL> toUrlsWithEmpty(URL consumer, String path, List<String> providers) {
        List<URL> urls = toUrlsWithoutEmpty(consumer, providers);
        if (urls == null || urls.isEmpty()) {
            int i = path.lastIndexOf(PATH_SEPARATOR);
            String category = i < 0 ? path : path.substring(i + 1);
            URL empty = URLBuilder.from(consumer)
                    .setProtocol(EMPTY_PROTOCOL)
                    .addParameter(CATEGORY_KEY, category)
                    .build();
            urls.add(empty);
        }
        return urls;
    }

}
