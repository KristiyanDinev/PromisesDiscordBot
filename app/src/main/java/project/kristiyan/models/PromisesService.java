package project.kristiyan.models;

public class PromisesService {

    public int id;
    public String time;  // 8:30 time/zone
    public int user_id;

    public PromisesService(int id, String time, int user_id) {
        this.id = id;
        this.time = time;
        this.user_id = user_id;
    }
}
