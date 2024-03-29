package com.webank.ai.fate.serving.common.utils;

import org.apache.commons.net.telnet.TelnetClient;

public class TelnetUtil {

    public  static  boolean   tryTelnet(String  host ,int  port){
        TelnetClient telnetClient = new TelnetClient("vt200");
        telnetClient.setDefaultTimeout(5000);
        boolean isConnected = false;
        try {
            telnetClient.connect(host, port);
            isConnected = true;
            telnetClient.disconnect();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return isConnected;
    }

}
