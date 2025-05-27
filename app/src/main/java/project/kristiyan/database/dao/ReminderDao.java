package project.kristiyan.database.dao;

import jakarta.persistence.EntityManager;

public class ReminderDao {
    private EntityManager em;

    public ReminderDao(EntityManager em) {
        this.em = em;
    }
}
