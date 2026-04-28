package singelton;

import model.Event;
import observer.User;
import producer.NotificationProducer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationSystem {
    private final NotificationProducer producer;
    private static volatile NotificationSystem INSTANCE = null;

    private NotificationSystem(){
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

    public synchronized void addUser(User user){
        if (findUser(user.getUserId())){
            System.out.println("User with id " + user.getUserId() + " already exists.");
            return;
        }
        producer.subscribe(user);
    }

    public synchronized void removeUser(User user){
        if (findUser(user.getUserId())){
            System.out.println("User with id " + user.getUserId() + " does not exist.");
            return;
        }
        producer.unsubscribe(user);
    }

    public synchronized void publish(Event event){
        producer.notifyAll(event);
    }

    public void notifyUser(User user, Event event){
        for (User user1 : producer.getUsers()) {
            if (user1.getUserId() == user.getUserId()){
                user1.update(event);
                break;
            }
        }
    }

    public void displayEvents(){
        for (User user:producer.getUsers()){
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

    private boolean findUser(int id){
        for (User user: producer.getUsers()){
            if (user.getUserId() == id) return true;
        }
        return false;
    }
}
