package com.webank.ai.fate.serving.core.rpc.core;

/**
 * @Description TODO
 * @Author
 **/
public class OutboundPackage<T> {


    public boolean hitCache = false;
    T data;

    public boolean isHitCache() {
        return hitCache;
    }

    public void setHitCache(boolean hitCache) {
        this.hitCache = hitCache;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
