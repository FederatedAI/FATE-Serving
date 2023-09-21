FATE-Serving在调用在线预测接口时，需要数据使用方（Guest）、数据提供方（Host）双方联合预测，Guest方对模型和特征数据进行业务处理后，Guest方接口参数中的sendToRemoteFeatureData会发往Host端，Host方则是通过自定义的Adaptor跟己方业务系统交互（eg：通过访问远程rpc接口、或者通过访问存储）来获取特征数据，并将获取的特征交给算法模块进行计算，最终得出合并后的预测结果并返回给Guest。源码中提供了两个抽象类AbstractSingleFeatureDataAdaptor、AbstractBatchFeatureDataAdaptor用于可用于继承并实现自定义的接口类。

>默认的情况使用系统自带的MockAdatptor，仅返回固定数据用于简单测试，实际生产环境中需要使用者需要自行开发并对接自己的业务系统。

 

## 自定义Adapter开发
实现自定义Adapter，只需要分别继承AbstractSingleFeatureDataAdaptor或AbstractBatchFeatureDataAdaptor并重写父类抽象方法，AbstractSingleFeatureDataAdaptor用于在线单次预测业务，AbstractBatchFeatureDataAdaptor用于在线批量预测业务。

>init方法中可直接使用environment获取serving-server.properties配置中参数
```java
public class CustomAdapter extends AbstractSingleFeatureDataAdaptor {
    
    @Override
  public void init() {
      // init() 方法中可以直接使用environment对象
        // environment.getProperty("port");
    }
  
  @Override
  public ReturnResult getData(Context context, Map<String, Object> featureIds) {
    // ...
  }
}
​
public class CustomBatchAdapter extends AbstractBatchFeatureDataAdaptor {
​
    @Override
    public void init() {
        
    }
​
    @Override
    public BatchHostFeatureAdaptorResult getFeatures(Context context, List<BatchHostFederatedParams.SingleInferenceData> featureIdList) {
        // ...
    }
```
Context为上下文信息，用于传递请求所需参数，featureIds用于传递数据提供方传递过来的特征ID（eg:手机号、设备号）
```yaml
#在host方的配置文件serving-server.properties中将其配置成自定义的类的全路径，如下所示
feature.single.adaptor=com.webank.ai.fate.serving.adaptor.dataaccess.CustomAdapter
feature.batch.adaptor=com.webank.ai.fate.serving.adaptor.dataaccess.CustomBatchAdapter
```
可以根据需要实现Adapter中的逻辑，并修改serving-server.properties中feature.single.adaptor或feature.batch.adaptor配置项为新增Adapter的全类名即可。可以参考源码中的MockAdaptor

## fate-serving-extension
为了更好的代码解耦合，代码中将自定义adapter分离到fate-serving-extension模块中。用户可在此模块中开发自定义的adapter。
>mvn clean package -pl fate-serving-extension -am

单独打包fate-serving-extension，将target/fate-serving-extension-{version}.jar拷贝到serving-server部署目录下extension中覆盖，重启服务即可生效。
>serving-server部署目录下extension``已加载到类路径

## 预设Adapter
fate-serving-extension中预设了6中Adapter的简单实现

#### MockAdapter
固定返回mock特征数据

#### MockBatchAdapter
用于批量预测，原理同上

#### BatchTestFileAdapter
用于批量预测，原理同上

#### TestFileAdapter
从host_data.csv中读取特征数据，每次调用返回值为csv中所有内容, host_data.csv需上传至Host方serving-server实例部署根目录下，
>x0:-0.320167,x1:0.58883,x2:-0.18408,x3:-0.384207,x4:2.201839,x5:1.68401,x6:1.219096,x7:1.150692,x8:1.9656,x9:1.572462,x10:-0.35685
x0:1,x1:5,x2:13,x3:58,x4:95,x5:352,x6:418,x7:833,x8:888,x9:937,x10:32776

#### HttpAdapter
在serving-server.properties文件中配置属性feature.single.adaptor和http.adapter.url，feature.single.adaptor为继承AbstractSingleFeatureDataAdaptor
接口，url为调用获取数据接口地址。http.adapter.url中标明的用户接口，返回格式请定义为 {"code": 200, "data": xxx}标准格式即可，httpAdapter中会根据接口返回状态码是否为200判断用户数据拉取接口是否执行成功。
```yaml
feature.single.adaptor=com.webank.ai.fate.serving.adaptor.dataaccess.HttpAdapter
http.adapter.url=http://127.0.0.1:9380/v1/http/adapter/getFeature
```

#### HttpBatchAdapter
用于批量预测，需将feature.single.adaptor配置改为feature.batch.adaptor