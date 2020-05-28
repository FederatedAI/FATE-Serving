## 测试相关及注意事项
### fate-serving
- 模型发布：
    1. 新增文件获取模型，需要提供参数`loadType`和`filePath`
    2. `loadType`默认为`FATEFLOW`，从FATE_FLOW中获取模型信息
- 模型回放机制：
    1. 重构模型回放机制
        
    2. 兼容旧版本模型缓存，旧版本模型缓存会转换成新版模型缓存，仅转换一次
- 流量控制：
    1. serving-server/serving-proxy会根据conf/FlowRule.json控制流量
    2. conf/FlowRule.json为可选配置，默认不限制
    3. FlowRule.json中`source`对应`@FateService`注解的`name`值
- 存储层抽象：
    1. 根据参数`cache.type`实现不同缓存，目前支持local、redis
    2. local缓存为LRU缓存
- 算法模块：
    1. 各算法组件改造，原有流程拆分，分别实现`localInference`和`mergeRemoteInference`，所有算法组件均需进行正确性测试
    2. `pipelineTask`改造，现重构为`PipelineModelProcessor`，支持单次/批量预测，流程：`localInference` -> `remoteInference` -> `mergeResult`，原有流程不能返回正确的数据，现改为根据`componentName`存储各组件返回值`Returnable`接口
- 全局异常处理：
    1. 全局异常处理逻辑统一
    2. 错误码规整  
        - SUCCESS = "0";
        - PARAM_ERROR = "100";
        - GUEST_PARAM_ERROR = "100";
        - HOST_PARAM_ERROR = "100";
        - GUEST_FEATURE_ERROR = "101";
        - HOST_FEATURE_ERROR = "101";
        - GUEST_LOAD_MODEL_ERROR = "102";
        - HOST_LOAD_MODEL_ERROR = "102";
        - GUEST_BIND_MODEL_ERROR = "103";
        - HOST_BIND_MODEL_ERROR = "103";
        - MODEL_NULL = "104";
        - HOST_MODEL_NULL = "104";
        - GUEST_ROUTER_ERROR = "105";
        - HOST_UNSUPPORTED_COMMAND_ERROR = "106";
        - GUEST_MERGE_ERROR = "107";
        - HOST_FEATURE_NOT_EXIST = "108";
        - FEATURE_DATA_ADAPTOR_ERROR = "109";
        - SYSTEM_ERROR = "500";
        - NET_ERROR = "501";
        - OVER_LOAD_ERROR = "502";
        - SHUTDOWN_ERROR = "503";
        - INVALID_ROLE_ERROR = "504";
        - SERVICE_NOT_FOUND = "505";
- 单笔预测：
    1. 参数校验 
    2. 预测结果缓存，根据`remoteModelInferenceResultCacheSwitch`参数开启
    3. 特征获取插件，需实现`SingleFeatureDataAdaptor`接口，通过配置`feature.single.adaptor`的类路径启用
- 批量预测：
    1. 参数校验
    2. 预测结果缓存，根据`remoteModelInferenceResultCacheSwitch`参数开启
    3. 特征获取插件，需实现`BatchFeatureDataAdaptor`接口，通过配置`feature.batch.adaptor`的类路径启用
    4. 异常情况性能测试，拉取host挂了，或者adaptor出异常，可能产生大量错误日志，需要测试验证性能
    5. 批量最大数量受参数`batch.inference.max`限制，默认500
    6. 批量转单笔预测，需要将`feature.batch.adaptor`配置为`com.webank.ai.fate.serving.adapter.dataaccess.ParallelBatchToSingleFeatureAdaptor`
- 异步事件处理机制：
    1. 触发错误立即上报
    2. 缓存预测结果
    3. 可以同时将事件发布到多个处理方
- 路由获取插件：
    1. 开启zk，从注册中心获取接口信息
    2. 关闭zk，从`conf/serving-server.properties`参数配置`proxy`获取接口地址

### fate-serving-admin，页面重构中...
- 模型管理：
    1. 模型卸载/解绑后，应卸载对应注册接口，接口无法使用，并且重启后模型回放不会重现
    2. 解绑功能只在guest方有效，host方模型不能解绑
    3. 根据关键字查询绑定模型
- 集群管理：
    1. 必须开启ZK注册
    2. 服务启动，会注册对应节点，服务停止，节点消失
    3. 各节点提供服务参数配置，调用量监控，serving-server会显示模型相关信息
    4. 调用量监控为每秒统计数据
- 接口管理：
    1. 显示集群中注册的所有接口
    2. 可对接口的`routerMode`/`weight`/`version`进行修改，分别配置不同路由模式和权重
