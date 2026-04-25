package notify;

import model.Event;

public interface Notification {
    void sendNotification(int userId, Event event);
}
