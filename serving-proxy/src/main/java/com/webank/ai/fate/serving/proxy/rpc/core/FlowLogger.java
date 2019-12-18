package com.webank.ai.fate.serving.proxy.rpc.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.util.OptionHelper;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @Description TODO
 * @Author
 * 坑爹玩意，需要根据业务字段打到不同文件
 **/
public class FlowLogger {


    private static final Map<String,Logger> container = new HashMap<>();
    public static  Logger getLogger(String name) {
        Logger logger = container.get(name);
        if(logger != null) {
            return logger;
        }
        synchronized (FlowLogger.class) {
            logger = container.get(name);
            if(logger != null) {
                return logger;
            }
            logger = build(name);
            container.put(name,logger);
        }
        return logger;
    }




    private static Logger build(String name) {

        RollingFileAppender infoAppender =getAppender(name,Level.INFO);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("FILE-" + name);
        //设置不向上级打印信息
        logger.setAdditive(false);

        logger.addAppender(infoAppender);


        return logger;
    }




    public static RollingFileAppender getAppender(String name, Level level){
        DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.SIMPLIFIED_CHINESE);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        //这里是可以用来设置appender的，在xml配置文件里面，是这种形式：
        // <appender name="error" class="ch.qos.logback.core.rolling.RollingFileAppender">
        RollingFileAppender appender = new RollingFileAppender();
//        ConsoleAppender consoleAppender = new ConsoleAppender();

//        //这里设置级别过滤器
        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setLevel(level);
        levelFilter.setOnMatch(FilterReply.ACCEPT);
        levelFilter.setOnMismatch(FilterReply.DENY);
        levelFilter.start();
        appender.addFilter(levelFilter);


        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        appender.setContext(context);
        //appender的name属性
        appender.setName("FILE-" + name);
        //设置文件名
        appender.setFile(OptionHelper.substVars( "logs/flow-"+name+".log",context));

        appender.setAppend(true);

        appender.setPrudent(false);

        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();


//        　　　　<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
//　　　　　　　　<fileNamePattern>logFile.%d{yyyy-MM-dd}.log</fileNamePattern>



        rollingPolicy.setFileNamePattern("logs/flow-"+name+".%d{yyyy-MM-dd}.log");
        rollingPolicy.setMaxHistory(30);
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(appender);
        rollingPolicy.start();
        appender.setRollingPolicy(rollingPolicy);



//        //设置文件创建时间及大小的类
//        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
//        //文件名格式
//        String fp = OptionHelper.substVars("E:/eppLog/"+ name +"/" + format.format(new Date())+"/"+ level.levelStr + "/.%d{yyyy-MM-dd}.%i.log",context);
//        //最大日志文件大小
//        policy.setMaxFileSize(FileSize.valueOf("128MB"));
//        //设置文件名模式
//        policy.setFileNamePattern(fp);
//        //设置最大历史记录为15条
//        policy.setMaxHistory(15);
//        //总大小限制
//        policy.setTotalSizeCap(FileSize.valueOf("32GB"));
//        //设置父节点是appender
//        policy.setParent(appender);
//        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
//        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
//        policy.setContext(context);
//        policy.start();
//
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        encoder.setContext(context);
        //设置格式
        encoder.setPattern("%d %p (%file:%line\\)- %m%n");
        encoder.start();

        //加入下面两个节点
//        appender.setRollingPolicy(policy);
        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }

}
