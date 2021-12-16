### 源码打包  
用户可以选择从源码打包部署，也可以选择下载已编译好的版本。从源码部署可根据需求实现自定义Adapter。
源码打包步骤具体如下：
<div class="termy">

```console
$ git clone https://github.com/FederatedAI/FATE-Serving.git

$ cd FATE-Serving

$ mvn clean package

```
</div>
随后拷贝，比如 serving-server/target/fate-serving-server-{version}-release.zip 到想要部署的路径下，并解压。（version为当前版本号）

用户也可选择下载已编译好的版本，链接如下:   
•	[fate-serving-server-2.1.2-release.zip](https://webank-ai-1251170195.cos.ap-guangzhou.myqcloud.com/fate-serving-server-2.1.2-release.zip)  
•	[fate-serving-proxy-2.1.2-release.zip](https://webank-ai-1251170195.cos.ap-guangzhou.myqcloud.com/fate-serving-proxy-2.1.2-release.zip)    
•	[fate-serving-admin-2.1.2-release.zip](https://webank-ai-1251170195.cos.ap-guangzhou.myqcloud.com/fate-serving-admin-2.1.2-release.zip)  


### zookeeper部署

FATE-serving 使用zookeeper管理已注册的服务以及协调各组件升级之后的同步。所以在部署其余组件之前，需要确保zookeeper已成功部署并运行。 
zookeeper官方地址：https://zookeeper.apache.org/  
如不想使用zookeeper，需要在下文提到的serving-server.properties中手动添加:   
```yaml
useRegister=false
useZkRouter=false
```

### serving-server部署

1.根据需要修改或者不修改部署目录下conf/serving-server.properties文件，具体配置项见配置详解[server.md](../config/server.md),
需要检查配置文件中以下几点：  
>zk.url 是否配置正确   
model.transfer.url是否配置正确，需要配置真实ip地址  

2.sh service.sh restart (或 ./service.sh restart) 启动应用（windows 脚本暂时不支持，如有需要可自行编写)  
有可能出现的问题：
>JDK问题，可以尝试执行 java -version 查看java命令是否能正常执行 ；
>若为路径问题，可service.sh中指定jdk，例如：
```yaml
xxxxxxxxxx
export JAVA_HOME=/data/projects/fate/common/jdk/jdk-8u192
export PATH=$PATH:$JAVA_HOME/bin
```

3.检查日志与端口看启动是否正常  

安装目录下logs文件夹，查看fate-serving-server.log 和 fate-serving-server-error.log。  
可以结合 ps 命令以及 netstat 命令查看进程以及端口状态  

### serving-proxy部署  
1.根据需求修改部署目录下 conf/application.properties文件，具体配置项解释见配置文件详解[proxy.md](../config/proxy.md)中application.properties配置。   
2.配置route_table.json ，具体配置项解释见文件详解[proxy.md](../config/proxy.md)中route_table配置。  
>对route_table.json的修改是定时刷新生效，可以不需要重启serving-proxy；配置本身为json格式，修改时需要注意是否满足json格式。
  
3.sh service.sh restart (或 ./service.sh restart) 启动应用（windows 脚本暂时不支持，如有需要可自行编写）  
有可能出现的问题：
 >jdk 没有安装成功，可以尝试执行 java -version 查看java命令是否能正常执行，可在bin/service.sh中指定jdk    
```yaml
xxxxxxxxxx
export JAVA_HOME=/data/projects/fate/common/jdk/jdk-8u192
export PATH=$PATH:$JAVA_HOME/bin
```
4.检查日志与端口看启动是否正常  

### serving-admin部署  
serving-admin提供了集群的可视化操作界面，可以展示集群中各实例的配置、状态、模型、流量等信息，并可以执行模型的卸载、服务接口的权重调整等操作。
>建议安装serving-admin，通过使用serving-admin可以更方便地查看并操作模型等信息，能更方便地监控集群。

1.根据需求修改部署目录下 conf/application.properties文件，具体配置项解释见文件详解[admin.md](../config/admin.md)  
>serving-admin依赖zookeeper注册中心，必须配置fate-serving其他服务所使用的zk.url地址. 
  
2.sh service.sh restart (或 ./service.sh restart) 启动应用（windows 脚本暂时不支持，如有需要可自行编写）  
有可能出现的问题：
 >jdk 没有安装成功，可以尝试执行 java -version 查看java命令是否能正常执行，可在bin/service.sh中指定jdk    
```yaml
xxxxxxxxxx
export JAVA_HOME=/data/projects/fate/common/jdk/jdk-8u192
export PATH=$PATH:$JAVA_HOME/bin
```
3.通过浏览器访问admin页面，默认访问地址: [http://127.0.0.1:8350](http://127.0.0.1:8350) 
