package com.webank.ai.fate.serving.grpc.service;

import com.webank.ai.fate.register.router.RouterService;
import com.webank.ai.fate.register.url.URL;
import com.webank.ai.fate.register.zookeeper.ZookeeperRegistry;
import com.webank.ai.fate.serving.common.health.*;
import com.webank.ai.fate.serving.common.model.Model;
import com.webank.ai.fate.serving.common.model.ModelProcessor;
import com.webank.ai.fate.serving.common.utils.TelnetUtil;
import com.webank.ai.fate.serving.core.bean.Context;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.model.ModelManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class HealthCheckEndPointService implements HealthCheckAware{

    @Autowired(required = false)
    private RouterService routerService;
    @Autowired
    private ModelManager  modelManager;

    private static HealthCheckEndPointService checkEndPointServiceInstance;

    private  void  checkFateFlow(HealthCheckResult  healthCheckResult){
        if (routerService != null) {
            String transferUri = "flow/online/transfer";
            URL url = URL.valueOf(transferUri);
            List urls = routerService.router(url);
            if(urls== null||urls.size()==0){
                healthCheckResult.getRecords().add(new  HealthCheckRecord(HealthCheckItemEnum.CHECK_FATEFLOW_IN_ZK.getItemName(), "fateflow is not found in zookeeper ",HealthCheckStatus.warn));
            }else{
                healthCheckResult.getRecords().add(new  HealthCheckRecord(HealthCheckItemEnum.CHECK_FATEFLOW_IN_ZK.getItemName(), "fateflow is found in zookeeper",HealthCheckStatus.ok));
            }
        }
    }
    private void  checkModel(HealthCheckResult healthCheckResult){
        List<Model> models =  modelManager.listAllModel();
        if(models==null||models.size()>0){
            healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_MODEL_LOADED.getItemName(),"model is loaded",HealthCheckStatus.ok));
        }
        else{
            healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_MODEL_LOADED.getItemName(),"model is not loaded",HealthCheckStatus.warn));
        }
    }



    private  void  checkDefaultFateflow(HealthCheckResult  healthCheckResult){
        String fullUrl = MetaInfo.PROPERTY_MODEL_TRANSFER_URL;
        if(StringUtils.isNotBlank(fullUrl)) {
            String host = fullUrl.substring(fullUrl.indexOf('/') + 2, fullUrl.lastIndexOf(':'));
            int port = Integer.parseInt(fullUrl.substring(fullUrl.lastIndexOf(':') + 1,
                    fullUrl.indexOf('/', fullUrl.lastIndexOf(':'))));
            boolean isConnected = TelnetUtil.tryTelnet(host, port);
            if (!isConnected) {
                String result1 = String.format(" %s can not connected", fullUrl);
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_DEFAULT_FATEFLOW.getItemName(),result1,HealthCheckStatus.warn));
            }else{
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_DEFAULT_FATEFLOW.getItemName(),"default fateflow url is ok",HealthCheckStatus.ok));
            }

        }
    }

    private void checkReturnAbleNum(HealthCheckResult  healthCheckResult){
        List<Model> models =  modelManager.listAllModel();
        if(models != null||models.size() > 0){
            for(Model model : models){
                ModelProcessor modelProcessor = model.getModelProcessor();
                if(modelProcessor.getReturnNums() > 1){
                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_RETURNABLE_NUM.getItemName(),"There are more than one returnable components in the " + model.getResourceName(),HealthCheckStatus.warn));
                }else{
                    healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_RETURNABLE_NUM.getItemName(), "The number of returnable components in the " + model.getResourceName() + " is norma",HealthCheckStatus.ok));
                }
            }
        }
    }

    @Autowired(required = false)
    ZookeeperRegistry zookeeperRegistry;

    private  void  checkZkConfig(HealthCheckResult  healthCheckResult){
        if(zookeeperRegistry==null){
            healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ZOOKEEPER_CONFIG.getItemName(),"zookeeper is not used or config is invalid",HealthCheckStatus.warn));
        }else{
            boolean isConnected  = zookeeperRegistry.getZkClient().isConnected();
            if(isConnected){
                healthCheckResult.getRecords().add(new HealthCheckRecord(HealthCheckItemEnum.CHECK_ZOOKEEPER_CONFIG.getItemName(),"zookeeper can not touched",HealthCheckStatus.error));
            }
        }

    }





    private  void  metircCheck(HealthCheckResult  healthCheckResult){


    }

    @Override
    public HealthCheckResult check(Context context) {

        if(MetaInfo.PROPERTY_ALLOW_HEALTH_CHECK) {
            HealthCheckItemEnum[] items = HealthCheckItemEnum.values();
            HealthCheckResult  healthCheckResult = new  HealthCheckResult();
            Arrays.stream(items).filter((item) -> {
                HealthCheckComponent healthCheckComponent = item.getComponent();
                return healthCheckComponent == HealthCheckComponent.ALL || healthCheckComponent == HealthCheckComponent.SERVINGSERVER;
            }).forEach((item) -> {
                        switch (item) {

                            case CHECK_MEMORY_USAGE:
                                HealthCheckUtil.memoryCheck(healthCheckResult);
                                break;
                            case CHECK_MODEL_LOADED:
                                checkModel(healthCheckResult);
                                break;
                            case CHECK_FATEFLOW_IN_ZK:
                                checkFateFlow(healthCheckResult);
                                break;
                            case CHECK_DEFAULT_FATEFLOW:
                                checkDefaultFateflow(healthCheckResult);
                                break;
                            case CHECK_RETURNABLE_NUM:
                                checkReturnAbleNum(healthCheckResult);
                                break;
                        }
                    }
            );
            return healthCheckResult;
        }else{
            return  null;
        }


    }

    @Autowired
    public void setCheckEndPointServiceInstance(HealthCheckEndPointService checkEndPointServiceInstance){
        HealthCheckEndPointService.checkEndPointServiceInstance = checkEndPointServiceInstance;
    }

    public static HealthCheckEndPointService getCheckEndPointServiceInstance() {
        return HealthCheckEndPointService.checkEndPointServiceInstance;
    }
}
