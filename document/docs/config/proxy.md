### application.properties配置
以下为conf/application.properties文件配置详解。源码中的配置文件没有罗列出所有配置，只保留了必需的配置， 其他配置都采用了默认值。如果需要可以根据以下表格来在配置文件中新增条目。  
例如：若需要把预测所用的grpc端口更改为8870，则在application.properties文件中手动添加: proxy.grpc.inter.port=8870

<table>
  <tr>
    <td>配置项</td>
    <td>配置项含义</td>
    <td>默认值</td>
  </tr>
  <tr>
    <td>server.port</td>
    <td>监听的http端口</td>
    <td>8059</td>
  </tr>
  <tr>
    <td>coordinator</td>
    <td>本方partid,此参数比较重要</td>
    <td>9999</td>
  </tr>
  <tr>
    <td>inference.service.name</td>
    <td>转发服务名称</td>
    <td>serving</td>
  </tr>
  <tr>
    <td>print.input.data</td>
    <td>访问日志中是否打印输入参数</td>
    <td>false</td>
  </tr>
  <tr>
    <td>print.output.data</td>
    <td>访问日志中是否打印返回结果</td>
    <td>false</td>
  </tr>
  <tr>
    <td>proxy.async.coresize</td>
    <td>异步处理http请求线程池核心线程数</td>
    <td>10</td>
  </tr>
  <tr>
    <td>proxy.async.maxsize</td>
    <td>异步处理http请求线程池最大线程数</td>
    <td>100</td>
  </tr>
  <tr>
    <td>proxy.async.timeout</td>
    <td>处理http请求的超时时间</td>
    <td>5000</td>
  </tr>
  <tr>
    <td>proxy.grpc.batch.inference.timeout</td>
    <td>批量预测请求的超时时间</td>
    <td>10000</td>
  </tr>
  <tr>
    <td>proxy.grpc.inference.timeout</td>
    <td>单笔预测请求的超时时间</td>
    <td>3000</td>
  </tr>
  <tr>
    <td>proxy.grpc.inter.port</td>
    <td>对集群外暴露的grpc端口</td>
    <td>8869</td>
  </tr>
  <tr>
    <td>proxy.grpc.intra.port</td>
    <td>对集群内暴露的grpc端口</td>
    <td>8879</td>
  </tr>
  <tr>
    <td>proxy.grpc.threadpool.coresize</td>
    <td>处理grpc请求的线程池的核心线程数</td>
    <td>50</td>
  </tr>
  <tr>
    <td>proxy.grpc.threadpool.maxsize</td>
    <td>处理grpc请求的线程池的最大线程数</td>
    <td>100</td>
  </tr>
  <tr>
    <td>proxy.grpc.threadpool.queuesize</td>
    <td>处理grpc请求的线程池的队列大小</td>
    <td>10</td>
  </tr>
  <tr>
    <td>proxy.grpc.unaryCall.timeout</td>
    <td>unaryCall请求的超时时间</td>
    <td>3000</td>
  </tr>
  <tr>
    <td>useZkRouter</td>
    <td>是否使用zk</td>
    <td>true</td>
  </tr>
  <tr>
    <td>zk.url</td>
    <td>zk集群地址</td>
    <td>localhost:2181,localhost:2182,localhost:2183</td>
  </tr>
  <tr>
    <td>acl.enable</td>
    <td>是否使用zookeeper acl鉴权</td>
    <td>false</td>
  </tr>
  <tr>
    <td>acl.username</td>
    <td>acl 用户名</td>
    <td>默认空</td>
  </tr>
  <tr>
    <td>acl.password</td>
    <td>acl 密码</td>
    <td>默认空</td>
  </tr>
</table>

### route_table配置
**在2.1.0版本开始支持HTTP接口配置，2.1.0之前的版本只支持GRPC配置。**     

1.GRPC
```yaml
{
  "route_table": {
    "default": {
      "default": [
        // 此处用于配置serving-proxy默认对外转发地址，
       // 切记不能配置成serving-proxy自己的ip端口，会形成回环
        {
          "ip": "127.0.0.1",
          "port": 9999
        }
      ]
    },
    //  向对方发送请求使用上面的default配置就能满足大部分需求。
    // 以下是路由中己方部分说明：
    
    //己方的serving-proxy 在收到grpc unaryCall接口的请求后，会根据请求中的目的partyId尝试匹配。
    比如请求中目的partId为10000，则会在路由表中查找是否存在10000的配置
    //此处的10000表示目的partId 为10000时的路由，匹配到10000之后，再根据请求中的角色信息role。
    比如请求中role 为serving则会继续匹配下面是否有serving的配置
    "10000": {
      // 在未找到对应role的路由地址时，会使用default的配置
      "default": [
        {
          "ip": "127.0.0.1",
          "port": 8889
        }
      ],
      "serving": [
        // 当已经匹配到role为serving，则代表请求为发给serving-server的请求，这时检查是否启用了ZK为注册中心，
        如果已启用ZK则优先从ZK中获取目标地址，未找到时使用以下地址
        
        {  // 此处配置己端对应serving服务地址列表，ip和port对应serving-server所启动的grpc服务地址
          "ip": "127.0.0.1",
          "port": 8080
        }
      ]
    }
  },
  // 此处配置当前路由表规则开启/关闭
  "permission": {
    "default_allow": true
  }
}
```
2.HTTP
```yaml
{
  "route_table": {
    "default": {
      "default": [
        {
          // 此处配置是否开启ssl证书认证
          "useSSL":false,
          "url":"http://127.0.0.1:8879/unary"
        }
      ]
    },
    "10000": {
      "default": [
        {
          "useSSL":false,
          "url":"http://127.0.0.1:8879/unary"
        }
      ],
      "serving": [
        
        {  // 此处配置己端对应serving服务地址列表，ip和port对应serving-server所启动的grpc服务地址
          "ip": "127.0.0.1",
          "port": 8080
        }
      ]
    }
  },
  // 此处配置当前路由表规则开启/关闭
  "permission": {
    "default_allow": true
  }
}



```