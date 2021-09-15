### 配置
目前fate-serving 使用log4j2作为日志组件，使用log4j2.xml来作为配置文件。
```xml
<Configuration status="ERROR" monitorInterval="60">
    <Properties>
        <Property name="logdir">logs</Property>
        <Property name="project">fate</Property>
        <Property name="module">serving-server</Property>
    </Properties>
    <Appenders>
        <Console name="console" target="SYSTEM_OUT">
            <PatternLayout charset="UTF-8"
                           pattern="%d{yyyy-MM-dd HH:mm:ss,SSS} [%-5p] %c{1.}(%F:%L) - %m%n"/>
        </Console>
        <RollingFile name="info" fileName="${logdir}/${project}-${module}.log"
                     filePattern="${logdir}/%d{yyyy-MM-dd}/${project}-${module}.log.%d{yyyy-MM-dd}">
            <PatternLayout charset="UTF-8" pattern="%d{yyyy-MM-dd HH:mm:ss,SSS} [%-5p] %c{1.}(%F:%L) - %m%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
            </Policies>
            <DefaultRolloverStrategy max="24"/>
        </RollingFile>
​
        <RollingFile name="flow" fileName="${logdir}/flow.log"
                     filePattern="${logdir}/%d{yyyy-MM-dd}/flow.log.%d{yyyy-MM-dd}.log">
            <PatternLayout charset="UTF-8" pattern="%d{yyyy-MM-dd HH:mm:ss,SSS}|%m%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
            </Policies>
            <DefaultRolloverStrategy max="24"/>
        </RollingFile>
​
        <RollingFile name="error" fileName="${logdir}/${project}-${module}-error.log"
                     filePattern="${logdir}/${project}-${module}-error.log.%d{yyyy-MM-dd}.log">
            <PatternLayout charset="UTF-8" pattern="%d{yyyy-MM-dd HH:mm:ss,SSS} [%-5p] %c{1.}(%F:%L) - %m%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
            </Policies>
            <DefaultRolloverStrategy max="24"/>
        </RollingFile>
​
​
        <RollingFile name="debug" fileName="${logdir}/${project}-${module}-debug.log"
                     filePattern="${logdir}/${project}-${module}-debug.log.%d{yyyy-MM-dd}.log">
            <PatternLayout charset="UTF-8" pattern="%d{yyyy-MM-dd HH:mm:ss,SSS} [%-5p] %c{1.}(%F:%L) - %m%n"/>
            <Policies>
                <TimeBasedTriggeringPolicy/>
            </Policies>
            <DefaultRolloverStrategy max="24"/>
        </RollingFile>
​
    </Appenders>
​
    <Loggers>
        <logger name="org.apache.zookeeper" level="WARN"></logger>
​
        <AsyncLogger name="flow" level="info" includeLocation="true" additivity="true">
            <AppenderRef ref="flow"/>
        </AsyncLogger>
​
<!--
    <AsyncLogger name="debug" level="debug" includeLocation="true" additivity="false">
        <AppenderRef ref="debug"/>
    </AsyncLogger>
-->
        <AsyncRoot level="info" includeLocation="true">
            <AppenderRef ref="console" level="info"/>
            <AppenderRef ref="info" level="info"/>
            <AppenderRef ref="error" level="error"/>
        </AsyncRoot>
    </Loggers>
</Configuration>
```
### 日志路径
默认日志路径会打在启动目录的logs文件夹中  
日志文件有四个：  
•	fate-${module}.log  
该日志为INFO日志，用于记录INFO级别日志,同时ERROR级别日志也会在其中体现

•	fate-${module}-error.log  
该日志为错误日志，用于记录error级别日志

•	fate-${module}-debug.log  
该日志为调试日志，用于记录debug级别日志，该日志需要放开注释手动开启

•	flow.log   
该日志为访问日志，用于记录每一笔请求的到达时间、耗时、返回码、请求参数等

### serving-server的日志格式
```yml
2020-08-10 12:47:49,708|6578868d962047c996c0505821cae830|2113|6|5|singleInference|172.16.153.105:8879||
```
时间|CaseId|返回码|总耗时|下游通信耗时|服务名称|路由信息|请求参数|返回值

### serving-proxy的日志格式
```yml
2020-08-10 14:08:39,430|127.0.0.1|1597039719427|9999|10000|0|3|3|unaryCall|172.16.153.136:8869||
```
时间|目标地址|CaseId|GuestAppId|HostAppId|返回码|总耗时|下游通信耗时|服务名称|路由信息|请求参数|返回值

### 可选打印内容
工程的配置文件中现提供两个可选参数，启动即可在flow日志中打印请求的参数和返回值，可以根据自己需要修改参数并重启系统
```properties
print.input.data=true   // flow日志打印参数
print.output.data=true  // flow日志打印返回值
```