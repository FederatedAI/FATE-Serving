### 什么是Fate-Serving
fate-serving是FATE的在线部分，在使用FATE进行联邦建模完成之后，可以使用fate-serving进行包括单笔预测、多笔预测以及多host预测在内的在线联合预测。

### 组件简介
•	serving-server  
serving-server用于实时处理在线预测请求, 理论上serving-server需要从fate-flow加载模型成功之后才能对外提供服务。 在FATE中建好模型之后，通过fate-flow的推送模型脚本可以将模型推送至serving-server。 推送成功之后，serving-server会将该模型相关的预测接口注册进zookeeper， 外部系统可以通过服务发现获取接口地址并调用。 同时本地文件持久化该模型,以便在serving-server实例在集群中某些组件不可用的情况下， 仍然能够从本地文件中恢复模型。

•	serving-proxy  
serving-proxy 是serving-server的代理，对外提供了grpc接口以及http的接口， 主要用于联邦预测请求的路由转发鉴权。在离线的联邦建模时， 每一个参与方都会分配一个唯一的partId。serving-proxy维护了一个各参与方partId的路由表， 并通过路由表中的信息来转发请求。

•	serving-admin  
serving-admin 提供在线集群的可视化操作界面， 可以查看管理集群中各节点的配置以及状态、查看模型列表、流量的调控、并能提供一定的监控的功能。

•	zookeeper  
zookeeper 用户各组件的信息同步协调以及服务注册于发现

### 架构简介  
各组件的具体功能以及整个项目的功能如下图所示：  
![架构1](img\Structure1.jpg)  
![架构2](img\Structure2.jpg) 

### 源码结构
•	fate-serving-register：主要为服务治理相关的逻辑  
•	fate-serving-admin：serving-admin模块，提供集群服务的可视化管理/监控  
•	fate-serving-admin-ui：serving-admin的前端代码，使用Vue.js开发  
•	fate-serving-common：fate-serving的公共组件模块  
•	fate-serving-core：fate-serving的核心代码包  
•	fate-serving-extension：主要用于实现一些扩展逻辑，如host端的自定义Adaptor  
•	fate-serving-federatedml：算法组件的实现逻辑  
•	fate-serving-proxy：serving-proxy模块，主要提供路由转发及鉴权，对外开放http/grpc接口  
•	fate-serving-sdk：提供sdk代码，用于代码接入serving-server服务  
•	fate-serving-server：serving-server模块，主要处理在线联邦预测业务和模型算法逻辑  
•	fate-serving-cli：golang实现，命令行工具的代码，提供serving-server的查询和预测  
 
