FATE-Serving的在线批量推理功能，在单次推理基础上提升吞吐量，提供了支持批量推理接口，入口则是Guest方提供的batchInference接口。

### http请求
#### http请求地址
请求路径：http://{ip}:{port}/federation/v1/batchInference    
{ip}:{port} 为Guest方serving-proxy的地址，依据实际部署架构而定

#### http请求类型
POST    
content-application/json

#### 请求内容
请求体（Request Body）
<table>
  <tr>
    <td>参数名</td>
    <td>是否必填</td>
    <td>类型</td>
    <td>描述</td>
  </tr>
  <tr>
    <td>head</td>
    <td>是</td>
    <td>json object</td>
    <td>系统所需参数</td>
  </tr>
  <tr>
    <td>body</td>
    <td>否</td>
    <td>json object</td>
    <td>模型预测需要用到的数据，一般是包括ID，比如手机号、设备号等</td>
  </tr>
</table>

#### 请求示例
```json
{
    "head": {
        "serviceId": "test-lr"
    },
    "body": {
        "batchDataList": [
            {
                "index": 0,
                "featureData": {
                    "x0": -0.161357,
                    "x1": 0.822813,
                    "x2": -0.031609,
                    "x3": -0.248363,
                    "x4": 1.662757,
                    "x5": 1.81831
                },
                "sendToRemoteFeatureData": {
                    "device_id": "8",
                    "phone_num": 644
                }
            },
            {
                "index": 1,
                "featureData": {
                    "x0": -0.161357,
                    "x1": 0.822813,
                    "x2": -0.031609,
                    "x3": -0.248363,
                    "x4": 1.662757,
                    "x5": 1.81831
                },
                "sendToRemoteFeatureData": {
                    "device_id": "8",
                    "phone_num": 644
                }
            }
        ]
    }
}
```
**Note**: 
>head中填入系统参数，body中batchDataList为参数集合，featureData中为模型所需特征数据，不会传递给Host方，只有sendToRemoteFeatureData中的才会传递给Host方，一般sendToRemoteFeatureData需要包含host方用于匹配样本的id，例如设备号或者手机号

#### 响应内容
<table>
  <tr>
    <td>字段名</td>
    <td>类型</td>
    <td>描述</td>
  </tr>
  <tr>
    <td>retcode</td>
    <td>int</td>
    <td>错误码，0 表示请求成功</td>
  </tr>
  <tr>
    <td>retmsg</td>
    <td>string</td>
    <td>错误提示信息</td>
  </tr>
  <tr>
    <td>flag</td>
    <td>int</td>
    <td>保留字段</td>
  </tr>
  <tr>
    <td>data.modelId</td>
    <td>string</td>
    <td>模型ID</td>
  </tr>
  <tr>
    <td>data.modelVersion</td>
    <td>string</td>
    <td>模型版本</td>
  </tr>
  <tr>
    <td>data.timestamp</td>
    <td>long</td>
    <td>模型发布时间戳</td>
  </tr>
  <tr>
    <td>batchDataList</td>
    <td>object array</td>
    <td>批量预测结果合集</td>
  </tr>
  <tr>
    <td>singleInferenceResultMap</td>
    <td>map</td>
    <td>批量预测结果下标映射Map</td>
  </tr>
</table>

```json
{
    "retcode": 0,
    "retmsg": "",
    "data": {
        "modelId": "guest#9999#arbiter-10000#guest-9999#host-10000#model",
        "modelVersion": "2020072814115256400000",
        "timestamp": 1595916929427
    },
    "flag": 0,
    "batchDataList": [
        {
            "index": 0,
            "retcode": 0,
            "retmsg": "",
            "data": {
                "score": 0.5386207970765767
            }
        },
        {
            "index": 1,
            "retcode": 0,
            "retmsg": "",
            "data": {
                "score": 0.5386207970765767
            }
        }
    ],
    "singleInferenceResultMap": {
        "0": {
            "index": 0,
            "retcode": 0,
            "retmsg": "",
            "data": {
                "score": 0.5386207970765767
            }
        },
        "1": {
            "index": 1,
            "retcode": 0,
            "retmsg": "",
            "data": {
                "score": 0.5386207970765767
            }
        }
    }
}
```

### java版SDK
目前提供了java版sdk，可以通过使用java版SDK来对接serving-server，sdk提供服务发现以及路由功能。
需要使用在目标工程pom文件中加入相关依赖

#### 操作步骤
```yml
xxxxxxxxxx
1.cd  fate-serving源码根目录，执行 mvn clean  install -pl com.webank.ai.fate:fate-serving-core,com.webank.ai.fate:fate-serving-sdk,com.webank.ai.fate:fate-serving-register,com.webank.ai.fate:fate-serving 
2.在目标工程文件中加入依赖         
    <dependency>
        <groupId>com.webank.ai.fate</groupId>
        <artifactId>fate-serving-sdk</artifactId>
        <version>2.0.0</version>
    </dependency>
```

#### 示例
```java
package com.webank.ai.fate.serving.sdk.client;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.ai.fate.serving.core.bean.BatchInferenceRequest;
import com.webank.ai.fate.serving.core.bean.BatchInferenceResult;
import com.webank.ai.fate.serving.core.bean.InferenceRequest;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;
​
/**
 * 该类主要演示如何使用带有注册中心的客户端
 */
public class RegisterClientExample {
​
	/**
		* 只能实例化一次 ，全局维护一个单例。  若是有多个ip ，则使用逗号分隔. 在调用前先检查zk是否正常，ip端口是否填写正确
		* 若是连接不上，客户端会每隔5秒一次重连
		*/
	static RegistedClient client  =ClientBuilder.getClientUseRegister("localhost:2181");
	​
	​
	/**
		* 构建单笔预测请求
		* @return
		*/
	static InferenceRequest buildInferenceRequest(){
		InferenceRequest  inferenceRequest = new  InferenceRequest();
		inferenceRequest.setServiceId("lr-test");
		Map<String,Object> featureData = Maps.newHashMap();
		featureData.put("x0", 0.100016);
		featureData.put("x1", 1.210);
		featureData.put("x2", 2.321);
		featureData.put("x3", 3.432);
		featureData.put("x4", 4.543);
		featureData.put("x5", 5.654);
		featureData.put("x6", 5.654);
		featureData.put("x7", 0.102345);
		inferenceRequest.setFeatureData(featureData);
		Map<String,Object>  sendToRemote = Maps.newHashMap();
		sendToRemote.put("id","123");
	/**
		* sendToRemote 数据会发送到host ，需要谨慎检查是否是敏感数据
		*/
		inferenceRequest.setSendToRemoteFeatureData(sendToRemote);
		return  inferenceRequest;
	}
​
	/**
		* 构建批量预测请求
		* @return
		*/
	static BatchInferenceRequest buildBatchInferenceRequest(){
		BatchInferenceRequest  batchInferenceRequest = new  BatchInferenceRequest();
		batchInferenceRequest.setServiceId("lr-test");
		List<BatchInferenceRequest.SingleInferenceData>  singleInferenceDataList = Lists.newArrayList();
		for (int i = 0; i < 10; i++) {
			BatchInferenceRequest.SingleInferenceData singleInferenceData = new BatchInferenceRequest.SingleInferenceData();
			singleInferenceData.getFeatureData().put("x0", 0.100016);
			singleInferenceData.getFeatureData().put("x1", 1.210);
			singleInferenceData.getFeatureData().put("x2", 2.321);
			singleInferenceData.getFeatureData().put("x3", 3.432);
			singleInferenceData.getFeatureData().put("x4", 4.543);
			singleInferenceData.getFeatureData().put("x5", 5.654);
			singleInferenceData.getFeatureData().put("x6", 5.654);
			singleInferenceData.getFeatureData().put("x7", 0.102345);
			/**
				* sendToRemote 数据会发送到host ，需要谨慎检查是否是敏感数据
				*/
			singleInferenceData.getSendToRemoteFeatureData().put("device_id","helloworld");
			/**
				* 这里的序号从0开始 ，序号很重要，不可以重复
				*/
			singleInferenceData.setIndex(i);
			singleInferenceDataList.add(singleInferenceData);
​
			}
			batchInferenceRequest.setBatchDataList(singleInferenceDataList);
			batchInferenceRequest.setServiceId("lr-test");
			return  batchInferenceRequest;
	}
​
	public  static  void main(String[] args) throws IOException, InterruptedException {
			InferenceRequest inferenceRequest = buildInferenceRequest();
			BatchInferenceRequest batchInferenceRequest = buildBatchInferenceRequest();
			try {
				/**
					* 测试单笔预测
					*/
				ReturnResult returnResult1 =  client.singleInference(inferenceRequest);
				System.err.println(returnResult1);
				/**
					*  使用注册中心的同时也可以绕过注册中心，使用ip端口的方式进行rpc调用
					*/
				ReturnResult returnResult2 = client.singleInference("localhost",8000,inferenceRequest);
				System.err.println(returnResult2);
				/**
					* 测试批量预测
					*/
				BatchInferenceResult BatchInferenceResult1 = client.batchInference(batchInferenceRequest);
				System.err.println(BatchInferenceResult1);
				/**
					* 指定ip端口批量预测
					*/
				BatchInferenceResult BatchInferenceResult2 = client.batchInference("localhost",8000,batchInferenceRequest);
				System.err.println(BatchInferenceResult2);
			} catch (Exception e) {
					e.printStackTrace();
			}
			System.err.println("over");
  }
}

```