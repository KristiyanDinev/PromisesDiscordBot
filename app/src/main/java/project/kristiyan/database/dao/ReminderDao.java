package project.kristiyan.database.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import project.kristiyan.database.entities.ReminderEntity;
import project.kristiyan.database.entities.UserEntity;

import java.util.ArrayList;
import java.util.List;

public class ReminderDao {
    private final EntityManager em;
    private final int page_amount = 10;

    public ReminderDao(EntityManager em) {
        this.em = em;
    }

    public void close() {
        em.close();
    }

    // can be detached | id needed
    public void subscribe(UserEntity user, String time) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Check if user exists and handle accordingly
            UserEntity managedUser = handleUserEntity(user);

            // Check if reminder already exists for this user
            ReminderEntity existingReminder = findReminderByUserId(managedUser.id);

            if (existingReminder != null) {
                // Update existing reminder
                existingReminder.time = time;
                // No need to call merge/persist, it's already managed
            } else {
                // Create new reminder
                ReminderEntity reminderEntity = new ReminderEntity();
                reminderEntity.time = time;
                reminderEntity.userEntity = managedUser;
                em.persist(reminderEntity);
            }

            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }
    }

    // Helper method to handle user entity (detached vs existing)
    private UserEntity handleUserEntity(UserEntity user) {
        if (user.id == 0) {
            // New user without ID - persist it
            em.persist(user);
            return user;

        } else {
            // User has ID - check if it exists in database
            UserEntity existingUser = em.find(UserEntity.class, user.id);
            if (existingUser != null) {
                // User exists - return managed instance
                return existingUser;

            } else {
                // User doesn't exist but has ID - this is problematic with IDENTITY generation
                // Create a new user entity without the ID and let the database generate it
                UserEntity newUser = new UserEntity();
                newUser.name = user.name;
                // Don't set the ID - let the database generate it
                em.persist(newUser);
                return newUser;
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

            return deletedCount > 0;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return false;
    }

    public List<ReminderEntity> getUsers(int page) {
        // Read operations don't typically need transactions, but it's safer to use them
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<ReminderEntity> query = criteriaBuilder.createQuery(ReminderEntity.class);
            query.from(ReminderEntity.class);

            TypedQuery<ReminderEntity> typedQuery = em.createQuery(query);
            typedQuery.setFirstResult(page * page_amount);
            typedQuery.setMaxResults(page_amount);

            List<ReminderEntity> result = typedQuery.getResultList();
            tx.commit();

            return result;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return new ArrayList<>();
    }

    public boolean checkIfReminderEntityExistsByUserId(long user_id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<ReminderEntity> query = criteriaBuilder.createQuery(ReminderEntity.class);
            Root<ReminderEntity> root = query.from(ReminderEntity.class);

            // Fixed: Use correct path to user ID
            query.where(
                    criteriaBuilder.equal(root.get("userEntity").get("id"), user_id)
            );

            try {
                em.createQuery(query).getSingleResult();
                tx.commit();
                return true; // Found reminder for this user

            } catch (NoResultException e) {
                tx.commit();
                return false; // No reminder found for this user
            }
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return false;
    }
}