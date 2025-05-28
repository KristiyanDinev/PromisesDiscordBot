package project.kristiyan.database.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import project.kristiyan.database.entities.PromiseEntity;
import project.kristiyan.database.entities.UserEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PromiseDao {
    private EntityManager em;
    private final int page_amount = 10;

    public PromiseDao(EntityManager em) {
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

            // Check if promise already exists for this user
            PromiseEntity existingPromise = findPromiseByUserId(managedUser.id);

            if (existingPromise != null) {
                // Update existing promise
                existingPromise.time = time;
                // No need to call merge/persist, it's already managed
            } else {
                // Create new promise
                PromiseEntity promiseEntity = new PromiseEntity();
                promiseEntity.time = time;
                promiseEntity.userEntity = managedUser;
                em.persist(promiseEntity);
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

    // Helper method to find promise by user ID
    @Nullable
    private PromiseEntity findPromiseByUserId(long userId) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<PromiseEntity> query = cb.createQuery(PromiseEntity.class);
            Root<PromiseEntity> root = query.from(PromiseEntity.class);

            query.where(cb.equal(root.get("userEntity").get("id"), userId));

            return em.createQuery(query).getSingleResult();

        } catch (NoResultException e) {
            return null; // No promise found for this user
        }
    }

    // id needed
    public boolean unsubscribe(long user_id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaDelete<PromiseEntity> delete = criteriaBuilder.createCriteriaDelete(PromiseEntity.class);
            Root<PromiseEntity> root = delete.from(PromiseEntity.class);

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

    public List<PromiseEntity> getUsers(int page) {
        // Read operations don't typically need transactions, but it's safer to use them
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<PromiseEntity> query = criteriaBuilder.createQuery(PromiseEntity.class);
            query.from(PromiseEntity.class);

            TypedQuery<PromiseEntity> typedQuery = em.createQuery(query);
            typedQuery.setFirstResult(page * page_amount);
            typedQuery.setMaxResults(page_amount);

            List<PromiseEntity> result = typedQuery.getResultList();
            tx.commit();

            return result;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return new ArrayList<>();
    }

    public boolean checkIfPromiseEntityExistsByUserId(long user_id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<PromiseEntity> query = criteriaBuilder.createQuery(PromiseEntity.class);
            Root<PromiseEntity> root = query.from(PromiseEntity.class);

            // Fixed: Use correct path to user ID
            query.where(
                    criteriaBuilder.equal(root.get("userEntity").get("id"), user_id)
            );

            try {
                em.createQuery(query).getSingleResult();
                tx.commit();
                return true; // Found promise for this user
            } catch (NoResultException e) {
                tx.commit();
                return false; // No promise found for this user
            }
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Failed to check if promise exists for user", e);
        }
    }
}