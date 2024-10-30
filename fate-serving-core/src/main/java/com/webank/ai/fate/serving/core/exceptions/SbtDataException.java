package com.webank.ai.fate.serving.core.exceptions;

import com.webank.ai.fate.serving.core.constant.StatusCode;

public class SbtDataException extends  BaseException{

    public  SbtDataException(){
        super(StatusCode.SBT_DATA_ERROR , "sbt model data error");
    }

}
