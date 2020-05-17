package com.webank.ai.fate.serving.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description TODO
 * @Author kaideng
 **/

@RequestMapping("/api")
@RestController
public class MonitorController {


    @GetMapping("/monitor/query")
    public  void  queryMonitorData(){



    }
}
