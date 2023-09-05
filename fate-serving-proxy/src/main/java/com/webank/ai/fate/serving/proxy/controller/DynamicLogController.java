package com.webank.ai.fate.serving.proxy.controller;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * @author hcy
 */
@RequestMapping("/proxy")
@RestController
public class DynamicLogController {
    private static final Logger logger = LoggerFactory.getLogger(DynamicLogController.class);

    @GetMapping("/alterSysLogLevel/{level}")
    public String alterSysLogLevel(@PathVariable String level){
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.getLogger("ROOT").setLevel(Level.valueOf(level));
            return "ok";
        } catch (Exception ex) {
            logger.error("proxy alterSysLogLevel failed : " + ex);
            return "failed";
        }

    }

    @GetMapping("/alterPkgLogLevel")
    public String alterPkgLogLevel(@RequestParam String level, @RequestParam String pkgName){
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.getLogger(pkgName).setLevel(Level.valueOf(level));
            return "ok";
        } catch (Exception ex) {
            logger.error("proxy alterPkgLogLevel failed : " + ex);
            return "failed";
        }
    }
}
