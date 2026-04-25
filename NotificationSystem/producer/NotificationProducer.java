package producer;

import model.Event;
import model.Priority;
import observer.Observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationProducer implements Subject {

    List<Observer> users;

    public NotificationProducer() {
        this.users = new CopyOnWriteArrayList<>();
    }

    @Override
    public void subscribe(Observer user) {
        users.add(user);
    }

    @Override
    public void unsubscribe(Observer user) {
        users.remove(user);
    }

    @Override
    public void notifyAll(Event event) {
        for(Observer user: users){
            user.update(event);
        }
    }

    public void pushEvent(Event event, Priority priority){
        if(event.getPriority().ordinal() >= priority.ordinal()){
            notifyAll(event);
        }
    }

}
