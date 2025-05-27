package project.kristiyan.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import project.kristiyan.database.entities.UserEntity;

import java.util.*;

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

    public List<UserEntity> getUsers_Promises(int page) {
        return  null;
    }

    public List<UserEntity> getUsers_Reminder(int page) {
        return  null;
    }

    public void deleteUser(long id) {
    }

    public void subscribeUserToPromiseService(UserEntity user, String time) {
    }

    public void subscribeUserToReminderService(UserEntity user, String time) {
    }

    public void updateUserSubscriptionPromises(long id, String time)  {
    }

    public void updateUserSubscriptionReminder(long id, String time)  {
    }

    public void unsubscribeUserPromises(long id)  {
    }

    public void unsubscribeUserReminder(long id)  {
    }
}
