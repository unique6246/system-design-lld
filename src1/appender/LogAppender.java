package appender;

import module.LogMessage;

public interface LogAppender {
    void append(LogMessage message);
}
