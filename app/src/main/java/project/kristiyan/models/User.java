package project.kristiyan.models;

public class User {
    public long id;
    public String username;
    public String time;
    public String last_time_message_sent;

    public User(long id, String username, String time, String last_time_message_sent) {
        this.id = id;
        this.username = username;
        this.time = time;
        this.last_time_message_sent = last_time_message_sent;
    }
}
