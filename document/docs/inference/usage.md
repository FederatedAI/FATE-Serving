目前有以下几种方式调用推理接口，支持[单笔](./single.md)或[批量](./batch.md)调用，具体采用哪种方式可以根据实际情况选择。 

1. 使用guest方serving-admin页面发送请求。    
>优点：使用简单，可用作测试。

![single](../img/inference-single-1.jpg)

![single](../img/inference-single-2.jpg)

2.访问serving-proxy的http接口 ，由serving-proxy转发请求至serving-server。
>优点：接入简单，可在serving-proxy前面增加nginx作为反向代理。

3.使用源码中自带的SDK访问serving-server。  
>优点：省去了中间serving-proxy作为转发节点 ，提高通信效率。并将使用fate-serving的服务注册以及发现等功能，直接调用serving-server的grpc接口。

4.自行开发并直接调用serving-server提供的grpc接口。     
>优点：目前sdk部分只提供了java版，若是其他未支持的语言，可以自行开发并调用相关接口 。部署时可以采用 [nginx前置部署](example/nginx.md)，用于反向代理grpc请求。
