
### 自定义grpc服务
如果需要自定义grpc服务，可遵循以下步骤。  
1.在FATE-SERVING/proto中配置grpc 服务相关的.proto文件  
2.在服务对应子项目的grpc.service目录下添加service方法，例如：想在serving-server新增grpc服务，则找到FATE-Serving\fate-serving-server\src\main\java\com\webank\ai\fate\serving\grpc\service目录，并要在服务方法上加入注解@RegisterService，示例如下：  
```java
@RegisterService(serviceName = UPDATE_SERVICE)
public void updateService(CommonServiceProto.UpdateServiceRequest request, 
    StreamObserver<CommonServiceProto.CommonResponse> responseObserver) {
    ...
}

```
该注解作用是把对应服务注册进zookeeper集群，具体实现可在FATE-Serving\fate-serving-register\src\main\java\com\webank\ai\fate\register\zookeeper\ZookeeperRegistry.java
中的 register 方法中查看。

3.在对应子项目添加ServiceProvider 方法，并在方法注入@FateServiceMethod。该注解作用是生成对应的grpc服务bean，具体实现在fate-serving-server/src/main/java/com/webank/ai/fate/serving/FateServiceRegister.java中
```java
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

