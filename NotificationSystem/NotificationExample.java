import model.Event;
import model.Priority;
import notify.impl.EmailNotification;
import notify.impl.PushNotification;
import notify.impl.SMSNotification;
import observer.User;
import singelton.NotificationSystem;

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
        user3.getNotifications().add(new SMSNotification());

        system.addUser(user1);
        system.addUser(user2);
        system.addUser(user3);

        system.publish(new Event(101,"maintenance now", Priority.HIGH));
        system.publish(new Event(102,"new feature released", Priority.MEDIUM));
        system.publish(new Event(103,"update the application", Priority.LOW));

//        system.addUserException(new User(102,"Praveen"));

        system.notifyUser(user1,new Event(100, "update mobile number",Priority.HIGH) );
        system.displayEvents();
    }
}