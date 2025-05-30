package project.kristiyan.database.dao;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import project.kristiyan.App;
import project.kristiyan.database.entities.ReminderEntity;
import project.kristiyan.database.entities.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class ReminderDao {
    private final EntityManager em;
    private final int page_amount = 10;
    private final String insertReminderSQL = "INSERT INTO reminder_service (time, user_id) VALUES (:time, :user_id);";

    public ReminderDao(EntityManager em) {
        this.em = em;
    }

    public void close() {
        em.close();
    }

    private boolean saveReminder_NoTransaction(ReminderEntity reminderEntity) {
        try {
            Query query = em.createNativeQuery(insertReminderSQL);
            query = query.setParameter("time", reminderEntity.time);
            query = query.setParameter("user_id", reminderEntity.userEntity.id);

            int saved = query.executeUpdate();
            em.clear();

            return saved > 0;

        } catch (Exception ignored) {
            return false;
        }
    }

    // can be detached | id needed
    public void subscribe(UserEntity user, String time) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            UserEntity userEntity = App.userDao.getUserById(user.id);
            if (userEntity == null && App.userDao.saveUser_not(user)) {
                throw new Exception();
            }

            // Check if reminder already exists for this user
            ReminderEntity existingReminder = findReminderByUserId(user.id);

            if (existingReminder != null) {
                // Update existing reminder
                existingReminder.time = time;
                // No need to call merge/persist, it's already managed

            } else {
                // Create new reminder
                ReminderEntity reminderEntity = new ReminderEntity();
                reminderEntity.time = time;
                reminderEntity.userEntity = user;

                if (!saveReminder_NoTransaction(reminderEntity)) {
                    throw new Exception();
                }
            }

            tx.commit();
            em.clear();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
    }

    // Helper method to find reminder by user ID
    private ReminderEntity findReminderByUserId(long userId) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<ReminderEntity> query = cb.createQuery(ReminderEntity.class);
            Root<ReminderEntity> root = query.from(ReminderEntity.class);

            query.where(cb.equal(root.get("userEntity").get("id"), userId));

            return em.createQuery(query).getSingleResult();

        } catch (NoResultException e) {
            return null; // No reminder found for this user
        }
    }

    // id needed
    public boolean unsubscribe(long user_id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaDelete<ReminderEntity> delete = criteriaBuilder.createCriteriaDelete(ReminderEntity.class);
            Root<ReminderEntity> root = delete.from(ReminderEntity.class);

            // Fixed: Use correct path to user ID
            delete.where(
                    criteriaBuilder.equal(root.get("userEntity").get("id"), user_id)
            );

            int deletedCount = em.createQuery(delete).executeUpdate();
            tx.commit();
            em.clear();

            return deletedCount > 0;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return false;
    }

    public List<ReminderEntity> getUsers(int page) {
        // If user gives page 1. Convert it to 0, so the algorithm may work.
        page -= 1;

        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<ReminderEntity> query = criteriaBuilder.createQuery(ReminderEntity.class);
            query.from(ReminderEntity.class);

            TypedQuery<ReminderEntity> typedQuery = em.createQuery(query);
            typedQuery.setFirstResult(page * page_amount);
            typedQuery.setMaxResults(page_amount);

            return typedQuery.getResultList();

        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public boolean checkIfReminderEntityExistsByUserId(long user_id) {
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<ReminderEntity> query = criteriaBuilder.createQuery(ReminderEntity.class);
            Root<ReminderEntity> root = query.from(ReminderEntity.class);

            query.where(
                    criteriaBuilder.equal(root.get("userEntity").get("id"), user_id)
            );

           em.createQuery(query).getSingleResult();
            return true; // Found reminder for this user

        } catch (Exception ignored) {
            return false;
        }
    }
}