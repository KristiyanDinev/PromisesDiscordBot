package project.kristiyan.models;

public class DUser {
    public long id;
    public String username;
    public String time;
    public boolean has_sent;

    public DUser(long id, String username, String time, boolean has_sent) {
        this.id = id;
        this.username = username;
        this.time = time;
        this.has_sent = has_sent;
    }
}
