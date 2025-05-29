package project.kristiyan.database.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(nullable = false)
    public long id;

    @Column(nullable = false)
    public String name;

    public UserEntity(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public UserEntity() {}
}
