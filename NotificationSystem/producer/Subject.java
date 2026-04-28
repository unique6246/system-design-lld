package producer;

import model.Event;
import observer.Observer;
import observer.User;

import java.net.UnknownServiceException;

public interface Subject {
    void subscribe(User user);
    void unsubscribe(User user);
    void notifyAll(Event event);
}
