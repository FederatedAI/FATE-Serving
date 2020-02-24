package com.webank.ai.fate.serving.core.exceptions;

/**
 * @Description TODO
 * @Author
 **/
public class LoadModelException extends RuntimeException  {

    public LoadModelException(){

        super("load model error");
    }
}
