package project.kristiyan.database.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import project.kristiyan.database.entities.AdminEntity;

import java.util.List;

public class AdminDao {
    private EntityManager em;
    private final String insertAdminSQL = "INSERT INTO admins (name, user_id) VALUES (:name, :user_id);";

    public AdminDao(EntityManager em) {
        this.em = em;
    }

    public void close() {
        em.close();
    }

    public boolean addAdmin(String name, long user_id) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Query query = em.createNativeQuery(insertAdminSQL);
            query = query.setParameter("name", name);
            query = query.setParameter("user_id", user_id);

            int saved = query.executeUpdate();
            tx.commit();
            em.clear();

            return saved > 0;

        } catch (Exception ignored) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return false;
        }
    }

    public void removeAdmin(long user_id) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

        CriteriaDelete<AdminEntity> delete = criteriaBuilder.createCriteriaDelete(AdminEntity.class);
        Root<AdminEntity> root = delete.from(AdminEntity.class);
        delete = delete.where(
                criteriaBuilder.equal(root.get("user_id"), user_id)
        );

        em.createQuery(delete).executeUpdate();
    }

    public List<AdminEntity> getAdmins() {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<AdminEntity> select = criteriaBuilder.createQuery(AdminEntity.class);
        select.from(AdminEntity.class);
        return em.createQuery(select).getResultList();
    }

    public boolean isAdmin(long user_id) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<AdminEntity> select = criteriaBuilder.createQuery(AdminEntity.class);

        Root<AdminEntity> root = select.from(AdminEntity.class);
        select = select.where(
                criteriaBuilder.equal(root.get("user_id"), user_id)
        );

        try {
            em.createQuery(select).getSingleResult();
            return true;

        } catch (Exception ignore) {
            return false;
        }
    }
}
