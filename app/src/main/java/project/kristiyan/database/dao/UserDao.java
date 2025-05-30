package project.kristiyan.database.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import project.kristiyan.database.entities.UserEntity;

import javax.annotation.Nullable;

public class UserDao {
    private EntityManager em;
    private final String insertUserSQL = "INSERT INTO users (id, name) VALUES (:id, :name);";

    public UserDao(EntityManager em) {
        this.em = em;
    }

    public void close() {
        em.close();
    }

    @Nullable
    public UserEntity getUserById(long id) {
        try {
            CriteriaBuilder builder = em.getCriteriaBuilder();
            CriteriaQuery<UserEntity> criteriaQuery = builder.createQuery(UserEntity.class);
            Root<UserEntity> root = criteriaQuery.from(UserEntity.class);

            criteriaQuery.where(
                    builder.equal(root.get("id"), id)
            );

            return em.createQuery(criteriaQuery).getSingleResult();

        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean saveUser_not(UserEntity userEntity) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Query query = em.createNativeQuery(insertUserSQL);
            query = query.setParameter("name", userEntity.name);
            query = query.setParameter("id", userEntity.id);

            int saved = query.executeUpdate();
            tx.commit();
            em.clear();

            return saved <= 0;

        } catch (Exception ignored) {
            if (tx.isActive()) {
                tx.rollback();
            }
        }

        return true;
    }
}
