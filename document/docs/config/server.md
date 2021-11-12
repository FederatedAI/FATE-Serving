### serving-server.properties配置   
以下为conf/serving-server.properties文件配置详解。源码中的配置文件没有罗列出所有配置，只保留了必需的配置，其他配置都采用了默认值。如果需要可以根据以下表格来在配置文件中新增条目

<table>
  <tr>
    <td>配置项</td>
    <td>配置项含义</td>
    <td>默认值</td>
  </tr>
  <tr>
    <td>port</td>
    <td>服务监听端口</td>
    <td>8000</td>
  </tr>
  <tr>
    <td>remoteModelInferenceResultCacheSwitch</td>
    <td>预测结果的缓存开关，false代表不使用缓存，true代表使用缓存，该配置跟cache.type 配合使用</td>
    <td>false</td>
  </tr>
  <tr>
    <td>cache.type</td>
    <td>缓存类型，可选local/redis,其中local为进程中维持的LRU内存，不建议在生产上使用local</td>
    <td>local</td>
  </tr>
  <tr>
    <td>local.cache.expire</td>
    <td>内置缓存过期时间，单位：秒，该配置在cache.type=local时生效</td>
    <td>30</td>
  </tr>
  <tr>
    <td>local.cache.interval</td>
    <td>内置缓存过期处理间隔，单位：秒 ，该配置在cache.type=local时生效</td>
    <td>3</td>
  </tr>
  <tr>
    <td>local.cache.maxsize</td>
    <td>内置缓存最大存储数量 ，该配置在cache.type=local时生效</td>
    <td>10000</td>
  </tr>
  <tr>
    <td>redis.ip</td>
    <td>redis ip地址，该配置在cache.type=redis时生效</td>
    <td>127.0.0.1</td>
  </tr>
  <tr>
    <td>redis.port</td>
    <td>redis端口 ，该配置在cache.type=redis时生效</td>
    <td>3306</td>
  </tr>
  <tr>
    <td>redis.cluster.nodes</td>
    <td>redis集群节点，配置则开启集群模式 ，该配置在cache.type=redis时生效</td>
    <td>空</td>
  </tr>
  <tr>
    <td>redis.password</td>
    <td>redis密码 ，该配置在cache.type=redis时生效</td>
    <td>空</td>
  </tr>
  <tr>
    <td>redis.expire</td>
    <td>redis过期时间 ，该配置在cache.type=redis时生效</td>
    <td>3000</td>
  </tr>
  <tr>
    <td>redis.timeout</td>
    <td>redis链接超时时间 ，该配置在cache.type=redis时生效</td>
    <td>2000</td>
  </tr>
  <tr>
    <td>redis.maxIdle</td>
    <td>redis连接池最大空闲链接 ，该配置在cache.type=redis时生效</td>
    <td>2</td>
  </tr>
  <tr>
    <td>redis.maxTotal</td>
    <td>redis连接池最大数量 ，该配置在cache.type=redis时生效</td>
    <td>20</td>
  </tr>
  <tr>
    <td>serving.core.pool.size</td>
    <td>grpc服务线程池核心线程数</td>
    <td>cpu核心数</td>
  </tr>
  <tr>
    <td>serving.max.pool.size</td>
    <td>grpc服务线程池最大线程数</td>
    <td>cpu核心数 * 2</td>
  </tr>
  <tr>
    <td>serving.pool.alive.time</td>
    <td>grpc服务线程池超时时间</td>
    <td>1000</td>
  </tr>
  <tr>
    <td>serving.pool.queue.size</td>
    <td>grpc服务线程池队列数量</td>
    <td>100</td>
  </tr>
  <tr>
    <td>single.inference.rpc.timeout</td>
    <td>单次预测超时时间</td>
    <td>3000</td>
  </tr>
  <tr>
    <td>batch.inference.max</td>
    <td>单次批量预测数量</td>
    <td>300</td>
  </tr>
  <tr>
    <td>batch.inference.rpc.timeout</td>
    <td>批量预测超时时间</td>
    <td>3000</td>
  </tr>
  <tr>
    <td>batch.split.size</td>
    <td>批量任务拆分数量，在批量预测时会根据该参数大小将批量任务拆分成多个子任务并行计算，比如如果该配置为100，那300条预测的批量任务会拆分成3个100条子任务并行计算</td>
    <td>100</td>
  </tr>
  <tr>
    <td>lr.use.parallel</td>
    <td>lr模型是否启用并行计算</td>
    <td>false</td>
  </tr>
  <tr>
    <td>lr.split.size</td>
    <td>LR多任务拆分数量 ，该配置在lr.use.parallel=true时生效</td>
    <td>500</td>
  </tr>
  <tr>
    <td>feature.batch.adaptor</td>
    <td>批量特征处理器，Host方需要配置，用于批量获取Host方特征信息，用户可根据业务情况，实现AbstractBatchFeatureDataAdaptor接口</td>
    <td>com.webank.ai.fate.serving.adaptor.dataaccess.MockBatchAdapter</td>
  </tr>
  <tr>
    <td>feature.single.adaptor</td>
    <td>单次特征处理器，Host方需要配置，用于获取Host方特征信息，用户可根据业务情况，实现AbstractSingleFeatureDataAdaptor接口</td>
    <td>com.webank.ai.fate.serving.adaptor.dataaccess.MockAdapter</td>
  </tr>
  <tr>
    <td>model.cache.path</td>
    <td>模型缓存地址，对于内存中存在的模型，serving-server会持久化到本地以便在重启时恢复</td>
    <td>服务部署目录</td>
  </tr>
  <tr>
    <td>model.transfer.url</td>
    <td>fateflow模型拉取接口地址，优先使用注册中心中的fateflow地址，若注册中心中没有找到fateflow地址，则会使用该配置地址</td>
    <td>http://127.0.0.1:9380/v1/model/transfer</td>
  </tr>
  <tr>
    <td>proxy</td>
    <td>proxy服务的地址，建议通过启用zookeeper自动获取地址，当不启用zk时需要直接在此处配置</td>
    <td>127.0.0.1:8879</td>
  </tr>
  <tr>
    <td>zk.url</td>
    <td>zookeeper集群地址</td>
    <td>localhost:2181,localhost:2182,localhost:2183</td>
  </tr>
  <tr>
    <td>useRegister</td>
    <td>使用注册中心，开启后会将serving-server中的接口注册至zookeeper</td>
    <td>true</td>
  </tr>
  <tr>
    <td>useZkRouter</td>
    <td>使用zk路由，开启后rpc调用时会使用注册中心中的地址进行路由</td>
    <td>true</td>
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

