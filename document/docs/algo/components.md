### One-Hot组件
利用训练得到的模型，将预测数据转成one-hot模式。请注意，该组件目前只支持整形数的输入，如果原始数据不是整数，可以配合Feature-binning组件使用。

该组件的文件为"OneHotEncoder"。

#### 参数和方法说明
1. initModel： 将模型的参数和结果，也就是Meta和param文件，反序列化从而对Serving模型初始化。其中从离线模型中继承的参数有：  
•	need_run：是否需要执行，如果为否，这个组件在后续预测时将被跳过  
•	cols： 需要做转化的列名  
•	colsMapMap： 每个需要转化的列，各种可能的值对应的新列名  
2. andlePredict： 进行转化功能，对每个需要转化的输入数据，和colsMapMap中的key做对比，当相等时，将对应的新列名的值设定为1，其余值均设定为0，如果其中没有值与输入数据相等，则所有新列名对应的值均为0。

### Scale组件
利用训练得到的模型，对预测数据进行归一化。目前之前的归一化包括min-max-scale和standard-scale。该组件的文件包括"Scale", "MinMaxScale"，"StandardScale"

#### 参数和方法说明
1. initModel： 将模型的参数和结果，也就是Meta和param文件，反序列化从而对Serving模型初始化。其中从离线模型中继承的参数有：  
•	need_run：布尔类型，是否需要执行，如果为否，这个组件在后续预测时将被跳过  
•	method： 字符串类型，离线归一化的方法，包括min-max-scale和standards-scale  
•	mode：字符串类型，归一化对应的模式，包括"normal"和"cap"  
•	area: 字符串类型，归一化的范围，"all"表示全部列都归一化，"col"表示只对参数"scale_column"对应的列进行归一化  
•	scale_column: 字符串数组，参见"area"  
•	feat_upper: 字符串数组，未归一化前，每一列特征数据的上限，当数据值超过上限，则用上限值代替原来的值  
•	feat_lower: 字符串数组，未归一化前，每一列特征数据的下限，当数据值低于下限，则用下限值代替原来的值  
•	with_mean: 布尔类型，standard-scale方法对应的参数，当为True时候，原始数值会减去均值  
•	with_std: 布尔类型，standard-scale方法对应的参数，当为True时候，数值会除以标准差  
•	column_upper: 浮点数数组，表示每一列的上限，参见"feat_upper"  
•	column_lower: 浮点数数组，表示每一列的下限，参见"feat_lower"  
•	mean: 浮点数数组，表示每一列的均值，参见"with_mean"  
•	std: 浮点数数组，表示每一列的标准差，参见"with_std"  
2. transform: 利用离线训练产生的数据，对在线数据做同样处理。根据归一化方法的不同，处理方法分别对应min-max-scale和standard-scale

### Imputer组件
缺失值在线处理模块，若离线建模，dataio有应用到缺失值处理，则在线推理也会经过相应处理，和dataio一样，缺失值模块会优先于其他在线推理组件。缺失值组件对应的文件为"Imputer"

#### 参数和方法说明
1. Imputer: 初始化模型参数，包括:  
•	missingValueSet: 离线训练时，包含异常值的每一列的列名，即变量名  
•	missingReplaceValues: key-value格式，离线训练时，每一列的异常值和对应的替换值  
2. transform: 异常值替换功能，具体逻辑为:  
a.搜索变量是否在离线训练时候进行过异常值处理  
b.对进行过异常值处理的变量对应的值，搜索是否在missingReplaceValues中，若在，用替换值替代   

### Outlier组件
异常值在线处理模块，若离线建模，dataio有应用到异常值处理，则在线推理也会经过相应处理，和dataio一样，异常值模块会优先于其他在线推理组件，在缺失值组件后面。异常值对应的文件为"Outlier"。

#### 参数和方法说明
1. Outlier: 初始化模型参数，包括:  
•	outlierValueSet: 离线训练时，包含缺失值的每一列的列名，即变量名  
•	outlierReplaceValues: key-value格式，离线训练时，每一列的缺失值和对应的替换值  
2. transform: 缺失值替换功能，具体逻辑为:  
a.搜索变量是否在离线训练时候进行过缺失值处理  
b.对进行过缺失值处理的变量对应的值，搜索是否在missingReplaceValues中，若在，用替换值替代  

以上模块在在线推理阶段，皆是单边运行，不涉及多方交互的流程。