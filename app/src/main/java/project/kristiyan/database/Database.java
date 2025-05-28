package project.kristiyan.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Database {
    private final EntityManagerFactory emf;

    public Database() {
        emf = Persistence.createEntityManagerFactory("db");
    }

    public void close() {
        emf.close();
    }

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }
}
