package com.webank.ai.fate.register.interfaces;


import com.webank.ai.fate.register.url.URL;

import java.util.List;


public interface RegistryService {


    void register(URL url);

    void unregister(URL url);


    void subscribe(URL url, NotifyListener listener);


    void unsubscribe(URL url, NotifyListener listener);


    List<URL> lookup(URL url);

}