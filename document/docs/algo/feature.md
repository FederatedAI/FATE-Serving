目前FATE-serving提供了以下特征工程组件，下面将会一一介绍。
### 纵向特征分箱（Hetero Feature Binning）
该模块利用训练得到的模型，根据训练时输入的不同参数，将数据转化为数据所在分箱的index。
#### 文件结构
该模块由三个文件组成，分别是"HeteroFeatureBinning"， "HeteroFeatureBinningGuest"，"HeteroFeatureBinningHost"。 

其中HeteroFeatureBinning是HeteroFeatureBinningGuest和HeteroFeatureBinningHost的基类。而HeteroFeatureBinning继承了BaseComponent。BaseComponent是所有模型组件的基类。
#### 参数和方法说明
1. initModel： 将模型的参数和结果，也就是Meta和param文件，反序列化从而对Serving模型初始化。其中从离线模型中继承的参数有：  
•	need_run：是否需要执行，如果为否，这个组件在后续预测时将被跳过  
•	transformCols： 需要对哪些列做转化  
•	featureBinningResult： 特征分箱后的结果。其中，包含每个特征的，分箱点，woe等  
2. handlePredict： 进行转化功能，将数据和模型结果中的splitPoint比较，确定属于哪个分箱后，用分箱的index代替原值。

### 纵向特征选择（Hetero Feature Selection）
根据训练所得的模型，直接选取在模型中保留的特征，其余特征被过滤掉。

#### 文件结构
该模块由三个文件组成，分别是"FeatureSelection"， "HeteroFeatureSelectionGuest"，"HeteroFeatureSelectionHost"。

其中FeatureSelection是HeteroFeatureSelectionGuest和HeteroFeatureSelectionHost的基类。而FeatureSelection继承了BaseComponent。BaseComponent是所有模型组件的基类。

#### 参数和方法说明
1. initModel： 将模型的参数和结果，也就是Meta和param文件，反序列化从而对Serving模型初始化。其中从离线模型中继承的参数有：  
•	need_run：是否需要执行，如果为否，这个组件在后续预测时将被跳过  
•	finalLeftCols： 经过特征选择后，需要保留的列名。
2. handlePredict： 进行转化功能，将输入数据中，属于最终需要保留的变量留下，其余变量被过滤掉。
