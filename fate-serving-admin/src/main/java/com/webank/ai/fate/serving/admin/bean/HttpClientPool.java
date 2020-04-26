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

package com.webank.ai.fate.serving.admin.bean;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class HttpClientPool {
    private static Logger logger = LoggerFactory.getLogger(HttpClientPool.class);

    private static PoolingHttpClientConnectionManager poolConnManager;
    private static RequestConfig requestConfig;
    private static CloseableHttpClient httpClient;

    private void config(HttpRequestBase httpRequestBase, Map<String, String> headers) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(500)
                .setConnectTimeout(500)
                .setSocketTimeout(2000).build();
        httpRequestBase.addHeader(Dict.CONTENT_TYPE, Dict.CONTENT_TYPE_JSON_UTF8);
        if (headers != null) {
            headers.forEach((key, value) -> {
                httpRequestBase.addHeader(key, value);
            });
        }
        httpRequestBase.setConfig(requestConfig);
    }

    @PostConstruct
    public void initPool() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register(
                    Dict.HTTP, PlainConnectionSocketFactory.getSocketFactory()).register(
                    Dict.HTTPS, sslsf).build();
            poolConnManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);
            poolConnManager.setMaxTotal(500);
            poolConnManager.setDefaultMaxPerRoute(200);
            int socketTimeout = 10000;
            int connectTimeout = 10000;
            int connectionRequestTimeout = 10000;
            requestConfig = RequestConfig.custom().setConnectionRequestTimeout(
                    connectionRequestTimeout).setSocketTimeout(socketTimeout).setConnectTimeout(
                    connectTimeout).build();
            httpClient = getConnection();
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            logger.error("init http client pool failed:", ex);
        }
    }

    private CloseableHttpClient getConnection() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager)
                .setDefaultRequestConfig(requestConfig)
                .evictExpiredConnections()
                .evictIdleConnections(5, TimeUnit.SECONDS)
                .setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
                .build();
        return httpClient;
    }

    public String sendPost(String url, Map<String, Object> requestData, Map<String, String> headers) {
        String requestDataJson = JSON.toJSONString(requestData);
        return sendPost(url, requestDataJson, headers);
    }

    public String sendPost(String url, String requestData, Map<String, String> headers) {
        HttpPost httpPost = new HttpPost(url);
        config(httpPost, headers);
        StringEntity stringEntity = new StringEntity(requestData, Dict.CHARSET_UTF8);
        stringEntity.setContentEncoding(Dict.CHARSET_UTF8);
        httpPost.setEntity(stringEntity);
        return getResponse(httpPost);
    }

    public String get(String url, Map<String, String> headers) {
        return sendGet(url, headers);
    }

    public String get(String url) {
        return sendGet(url, null);
    }

    public String sendGet(String url, Map<String, String> headers) {
        HttpGet httpGet = new HttpGet(url);
        config(httpGet, headers);
        return getResponse(httpGet);
    }

    private String getResponse(HttpRequestBase request) {
        HttpClientContext httpClientContext = HttpClientContext.create();
        try (CloseableHttpResponse response = httpClient.execute(request, httpClientContext)) {
            HttpEntity entity = response.getEntity();

            String result = EntityUtils.toString(entity, Dict.CHARSET_UTF8);
            EntityUtils.consume(entity);
            return result;
        } catch (IOException ex) {
            logger.error("get http response failed:", ex);
            return null;
        }
    }
}
