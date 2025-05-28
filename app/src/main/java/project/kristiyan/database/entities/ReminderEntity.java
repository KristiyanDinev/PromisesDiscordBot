package project.kristiyan.database.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "reminder_service")
public class ReminderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(nullable = false)
    public String time;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    public UserEntity userEntity;

    public ReminderEntity() {}

    public ReminderEntity(int id, String time, UserEntity userEntity) {
        this.id = id;
        this.time = time;
        this.userEntity = userEntity;
    }
}
