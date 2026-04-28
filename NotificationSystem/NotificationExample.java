import model.Event;
import model.Priority;
import notify.impl.EmailNotification;
import notify.impl.PushNotification;
import notify.impl.SMSNotification;
import observer.User;
import singelton.NotificationSystem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class NotificationExample {
    public static void main(String[] args) {

        NotificationSystem system = NotificationSystem.getInstance();


        User user1 = new User(1,"John");
        User user2 = new User(2,"Alice");
        User user3 = new User(3, "Bob");

        user1.getNotifications().add(new EmailNotification());
        user2.getNotifications().add(new SMSNotification());
        user3.getNotifications().add(new PushNotification());
//        user3.getNotifications().add(new SMSNotification());

        system.addUser(user1);
        system.addUser(user2);
        system.addUser(user3);
//
//        system.publish(new Event(101,"maintenance now", Priority.HIGH));
//        system.publish(new Event(102,"new feature released", Priority.MEDIUM));
//        system.publish(new Event(103,"update the application", Priority.LOW));
//
////        system.addUserException(new User(102,"Praveen"));
//
//        system.notifyUser(user1,new Event(100, "update mobile number",Priority.HIGH) );
//        system.displayEvents();


        ExecutorService service = Executors.newFixedThreadPool(10);

        service.submit(() -> system.publish(new Event(104,"System outage", Priority.HIGH)));
        service.submit(() -> system.publish(new Event(105,"abc",Priority.LOW)));
        service.submit(() -> system.publish(new Event(106,"New feature available", Priority.MEDIUM)));
        service.submit(() -> system.publish(new Event(107,"Security update", Priority.HIGH)));
        service.submit(() -> system.publish(new Event(108,"Maintenance scheduled", Priority.MEDIUM)));
        service.submit(() -> system.publish(new Event(109,"Performance improvements", Priority.LOW)));
        service.submit(() -> system.publish(new Event(110,"Bug fixes released", Priority.MEDIUM)));
        service.submit(() -> system.publish(new Event(111,"New user interface", Priority.HIGH)));
        service.submit(() -> system.publish(new Event(112,"API changes", Priority.MEDIUM)));
        service.submit(() -> system.publish(new Event(113,"Database migration", Priority.HIGH)));
        service.submit(() -> system.publish(new Event(114,"Cloud deployment", Priority.MEDIUM)));
        service.submit(system::displayEvents);
        service.shutdown();
    }
}