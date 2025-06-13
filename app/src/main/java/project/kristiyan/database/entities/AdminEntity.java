package project.kristiyan.database.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "admins")
public class AdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public long user_id;

    public AdminEntity() {}
}
