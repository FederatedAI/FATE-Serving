### 基础类
PipelineModelProcessor类是模型处理逻辑的实现类，serving-server在收到推送模型的请求后，会在内存中初始化一个PipelineModelProcessor实例，该实例中包含了由模型中各算法组件组成的pipeline。pipeline处理逻辑分为两个阶段：  
1) 本地模型处理 （这一步在Guest方，以及Host方都会存在）  
2) 合并远端数据 （这一步只在Guest方存在）  

BaseComponent 类是所有算法组件的基类，它实现了LocalInferenceAware接口  
```yml
xxxxxxxxxx
public interface LocalInferenceAware {
​
    public Map<String, Object> localInference(Context context, List<Map<String, Object>> input);
​
}
```
PipelineModelProcessor中的pipeline在检测到组件为LocalInferenceAware时，会调用localInference方法来进行本地预测。
另外一个重要的接口MergeInferenceAware，实现该接口的组件可以执行合并远端返回数据的逻辑。  
```yml
xxxxxxxxxx
public interface MergeInferenceAware {
​
    public Map<String, Object> mergeRemoteInference(Context context, List<Map<String, Object>> localData, Map<String, Object> remoteData);
}
```

### HeteroSecureBoost 组件
HeteroSecureBoost 为纵向联邦SecureBoost在线推理的实现过程，与离线不同的是，目前serving只支持单个host的预测。  
它主要包含3个文件"HeteroSecureBoost", "HeteroSecureBoostingTreeGuest", "HeteroSecureBoostingTreeHost"，下面对这三个文件展开说明

#### HeteroSecureBoost
HeteroSecureBoost是HeteroSecureBoostingTreeGuest和HeteroSecureBoostingTreeHost的模型基类，该基类提供了模型的初始化、层次遍历等功能。

1. HeteroSecureBoost继承自BaseComponent类，BaseComponent类为所有模型的基类，所有算法模型必须继承该类和实现相关接口，用于统一的调度。
2. 模型初始化：initModel函数，功能是对输入的Meta和Param两个序列化的模型文件进行反序列话，同时，初始化相关的类属性，初始化的内容包括：  
treeNum: 树的数量
initScore: boost的初始化得分，具体可参考FATE离线建模文档
trees: 具体的树信息列表，可参考对应的DecisionTreeModelParam
numClasses: 多少类，二分类问题为2，多分类问题则为具体分类数，回归问题为0，通过该字段可以判断具体建模任务类型
classes: 类别标签，对于分类问题，用预测的结果下表去索引真正的分类标签
treeDim: boost的每轮树的数量，对于回归和二分类等于1，对于多分类，是类别数量，每轮每个分类都有一个对应的树
learningRate: 学习率和权重放缩因子，推理时每个树得到的权重都会乘以learning_rate。
3. 功能函数说明：  
getSite: 离线的时候，每个树节点的域信息是role:partyid，如host:10000，通过该函数获取role
generateTag: 用来存储和读取每轮使用的数据，用法在下面辉介绍
gotoNextLevel: 输入当前的树、节点编号，特征值，输出树的下一层节点编号  

#### HeteroSecureBoostingTreeGuest and HeteroSecureBoostingTreeHost
HeteroSecureBoostingTreeGuest和HeteroSecureBoostingTreeHost是party guest和host对应的实现代码，其中party guest收到请求后，会执行推理流程，与此同时，需要与host一起决策每个树的预测流程，在2.0.x版本中，serving对预测流程做了专门优化，现预测流程如下:

1. HeteroSecureBoostingTreeGuest 首先执行本地推导，对于所有的树进行遍历，如果某棵树遇到host节点，则将host节点记录；如果所有树都没遇到host节点，则得到叶子节点权重，跳至步骤5。
2. HeteroSecureBoostingTreeGuest 遇到的host节点，通过通信通知Host。
3. Host 接收到推理指令后，执行完整的推理调用逻辑（数据预处理 -> 特征工程 -> HeteroSecureBoost)，完成数据处理后，Host本地提取节点路由，对于该预测的样本，对于所有自身拥有的Host节点，判断该样本的遍历方向（向左还是向右），并把所有的路由方向记录下来。
4. Host将提取出的路由方向整理发送至Guest
5. HeteroSecureBoostingTreeGuest继续推理逻辑，由于已经获得Host方发送的路由表，则在后续的推理过程中，遇到任何的Host的节点，都可以知道遍历方向。 Guest从记录的host节点继续往下推导，直至到达叶子节点。
6. Guest得到每个树的节点编号，利用节点编号索引出叶子权重，经过处理后得到预测结果，并将结果输出，推理流程完成。  

当前的HeteroSecureBoost交互次数最坏情况下也仅需要1次数据交换。
另外，HeteroSecureBoost在2.0.x版本开始支持批量预测。

HeteroSecureBoost在线推理流程图如下：
![inference_flow](..\img\inference_flow.jpg)

### HeteroLR 在线推理
HeteroLR为纵向联邦逻辑回归在线推理的实现过程。
它主要包含3个文件"HeteroLRBoost", "HeteroLRGuest", "HeteroLRHost"，下面对这三个文件展开说明
#### HeteroLR
HeteroLR是HeteroLRGuest和HeteroLRHost的模型基类，该基类提供了模型的初始化、模型评分等功能。

1. HeteroLR继承自BaseComponent类，BaseComponent类为所有模型的基类，所有算法模型必须继承该类和实现相关接口，用于统一的调度。
2. 模型初始化: initModel函数，功能是对输入的Meta和Param两个序列化的模型文件进行反序列话，同时，初始化相关的类属性，初始化的内容包括:  
    a. weight: LR模型的权值  
    b. intercept: LR模型的偏置
3. 功能函数说明:  
    a. forward: 计算score = weight * value + intercept, 若是host方，则intercept为0

#### HeteroLRGuest and HeteroLRHost
HeteroLRGuest和HeteroLRHost是party guest和host对应的实现代码，其中party guest收到请求后，会执行推理过程，与此同时，需要与host也一起进行推理，下面给出具体说明。

1. 系统针对请求的id，同时给guest和host发起推理请求
2. Host接收到推理指令后，执行forward函数的前向计算流程，并将结果返回，由系统调度给guest
3. Guest接收到推理指令后，执行forward函数的前向计算流程，并且获取host的计算结果，组合一起，并计算sigmod得到最终评分，完成完整的推理流程  
