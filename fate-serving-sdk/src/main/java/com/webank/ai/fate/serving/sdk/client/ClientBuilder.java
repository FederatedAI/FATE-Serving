package com.webank.ai.fate.serving.sdk.client;


import com.google.common.base.Preconditions;
import com.webank.ai.fate.register.utils.StringUtils;
import com.webank.ai.fate.serving.core.bean.MetaInfo;
import com.webank.ai.fate.serving.core.utils.NetUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ClientBuilder {

    /**
     * without Register
     * @return
     */
    public static synchronized SimpleClient getClientWithoutRegister() {
        return new SimpleClient();
    }
    /**
     *
     * @param zkAddress   eg:   127.0.0.1:2181,127.0.0.2:2181,127.0.0.3:2181
     * @return
     */
    public static synchronized RegistedClient getClientUseRegister(String zkAddress) {
        return  getClientUseRegister(zkAddress,false);
    }

    /**
       sigleton
     */
    private static  RegistedClient  registedClient;

    private static  synchronized RegistedClient  getClientUseRegister(String zkAddress,boolean useAcl) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(zkAddress));
        MetaInfo.PROPERTY_ACL_ENABLE=useAcl;
        String[] addresses = zkAddress.split(",");
        for(String address:addresses){
            Preconditions.checkArgument(NetUtils.isValidAddress(address)||NetUtils.isLocalhostAddress(address),"register address is invalid");
        }
        if (registedClient == null) {
            RegistedClient client = new RegistedClient(zkAddress);
            registedClient= client;
        }else{
            throw  new  RuntimeException("register is already exist ,register address is "+ registedClient.getRegisterAddress());
        }
        return  registedClient;
    }

    /**
     *
     * @param zkAddress  eg:   127.0.0.1:2181,127.0.0.2:2181,127.0.0.3:2181
     * @param useAcl
     * @param aclUserName
     * @param aclPassword
     * @return
     */
    public static synchronized RegistedClient getClientUseRegister(String zkAddress, boolean useAcl, String aclUserName, String aclPassword) {
        MetaInfo.PROPERTY_ACL_ENABLE = useAcl;
        MetaInfo.PROPERTY_ACL_USERNAME = aclUserName;
        MetaInfo.PROPERTY_ACL_PASSWORD = aclPassword;
        return getClientUseRegister(zkAddress,useAcl);
    }

}
