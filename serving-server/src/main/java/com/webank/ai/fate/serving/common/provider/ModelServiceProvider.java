package com.webank.ai.fate.serving.common.provider;


import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ModelActionType;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.core.rpc.core.OutboundPackage;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import com.webank.ai.fate.serving.model.ModelManager;
import com.webank.ai.fate.serving.model.ModelManager;
import org.springframework.beans.factory.annotation.Autowired;

public class ModelServiceProvider extends AbstractServingServiceProvider{
    @Autowired
    ModelManager modelManager;

    final  String GET_MODEL_BY_SERVICE_ID = ModelActionType.GET_MODEL_BY_SERVICE_ID.name();
    final  String MODEL_LOAD =  ModelActionType.MODEL_LOAD.name();
    final  String MODEL_PUBLISH_ONLINE =  ModelActionType.MODEL_PUBLISH_ONLINE.name();
    final  String UNLOAD = ModelActionType.UNLOAD.name();
    final  String UNBIND = ModelActionType.UNBIND.name();
    final  String LIST_ALL_MODEL = ModelActionType.LIST_ALL_MODEL.name();
    final  String GET_MODEL_BY_TABLE_NAME_AND_NAMESPACE  = ModelActionType.GET_MODEL_BY_TABLE_NAME_AND_NAMESPACE.name();


    @Override
    public Object doService(Context context, InboundPackage data, OutboundPackage outboundPackage) {

        String  actionType  = context.getActionType();

        switch (actionType){
            case   "GET_MODEL_BY_SERVICE_ID" :

                data.getBody();
                String  serviceId = null;
                modelManager.getModelByServiceId(serviceId);
                break;
            case   "MODEL_LOAD" :
                ModelServiceProto.PublishRequest  publishRequest = (ModelServiceProto.PublishRequest)data.getBody();
                ReturnResult returnResult = modelManager.load(context,publishRequest);
                return  returnResult;

            case   "MODEL_PUBLISH_ONLINE" :
                ModelServiceProto.PublishRequest req=  (ModelServiceProto.PublishRequest)data.getBody();
                modelManager.bind(context,req);
                break;
            case  "UNBIND":

                modelManager.unbind();
                break;

            case  "UNLOAD":
                modelManager.unload();
                break;
            case  "LIST_ALL_MODEL":
                modelManager.listAllModel();
                break;
            case "GET_MODEL_BY_TABLE_NAME_AND_NAMESPACE":
                modelManager.getModelByTableNameAndNamespace()
                break;
                default:
        }

        return null;
    }
}
