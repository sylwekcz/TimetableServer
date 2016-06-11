package pl.sylwekczmil.timetableserver.controller;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.transaction.UserTransaction;
import pl.sylwekczmil.timetableserver.model.Event;
import pl.sylwekczmil.timetableserver.model.Timetable;
import pl.sylwekczmil.timetableserver.controller.exceptions.NonexistentEntityException;
import pl.sylwekczmil.timetableserver.controller.exceptions.RollbackFailureException;


public class EventJpaController implements Serializable {

    public EventJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Event event) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Timetable idTimetable = event.getIdTimetable();
            if (idTimetable != null) {
                idTimetable = em.getReference(idTimetable.getClass(), idTimetable.getIdTimetable());
                event.setIdTimetable(idTimetable);
            }
            em.persist(event);
            if (idTimetable != null) {
                idTimetable.getEventCollection().add(event);
                idTimetable = em.merge(idTimetable);
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

    public void edit(Event event) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Event persistentEvent = em.find(Event.class, event.getIdEvent());
            Timetable idTimetableOld = persistentEvent.getIdTimetable();
            Timetable idTimetableNew = event.getIdTimetable();
            if (idTimetableNew != null) {
                idTimetableNew = em.getReference(idTimetableNew.getClass(), idTimetableNew.getIdTimetable());
                event.setIdTimetable(idTimetableNew);
            }
            event = em.merge(event);
            if (idTimetableOld != null && !idTimetableOld.equals(idTimetableNew)) {
                idTimetableOld.getEventCollection().remove(event);
                idTimetableOld = em.merge(idTimetableOld);
            }
            if (idTimetableNew != null && !idTimetableNew.equals(idTimetableOld)) {
                idTimetableNew.getEventCollection().add(event);
                idTimetableNew = em.merge(idTimetableNew);
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
                Integer id = event.getIdEvent();
                if (findEvent(id) == null) {
                    throw new NonexistentEntityException("The event with id " + id + " no longer exists.");
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
            Event event;
            try {
                event = em.getReference(Event.class, id);
                event.getIdEvent();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The event with id " + id + " no longer exists.", enfe);
            }
            Timetable idTimetable = event.getIdTimetable();
            if (idTimetable != null) {
                idTimetable.getEventCollection().remove(event);
                idTimetable = em.merge(idTimetable);
            }
            em.remove(event);
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

    public List<Event> findEventEntities() {
        return findEventEntities(true, -1, -1);
    }

    public List<Event> findEventEntities(int maxResults, int firstResult) {
        return findEventEntities(false, maxResults, firstResult);
    }

    private List<Event> findEventEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Event as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Event findEvent(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Event.class, id);
        } finally {
            em.close();
        }
    }

    public int getEventCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Event as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
