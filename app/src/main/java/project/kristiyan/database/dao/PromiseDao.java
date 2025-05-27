package project.kristiyan.database.dao;

import jakarta.persistence.EntityManager;

public class PromiseDao {
    private EntityManager em;

    public PromiseDao(EntityManager em) {
        this.em = em;
    }
}
