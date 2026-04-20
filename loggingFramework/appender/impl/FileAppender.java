package appender.impl;

import appender.LogAppender;
import module.LogMessage;

public class FileAppender implements LogAppender {
    @Override
    public void append(LogMessage message) {
        System.out.println("[FILE] : " + message.toString());
    }
}
