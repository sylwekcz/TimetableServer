
package pl.sylwekczmil.timetableserver.controller;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import pl.sylwekczmil.timetableserver.Timetable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import pl.sylwekczmil.timetableserver.User;
import pl.sylwekczmil.timetableserver.controller.exceptions.NonexistentEntityException;
import pl.sylwekczmil.timetableserver.controller.exceptions.RollbackFailureException;


public class UserJpaController implements Serializable {

    public UserJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(User user) throws RollbackFailureException, Exception {
        if (user.getTimetableCollection() == null) {
            user.setTimetableCollection(new ArrayList<Timetable>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<Timetable> attachedTimetableCollection = new ArrayList<Timetable>();
            for (Timetable timetableCollectionTimetableToAttach : user.getTimetableCollection()) {
                timetableCollectionTimetableToAttach = em.getReference(timetableCollectionTimetableToAttach.getClass(), timetableCollectionTimetableToAttach.getIdTimetable());
                attachedTimetableCollection.add(timetableCollectionTimetableToAttach);
            }
            user.setTimetableCollection(attachedTimetableCollection);
            em.persist(user);
            for (Timetable timetableCollectionTimetable : user.getTimetableCollection()) {
                timetableCollectionTimetable.getUserCollection().add(user);
                timetableCollectionTimetable = em.merge(timetableCollectionTimetable);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(User user) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            User persistentUser = em.find(User.class, user.getIdUser());
            Collection<Timetable> timetableCollectionOld = persistentUser.getTimetableCollection();
            Collection<Timetable> timetableCollectionNew = user.getTimetableCollection();
            Collection<Timetable> attachedTimetableCollectionNew = new ArrayList<Timetable>();
            for (Timetable timetableCollectionNewTimetableToAttach : timetableCollectionNew) {
                timetableCollectionNewTimetableToAttach = em.getReference(timetableCollectionNewTimetableToAttach.getClass(), timetableCollectionNewTimetableToAttach.getIdTimetable());
                attachedTimetableCollectionNew.add(timetableCollectionNewTimetableToAttach);
            }
            timetableCollectionNew = attachedTimetableCollectionNew;
            user.setTimetableCollection(timetableCollectionNew);
            user = em.merge(user);
            for (Timetable timetableCollectionOldTimetable : timetableCollectionOld) {
                if (!timetableCollectionNew.contains(timetableCollectionOldTimetable)) {
                    timetableCollectionOldTimetable.getUserCollection().remove(user);
                    timetableCollectionOldTimetable = em.merge(timetableCollectionOldTimetable);
                }
            }
            for (Timetable timetableCollectionNewTimetable : timetableCollectionNew) {
                if (!timetableCollectionOld.contains(timetableCollectionNewTimetable)) {
                    timetableCollectionNewTimetable.getUserCollection().add(user);
                    timetableCollectionNewTimetable = em.merge(timetableCollectionNewTimetable);
                }
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = user.getIdUser();
                if (findUser(id) == null) {
                    throw new NonexistentEntityException("The user with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            User user;
            try {
                user = em.getReference(User.class, id);
                user.getIdUser();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The user with id " + id + " no longer exists.", enfe);
            }
            Collection<Timetable> timetableCollection = user.getTimetableCollection();
            for (Timetable timetableCollectionTimetable : timetableCollection) {
                timetableCollectionTimetable.getUserCollection().remove(user);
                timetableCollectionTimetable = em.merge(timetableCollectionTimetable);
            }
            em.remove(user);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<User> findUserEntities() {
        return findUserEntities(true, -1, -1);
    }

    public List<User> findUserEntities(int maxResults, int firstResult) {
        return findUserEntities(false, maxResults, firstResult);
    }

    private List<User> findUserEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from User as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public User findUser(Integer id){
        EntityManager em = getEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }
    
     public User findUserByUsername(String username) throws NonexistentEntityException {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<User> q = em.createNamedQuery("User.findByUsername", User.class);
             q.setParameter("username", username);           
            return q.getSingleResult();
        } finally {
            em.close();
        }
    }

    public int getUserCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from User as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    public List<Timetable> findTimetables(Integer userid) {
        EntityManager em = getEntityManager();
        try {
             TypedQuery<Timetable> q = em.createNamedQuery("User.findTimetables", Timetable.class);
             q.setParameter("idUser", userid);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
