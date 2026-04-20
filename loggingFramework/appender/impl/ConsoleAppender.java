package appender.impl;

import appender.LogAppender;
import module.LogMessage;

public class ConsoleAppender implements LogAppender {
    @Override
    public void append(LogMessage message) {
        System.out.println("[CONSOLE] : " + message.toString());
    }
}
