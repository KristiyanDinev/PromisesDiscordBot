package project.kristiyan.database.dao;

import jakarta.persistence.EntityManager;

public class UserDao {
    private EntityManager em;

    public UserDao(EntityManager em) {
        this.em = em;
    }
}
