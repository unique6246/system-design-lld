package producer;

import model.Event;
import model.Priority;
import observer.Observer;
import observer.User;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationProducer implements Subject {

    List<User> users;

    public NotificationProducer() {
        this.users = new CopyOnWriteArrayList<>();
    }

    @Override
    public synchronized void subscribe(User user) {
        if (!users.contains(user)) {
            users.add(user);
        }
    }

    @Override
    public synchronized void unsubscribe(User user) {
        users.remove(user);
    }

    @Override
    public synchronized void notifyAll(Event event) {
        for(Observer user: users){
            user.update(event);
        }
    }

    public synchronized void pushEvent(Event event, Priority priority){
        if(event.getPriority().ordinal() >= priority.ordinal()){
            notifyAll(event);
        }
    }

    public synchronized List<User> getUsers() {
        return users;
    }

}
