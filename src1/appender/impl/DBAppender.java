package appender.impl;

import appender.LogAppender;
import module.LogMessage;

import java.util.ArrayList;
import java.util.List;

public class DBAppender implements LogAppender {
    private static final List<String> messages = new ArrayList<>();

    @Override
    public void append(LogMessage message) {
        String response = "[DB] : " + message.toString();
        messages.add(response);
        System.out.println(response);
    }

    public void getDBLogs(){
        for(String message:messages){
            System.out.println(message);
        }
    }
}
