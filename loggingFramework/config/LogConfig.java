package config;

import appender.LogAppender;
import module.enums.LogLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogConfig {
    private LogLevel level;
    private List<LogAppender> appenders;

    // Accept one or more appenders using varargs
    public LogConfig(LogLevel level, LogAppender... appenders) {
        this.level = level;
        this.appenders = new ArrayList<>(Arrays.asList(appenders));
    }

    public List<LogAppender> getAppenders() {
        return this.appenders;
    }

    public LogLevel getLevel() {
        return this.level != null ? this.level : LogLevel.INFO;
    }

    // Add a new appender to the existing list
    public void addAppender(LogAppender appender) {
        this.appenders.add(appender);
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }
}
