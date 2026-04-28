package observer;

import model.Event;
import notify.Notification;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class User implements Observer {
    private final int userId;
    private final String name;
    private final List<Event> events;
    private final List<Notification> notifications;

    public User(int userId, String name){
        this.userId = userId;
        this.name = name;
        this.events = new CopyOnWriteArrayList<>();
        this.notifications = new CopyOnWriteArrayList<>();
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    @Override
    public synchronized void update(Event event) {
        for (Notification notification: notifications){
            Event eventCopy = new Event(event.getId(), event.getMessage(), event.getPriority());
            eventCopy.setSource(notification.getClass().getSimpleName());
            notification.sendNotification(this.userId, eventCopy);
            events.add(eventCopy);
        }
    }
}
