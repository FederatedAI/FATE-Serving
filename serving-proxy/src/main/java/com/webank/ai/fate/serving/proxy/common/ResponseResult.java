package com.webank.ai.fate.serving.proxy.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseResult<T> {

    @JsonProperty(value = "code")
    private String code = "0";

    @JsonProperty(value = "message")
    private String msg = "success";

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @JsonProperty(value = "data")
    private T data;

    public ResponseResult(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public ResponseResult() {

    }

    public ResponseResult(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseResult(String code, T data) {
        this.code = code;
        this.data = data;
    }



    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }



}