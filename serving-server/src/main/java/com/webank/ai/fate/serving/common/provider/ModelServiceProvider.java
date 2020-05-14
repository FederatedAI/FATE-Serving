package com.webank.ai.fate.serving.common.provider;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.webank.ai.fate.api.mlmodel.manager.ModelServiceProto;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.ReturnResult;
import com.webank.ai.fate.serving.core.constant.StatusCode;
import com.webank.ai.fate.serving.core.model.Model;
import com.webank.ai.fate.serving.core.rpc.core.FateService;
import com.webank.ai.fate.serving.core.rpc.core.FateServiceMethod;
import com.webank.ai.fate.serving.core.rpc.core.InboundPackage;
import com.webank.ai.fate.serving.guest.provider.AbstractServingServiceProvider;
import com.webank.ai.fate.serving.model.ModelManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@FateService(name = "modelService", preChain = {
        "requestOverloadBreaker",
}, postChain = {
})
@Service
public class ModelServiceProvider extends AbstractServingServiceProvider {
    @Autowired
    ModelManager modelManager;


    @FateServiceMethod(name = "MODEL_LOAD")
    public Object load(Context context, InboundPackage data) {
        ModelServiceProto.PublishRequest publishRequest = (ModelServiceProto.PublishRequest) data.getBody();
        ReturnResult returnResult = modelManager.load(context, publishRequest);
        return returnResult;
    }

    @FateServiceMethod(name = "MODEL_PUBLISH_ONLINE")
    public Object bind(Context context, InboundPackage data) {
        ModelServiceProto.PublishRequest req = (ModelServiceProto.PublishRequest) data.getBody();
        ReturnResult returnResult = modelManager.bind(context, req);
        return returnResult;
    }

    @FateServiceMethod(name = "QUERY_MODEL")
    public ModelServiceProto.QueryModelResponse queryModel(Context context, InboundPackage data) {
        ModelServiceProto.QueryModelRequest req = (ModelServiceProto.QueryModelRequest) data.getBody();
        String content = modelManager.queryModel(context, req);
        ModelServiceProto.QueryModelResponse.Builder builder = ModelServiceProto.QueryModelResponse.newBuilder();

        JSONArray returnArray = JSONArray.parseArray(content);
        for (int i = 0; i < returnArray.size(); i++) {
            Model model = JSONObject.parseObject(returnArray.getString(i), Model.class);

            if (req.getQueryType() == 1) {
                model.setServiceId(req.getServiceId());
            }

            ModelServiceProto.ModelInfoEx.Builder modelExBuilder = ModelServiceProto.ModelInfoEx.newBuilder();
            modelExBuilder.setIndex(i);
            modelExBuilder.setTableName(model.getTableName());
            modelExBuilder.setNamespace(model.getNamespace());
            modelExBuilder.setServiceId(model.getServiceId());
            modelExBuilder.setContent(JSONObject.toJSONString(model));

            builder.addModelInfos(modelExBuilder.build());
        }

        builder.setRetcode(StatusCode.SUCCESS);
//        builder.setMessage(content);
        return builder.build();
    }


}
