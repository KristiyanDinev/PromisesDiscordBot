package project.kristiyan.database.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import project.kristiyan.App;
import project.kristiyan.database.entities.PromiseEntity;
import project.kristiyan.database.entities.UserEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PromiseDao {
    private EntityManager em;
    private final int page_amount = 10;
    private final String insertPromiseSQL = "INSERT INTO promises_service (time, user_id) VALUES (:time, :user_id);";

    public PromiseDao(EntityManager em) {
        this.em = em;
    }

    public void close() {
        em.close();
    }

    private boolean savePromise_NoTransaction(PromiseEntity promiseEntity) {
        try {
            Query query = em.createNativeQuery(insertPromiseSQL);
            query = query.setParameter("time", promiseEntity.time);
            query = query.setParameter("user_id", promiseEntity.userEntity.id);

            int saved = query.executeUpdate();
            em.clear();

            return saved > 0;

        } catch (Exception ignore) {
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

            // Check if promise already exists for this user
            PromiseEntity existingPromise = findPromiseByUserId(user.id);

            if (existingPromise != null) {
                // Update existing promise
                existingPromise.time = time;
                // No need to call merge/persist, it's already managed

            } else {
                // Create new promise
                PromiseEntity promiseEntity = new PromiseEntity();
                promiseEntity.time = time;
                promiseEntity.userEntity = user;

                if (!savePromise_NoTransaction(promiseEntity)) {
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

    // Helper method to find promise by user ID
    @Nullable
    private PromiseEntity findPromiseByUserId(long userId) {
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<PromiseEntity> query = cb.createQuery(PromiseEntity.class);
            Root<PromiseEntity> root = query.from(PromiseEntity.class);

            query.where(cb.equal(root.get("userEntity").get("id"), userId));

            return em.createQuery(query).getSingleResult();

        } catch (Exception ignore) {
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

            em.clear();

            return deletedCount >= 0;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return false;
    }

    public List<PromiseEntity> getUsers(int page) {
        page -= 1;

        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<PromiseEntity> query = criteriaBuilder.createQuery(PromiseEntity.class);
            query.from(PromiseEntity.class);

            TypedQuery<PromiseEntity> typedQuery = em.createQuery(query);
            typedQuery.setFirstResult(page * page_amount);
            typedQuery.setMaxResults(page_amount);

            return typedQuery.getResultList();

        } catch (Exception ignore) {
        }

        return new ArrayList<>();
    }

    public boolean checkIfPromiseEntityExistsByUserId(long user_id) {
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<PromiseEntity> query = criteriaBuilder.createQuery(PromiseEntity.class);
            Root<PromiseEntity> root = query.from(PromiseEntity.class);

            query.where(
                    criteriaBuilder.equal(root.get("userEntity").get("id"), user_id)
            );

            em.createQuery(query).getSingleResult();
            return true; // Found promise for this user

        } catch (Exception ignore) {
                return false;
        }
    }
}