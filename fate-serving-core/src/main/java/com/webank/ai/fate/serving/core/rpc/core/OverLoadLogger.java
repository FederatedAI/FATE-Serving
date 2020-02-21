package com.webank.ai.fate.serving.core.rpc.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description TODO
 * @Author
 **/
public class OverLoadLogger {

    static Logger logger = LoggerFactory.getLogger(OverLoadLogger.class);



//    private static final Map<String,Logger> container = new HashMap<>();
//    public Logger getLogger(String name) {
//        Logger logger = container.get(name);
//        if(logger != null) {
//            return logger;
//        }
//        synchronized (LoggerBuilder.class) {
//            logger = container.get(name);
//            if(logger != null) {
//                return logger;
//            }
//            logger = build(name);
//            container.put(name,logger);
//        }
//        return logger;
//    }
//
//
//
//
//    private static Logger build(String name) {
//        RollingFileAppender errorAppender =new AppenderTest().getAppender(name,Level.ERROR);
//        RollingFileAppender infoAppender =new AppenderTest().getAppender(name,Level.INFO);
//        RollingFileAppender warnAppender =new AppenderTest().getAppender(name,Level.WARN);
//        RollingFileAppender debugAppender =new AppenderTest().getAppender(name,Level.DEBUG);
//        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//        Logger logger = context.getLogger("FILE-" + name);
//        //设置不向上级打印信息
//        logger.setAdditive(false);
//        logger.addAppender(errorAppender);
//        logger.addAppender(infoAppender);
//        logger.addAppender(warnAppender);
//        logger.addAppender(debugAppender);
//
//        return logger;
//    }




//    public RollingFileAppender getAppender(String name, Level level){
//        DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.SIMPLIFIED_CHINESE);
//        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
//        //这里是可以用来设置appender的，在xml配置文件里面，是这种形式：
//        // <appender name="error" class="ch.qos.logback.core.rolling.RollingFileAppender">
//        RollingFileAppender appender = new RollingFileAppender();
////        ConsoleAppender consoleAppender = new ConsoleAppender();
//
////        //这里设置级别过滤器
////        LevelController levelController = new LevelController();
////        LevelFilter levelFilter = levelController.getLevelFilter(level);
////        levelFilter.start();
////        appender.addFilter(levelFilter);
//
//
//        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
//        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
//        appender.setContext(context);
//        //appender的name属性
//        appender.setName("FILE-" + name);
//        //设置文件名
//        appender.setFile(OptionHelper.substVars("E:/eppLog/"+ name+"/" + format.format(new Date())+"/"+ level.levelStr + ".log",context));
//
//        appender.setAppend(true);
//
//        appender.setPrudent(false);
//
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
//        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
//        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
//        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
//        encoder.setContext(context);
//        //设置格式
//        encoder.setPattern("%d %p (%file:%line\\)- %m%n");
//        encoder.start();
//
//        //加入下面两个节点
//        appender.setRollingPolicy(policy);
//        appender.setEncoder(encoder);
//        appender.start();
//        return appender;
//    }



}
