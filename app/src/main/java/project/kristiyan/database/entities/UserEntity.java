package project.kristiyan.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
