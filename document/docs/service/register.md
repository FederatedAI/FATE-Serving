
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
第五级路径：临时节点，详细描述注册信息 eg:  grpc://192.168.1.1:8000

### ACL鉴权
FATE-Serving支持zookeeper acl鉴权，在使用zk注册中心时，启用zookeeper acl鉴权的服务所注册的接口节点，未配置acl鉴权的服务不能访问。启用添加如下配置： 
```properties
acl.enable=true     
acl.username=${username}    
acl.password=${password}
 ``` 
>注意：使用同一注册中心的服务，启用ACL鉴权，需要将所有服务都启用，否则会影响接口注册及访问。

 