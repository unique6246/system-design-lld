package notify.impl;

import model.Event;
import notify.Notification;

public class SMSNotification implements Notification {
    @Override
    public void sendNotification(int userId, Event event) {
        System.out.println("sending SMS notification to "+ userId + " with event '" + event.getMessage()+"'");
    }
}
