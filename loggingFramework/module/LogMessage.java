package module;

import module.enums.LogLevel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.PrimitiveIterator;

public class LogMessage {
    private String message;
    private String time;
    private LogLevel level;

    public LogMessage(LogLevel level, String message){
        this.message = message;
        this.time = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
        this.level = level;
    }

    @Override
    public String toString(){
        return "[" + time + "] : [" + level + "] : " + message;
    }
}
