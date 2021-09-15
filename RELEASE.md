# Release 2.1.0
## Major Features and Improvements
* Support multi-host algorithm model online prediction,currently, the supported multi-host algorithms include HeteroLR and HeteroSecureBoost.
* Support to display model information in pipeline on admin page.
* Increase the model synchronization function, support the synchronous replication of models between different serving-server nodes in the cluster, which is convenient for machine expansion and contraction.
* Admin page model operation/display function optimization.
* Add a cluster one-click self-check tool on the admin page, you can view the cluster service self-check status, and the cluster will be self-checked regularly from the background, and the user can also take the initiative to perform one-click self-check.
* Add routing table editing function, you can directly edit the routing table information of the current serving-proxy configuration through the page.
* Service topology map optimization, showing the true mapping relationship between each machine, the specified machine can be selected on the topology map and its details can be viewed.

# Release 2.0.0
## Major Features and Improvements
*  For single inference, the guest side and host side of version 2.0. * will be calculated in parallel, thus reducing the time consumption.

*  Batch inference, which is a new feature introduced in version 2.0 *. To batch submit a batch of data to be predicted in one request, which greatly improves the throughput.

*  Parallel computing: in version 1.3. * the  inference of the guest side and the  inference of the host side are serial. From version 2.0, the prediction of the guest side and the host side will adopt the method of parallel  inference. The inference of each party can be divided into subtasks according to the number of features and then parallel computing.

*  Introduce a new component serving-admin, which will provide the visual operation interface of cluster, including model management, traffic monitoring, configuration view, service management and so on.

*  The new model persistence / recovery mode: when the service server is restarted, version 1.3. * uses the playback push model request to restore the model when the instance is restarted, and version 2.0. * uses the method of directly recovering the memory data to restore the model.

*  Java SDK. With this SDK, you can use the service governance related functions of Fat-service, such as service automatic discovery and routing.

*  In the new extension module, the user-defined development part (such as: host side feature acquisition adapter interface development) is put into this module, so as to separate from the core source code.

*  Support a variety of caching methods. Fat-service strongly relies on redis in version 1.3 *. And no longer relies on redis since version 2.0 *. You can choose not to use cache, use local memory cache, and use redis.

*  Change the internal prediction process, reconstruct the core code, remove the pre-processing and post-processing components, and use the unified exception handling. The algorithm component is no longer tightly coupled with the RPC interface.

* Provide command line tools to query configuration and model information

# Release 1.3.2
## Major Features and Improvements
* Add input feature hitting rate counting for HeteroLR and Hetero SecureBoost

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

