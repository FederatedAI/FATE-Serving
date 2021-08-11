### FATE-SERVING 服务特性
-订阅与注册的可靠性保证  
目前订阅/取消订阅  、  注册/取消注册操作都使用了定时重试的机制来保证操作的最终成功。

-客户端服订阅务信息的缓存与持久化  
默认在serving-server实例部署目录下（2.1版本之前是在当前用户目录下）生成.fate 的文件夹，所有的持久化信息都会放入该文件夹。 使用服务治理管理的模块启动之后，首先会从本地缓存文件加载之前订阅的接口，然后再从注册中心拉取并更新本地文件。在极端情况下，如注册中心宕机，本地的持久化文件将继续服务，不会影响业务流量。

-客户端的负载均衡  
目前支持加权随机（可以通过serving-admin页面调整接口权重）

-服务端的优雅停机  
服务端会在jvm 退出时，主动取消注册在注册中心的接口，拒绝新的请求，并等待当前正在处理的请求退出。 需要注意的是，不能使用kill -9 命令退出，这样不会触发jvm退出前的动作，若进程强制退出，虽然zookeeper会判断心跳超时并将所创建的临时节点消失，但是在心跳还未超时的这段时间里业务流量还会被路由到当前已被kill的实例上来，造成风险。建议使用kill。


### 服务治理
在线预测部分主要有以下几个模块会进行rpc调用：  
•	serving-server  
•	serving-proxy  
•	serving-admin  
•	fateflow  
其中serving-server 和serving-proxy对外提供了预测接口， 承载业务流量，所以有弹性扩缩容的需求，同时要求高可用。针对以上的几点，fate-serving使用了zookeeper作为注册中心，管理各模块的服务注册以及发现。


### zookeeper中的数据结构
zookeeper中使用的数据结构如下
```yml
/FATE-SERVICES/{模块名}/{ID}/{接口名}/provider/{服务提供者信息}
```
接下来说明zookeeper各级路径的含义  
第一级路径：永久节点，固定为FATE-SERVICES  
第二级路径：永久节点，各自模块的名字，比如serving-proxy的模块名为proxy ，serving-server的模块名为serving  
第三级路径：永久节点，ID ，根据接口的不同ID的值不一样。与模型版本强相关的接口，如inference接口，ID为fate-flow推送模型时产生的serviceId ，这样可以使得不同版本模型注册路径不一样。其他接口为字符串online  
第四级路径：永久节点，固定字符串provider  
第五级路径：临时节点，详细描述注册信息 eg:  grpc://172.168.1.1:8000


### 自定义服务
如果需要自定义grpc服务，可遵循以下步骤。  
1.在FATE-SERVING/proto中配置grpc 服务相关的.proto文件  
2.在服务对应子项目的grpc.service目录下添加service方法，例如：想在serving-server新增grpc服务，则找到FATE-Serving\fate-serving-server\src\main\java\com\webank\ai\fate\serving\grpc\service目录，并要在服务方法上加入注解@RegisterService，示例如下：  
```yml
@RegisterService(serviceName = UPDATE_SERVICE)
public void updateService(CommonServiceProto.UpdateServiceRequest request, 
    StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
    ...
}

```
该注解作用是把对应服务注册进zookeeper集群，具体实现可在FATE-Serving\fate-serving-register\src\main\java\com\webank\ai\fate\register\zookeeper\ZookeeperRegistry.java
中的 register 方法中查看。

3.在对应子项目添加ServiceProvider 方法，并在方法注入@FateServiceMethod。该注解作用是生成对应的grpc服务bean，具体实现在fate-serving-server/src/main/java/com/webank/ai/fate/serving/FateServiceRegister.java中
```yml
public void onApplicationEvent(ApplicationReadyEvent applicationEvent) {
    String[] beans = applicationContext.getBeanNamesForType(AbstractServiceAdaptor.class);
    for (String beanName : beans) {
        AbstractServiceAdaptor serviceAdaptor = applicationContext.getBean(beanName, AbstractServiceAdaptor.class);
        FateService proxyService = serviceAdaptor.getClass().getAnnotation(FateService.class);
        Method[] methods = serviceAdaptor.getClass().getMethods();
        for (Method method : methods) {
            FateServiceMethod fateServiceMethod = method.getAnnotation(FateServiceMethod.class);
            if (fateServiceMethod != null) {
                String[] names = fateServiceMethod.name();
                for (String name : names) {
                    serviceAdaptor.getMethodMap().put(name, method);
                }
            }
        }

```

