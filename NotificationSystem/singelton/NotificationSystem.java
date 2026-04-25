package singelton;

import model.Event;
import observer.User;
import producer.NotificationProducer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationSystem {
    private final List<User> users;
    private final NotificationProducer producer;
    private static volatile NotificationSystem INSTANCE = null;

    private NotificationSystem(){
        this.users = new CopyOnWriteArrayList<>();
        this.producer = new NotificationProducer();
    }
    public static NotificationSystem getInstance(){
        if (INSTANCE == null){
            synchronized (NotificationSystem.class){
                if (INSTANCE == null){
                    INSTANCE = new NotificationSystem();
                }
            }
        }
        return INSTANCE;
    }

    public void addUser(User user){
        users.add(user);
        producer.subscribe(user);
    }

    public void removeUser(User user){
        users.remove(user);
        producer.unsubscribe(user);
    }

    public void publish(Event event){
        producer.notifyAll(event);
    }

    public void notifyUser(User user, Event event){
        for (User user1 : users) {
            if (user1.getUserId() == user.getUserId()){
                user1.update(event);
                break;
            }
        }
    }

    public void displayEvents(){
        for (User user:users){
            System.out.println("User: " + user.getName() + " has events: ");
            for (Event event: user.getEvents()){
                System.out.println("     -> "+ event);
            }
        }
    }
//    public void addUserException(User user){
//        for(Observer user1:users){
//            users.add(user);
//        }
//    }

}
