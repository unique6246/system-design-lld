package observer;

import model.Event;

public interface Observer {
    void update(Event event);
}
