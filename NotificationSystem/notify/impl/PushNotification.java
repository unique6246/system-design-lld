package notify.impl;

import model.Event;
import notify.Notification;

public class PushNotification implements Notification {
    @Override
    public void sendNotification(int userId, Event event) {
        System.out.println("sending PUSH notification to "+ userId + " with event '" + event.getMessage()+"'");
    }
}
