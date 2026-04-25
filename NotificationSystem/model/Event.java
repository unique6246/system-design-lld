package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Event {
    private final int id;
    private final String message;
    private final Priority priority;
    private final String time;
    private String source;

    public Event(int id, String message, Priority priority) {
        this.id = id;
        this.message = message;
        this.priority = priority;
        this.source = "";
        this.time = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", message='" + message + '\'' +
                ", priority=" + priority + '\'' +
                ", time=[" + time +"]"+
                '}';
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String s){
        this.source = s;
    }
    public Priority getPriority() {
        return priority;
    }
}
