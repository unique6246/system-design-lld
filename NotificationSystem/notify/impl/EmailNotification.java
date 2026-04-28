package notify.impl;

import model.Event;
import notify.Notification;

public class EmailNotification implements Notification {
    @Override
    public synchronized void sendNotification(int userId, Event event) {
        System.out.println("sending EMAIL notification to "+ userId + " with event '" + event.getMessage()+"'");
    }
}
