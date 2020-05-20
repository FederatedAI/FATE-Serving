# Release 1.3.0
## Major Features and Improvements
* Hetero Secureboosting communication optimization: communication round is reduced to 1 by letting the host send a pre-computed host node route, which is used for inferencing, to the guest. 


# Release 1.2.0
## Major Features and Improvements
* Replace serving-router with a brand new service called serving-proxy, which supports authentication and inference request with HTTP or gRPC
* Decouple FATE-Serving and Eggroll, model is read directly from FATE-Flow
* Fixed a bug that got the remote inference result cache

# Release 1.1.2
## Major Features and Improvements
* Using metrics components and providing monitoring through JMX
* Host supports binding grpc interface with model information and registering it in zookeeper, and supports routing to different instances through model information.
* Guest adds a grpc interface for model binding. It supports model binding service id and registering it in zookeeper. The caller can route to different instances through service id. The service id is specified by fate_flow, which can uniquely represent a model.

# Release 1.1.1
## Major Features and Improvements
* Support indicating partial columns in Onehot Encoder

# Release 1.1
## Major Features and Improvements
* Add Online OneHotEncoder transform
* Add Online heterogeneous FeatureBinning transform
* Add heterogeneous SecureBoost Online Inference for binary-class classification，multi-class classfication and regression
* Add service governance, obtain IP and port of all GRPC interfaces through zookeeper 
* Support automatically to restore the loaded model when service restarts

# Release 1.0
## Major Features and Improvements
* Add online federated modeling pipeline DSL parser for online federated inference

# Release 0.3
## Major Features and Improvements
* Add multi-level cache for multi-party inference result
* Add startInferceJob and getInferenceResult interfaces to support the inference process asynchronization
* Normalized inference return code
* Real-time logging of inference summary logs and inferential detail logs
* Improve the loading of the pre and post processing adapter and data access adapter for host

# Release 0.2
## Major Features and Improvements
*	Dynamic Loading Federated Learning Models.
*	Real-time Prediction Using Federated Learning Models.

