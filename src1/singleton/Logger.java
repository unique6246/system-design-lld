package singleton;

import appender.LogAppender;
import config.LogConfig;
import module.LogMessage;
import module.enums.LogLevel;

public class Logger {
    private static volatile Logger Instance = null;
    private LogConfig logConfig;

    private Logger (LogConfig logConfig){
        this.logConfig = logConfig;
    }

    public static Logger getInstance(LogConfig logConfig){
        if (Instance == null){
            synchronized (Logger.class){
                if (Instance == null){
                    Instance =  new Logger(logConfig);
                }
            }
        }
        return Instance;
    }

    public synchronized void setLogConfig(LogConfig logConfig){
        this.logConfig = logConfig;
    }

    public LogConfig getLogConfig(){
        return this.logConfig;
    }

    public synchronized void log(LogLevel level, String message){
        if (level.getPriority() >= logConfig.getLevel().getPriority()) {
            LogMessage logMessage = new LogMessage(level, message);
            // Fan-out: send to every appender in the list
            for (LogAppender appender : logConfig.getAppenders()) {
                appender.append(logMessage);
            }
        }
    }

    public void debug (String message) { log(LogLevel.DEBUG, message);}
    public void info (String message) { log(LogLevel.INFO, message);}
    public void warn (String message) { log(LogLevel.WARNING, message);}
    public void error (String message) { log(LogLevel.ERROR, message);}

}
