
package com.webank.ai.fate.serving.core.cache;

import java.util.concurrent.TimeUnit;

public interface Cache {

    void put(Object key, Object value);

    Object get(Object key);

}
