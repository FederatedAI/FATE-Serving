package com.webank.ai.fate.serving.core.bean;


import java.util.List;
import java.util.Map;

public interface TwoPhaseComponent  {

     public Map<String,Object> localInference(Context context,List<Map<String,Object>> input);

     public  Map<String,Object>  mergeRemoteInference(Context context,Map<String,Object> input);

}
