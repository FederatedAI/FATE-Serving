FATE-Serving 2.0.4 版本开始支持TLS双向认证，数据使用方和数据提供方分别作为客户端和服务端，启用TLS认证后双方交互时会进行安全性认证，配置所需文件由服务端生成提供给客户端。要使用TLS，需要以PEM格式指定证书链和私钥。  
开启TLS认证需要在serving-proxy组件中配置，配置如下：  

## 服务端配置  
•	application.properties配置   
```yaml
# only support PLAINTEXT, TLS(we use Mutual TLS here), if use TSL authentication
proxy.grpc.inter.negotiationType=TLS
# only needs to be set when negotiationType is TLS
proxy.grpc.inter.CA.file=/data/projects/fate-serving/serving-proxy/conf/ssl/ca.crt
# negotiated server side certificates
proxy.grpc.inter.server.certChain.file=/data/projects/fate-serving/serving-proxy/conf/ssl/server.crt
proxy.grpc.inter.server.privateKey.file=/data/projects/fate-serving/serving-proxy/conf/ssl/server.pem
```

## 客户端配置 
#### 2.1.0之前：
•	application.properties配置  
```yaml
# only support PLAINTEXT, TLS(we use Mutual TLS here), if use TSL authentication
proxy.grpc.inter.negotiationType=TLS
# only needs to be set when negotiationType is TLS
proxy.grpc.inter.CA.file=/data/projects/fate-serving/serving-proxy/conf/ssl/ca.crt
# negotiated client side certificates
proxy.grpc.inter.client.certChain.file=/data/projects/fate-serving/serving-proxy/conf/ssl/client.crt
proxy.grpc.inter.client.privateKey.file=/data/projects/fate-serving/serving-proxy/conf/ssl/client.pem
```

•	route_table.json配置  
```json
{
  "route_table": {
    "default": {
      "default": [
        {
          "ip": "127.0.0.1",
          "port": 9999,
          "useSSL": true        # 配置对外节点时，需要将useSSL配置成true，client端请求时将携带证书
        }
      ]
    },
      ......
  }
}
```

#### 2.1.0之后：
由于FATE-Serving要支持多host预测，所以客户端guest方需要在route_table内配置安全证书。 
 
•   route_table.json配置如下：
```json
{
  "route_table": {
    "default": {
      "default": [
        {
          "ip": "127.0.0.1",
          "port": 9999,
          "useSSL": true        # 配置对外节点时，需要将useSSL配置成true，client端请求时将携带证书
          "negotiationType": "TLS",
          "certChainFile": "/data/projects/fate-serving/serving-proxy/conf/ssl/client.crt ",
	      "privateKeyFile": "/data/projects/fate-serving/serving-proxy/conf/ssl/client.pem",
	      "caFile": "/data/projects/fate-serving/serving-proxy/conf/ssl/ca.crt"
        }
      ]
    },
    ......
  }
}

```
