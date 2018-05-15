package sqlcollector.core.logs;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class L4j {

    private static final L4j instance;
    static Logger log;

    static {
        instance = new L4j();
    }

    private L4j(){
        log = LogManager.getLogger();
    }

    public static Logger getL4j() {
        return log;
    }

    public static Logger getLogger(String sLoggerName, String sLogPath, String sLogFileName, String sLogLevel, String sLogPattern, String sLogRollingPattern) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        
        if (sLoggerName == null) sLoggerName = "SQLCollector";
        if (sLogPath == null) sLogPath = "logs/";
        if (sLogFileName == null) sLogFileName = "SQLCollector";
        if (sLogLevel == null) sLogLevel = "error";
        if (sLogPattern == null) sLogPattern = "[%5p] - %d{yyyy-MM-dd HH:mm:ss.SSS} - %m%n";
        if (sLogRollingPattern == null) sLogRollingPattern = "-%d{yyyy-MM-dd}";
        String sLogFilePattern = sLogPath + sLogFileName + sLogRollingPattern + ".log.gz";
        System.out.println("############################");
    	System.out.println("# Creating Logger with values:");
    	System.out.println("# LogPath:           " + sLogPath);
    	System.out.println("# LogFileName:       " + sLogFileName);
    	System.out.println("# LogLevel:          " + sLogLevel);
    	System.out.println("# LogPattern:        " + sLogPattern);
    	System.out.println("# LogRollingPattern: " + sLogRollingPattern);
    	System.out.println("# LogFilePattern:    " + sLogFilePattern);
        System.out.println("############################");

        PatternLayout layout = PatternLayout.newBuilder()
          .withConfiguration(config)
          .withPattern(sLogPattern)
          .build();
        
        TimeBasedTriggeringPolicy triggeringPolicy = TimeBasedTriggeringPolicy.newBuilder()
        		.withInterval(1)
        		.withModulate(true)
        		.build();

        Appender appender = RollingFileAppender.newBuilder()
          .setConfiguration(config)
          .withLayout(layout)
          .withName("rollingFileAppender")
          .withFileName(sLogPath+sLogFileName+".log")
          .withFilePattern(sLogFilePattern)
          .withPolicy(triggeringPolicy)
          .build();
            
        appender.start();
        config.addAppender(appender);
        
        AppenderRef ref = AppenderRef.createAppenderRef("rollingFileAppender", null, null);
        AppenderRef[] refs = new AppenderRef[] { ref };

        LoggerConfig loggerConfig = LoggerConfig
          .createLogger(false, Level.getLevel(sLogLevel.toUpperCase()), sLoggerName, "true", refs, null, config, null);
        loggerConfig.addAppender(appender, null, null);
        config.addLogger(sLoggerName, loggerConfig);
        ctx.updateLoggers();

        Logger logger = LogManager.getLogger(sLoggerName);
        logger.info("############################");
    	logger.info("# Logger created with values:");
    	logger.info("# LogPath:           " + sLogPath);
    	logger.info("# LogFileName:       " + sLogFileName);
    	logger.info("# LogLevel:          " + sLogLevel);
    	logger.info("# LogPattern:        " + sLogPattern);
    	logger.info("# LogRollingPattern: " + sLogRollingPattern);
        logger.info("############################");
        return logger;        
    }    

    public void critical(String msg) {
        if (log.isFatalEnabled()) {
            log.fatal(msg);
        }
    }

    public void error(String msg) {
        if (log.isErrorEnabled()) {
            log.error(msg);
        }
    }

    public void error(String msg, Throwable t) {
        if (log.isErrorEnabled()) {
            log.error(msg, t);
        }
    }

    public void warn(String msg) {
        if (log.isWarnEnabled()) {
            log.warn((Object)msg);
        }
    }

    public void notice(String msg) {
        if (log.isInfoEnabled()) {
            log.info(msg);
        }
    }

    public void info(String msg) {
        if (log.isInfoEnabled()) {
            log.info(msg);
        }
    }

    public void debug(String msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    public void debug(String msg, Throwable t) {
        if (log.isDebugEnabled()) {
            log.debug(msg, t);
        }
    }
}
