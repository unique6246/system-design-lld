package producer;

import model.Event;
import observer.Observer;

public interface Subject {
    void subscribe(Observer user);
    void unsubscribe(Observer user);
    void notifyAll(Event event);
}
