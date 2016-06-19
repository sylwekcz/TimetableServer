package pl.sylwekczmil.timetableserver.controller;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import pl.sylwekczmil.timetableserver.model.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.UserTransaction;
import pl.sylwekczmil.timetableserver.model.Event;
import pl.sylwekczmil.timetableserver.model.Timetable;
import pl.sylwekczmil.timetableserver.controller.exceptions.IllegalOrphanException;
import pl.sylwekczmil.timetableserver.controller.exceptions.NonexistentEntityException;
import pl.sylwekczmil.timetableserver.controller.exceptions.RollbackFailureException;


public class TimetableJpaController implements Serializable {

    public TimetableJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Timetable timetable) throws RollbackFailureException, Exception {
        if (timetable.getUserCollection() == null) {
            timetable.setUserCollection(new ArrayList<User>());
        }
        if (timetable.getEventCollection() == null) {
            timetable.setEventCollection(new ArrayList<Event>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Collection<User> attachedUserCollection = new ArrayList<User>();
            for (User userCollectionUserToAttach : timetable.getUserCollection()) {
                userCollectionUserToAttach = em.getReference(userCollectionUserToAttach.getClass(), userCollectionUserToAttach.getIdUser());
                attachedUserCollection.add(userCollectionUserToAttach);
            }
            timetable.setUserCollection(attachedUserCollection);
            Collection<Event> attachedEventCollection = new ArrayList<Event>();
            for (Event eventCollectionEventToAttach : timetable.getEventCollection()) {
                eventCollectionEventToAttach = em.getReference(eventCollectionEventToAttach.getClass(), eventCollectionEventToAttach.getIdEvent());
                attachedEventCollection.add(eventCollectionEventToAttach);
            }
            timetable.setEventCollection(attachedEventCollection);
            em.persist(timetable);
            for (User userCollectionUser : timetable.getUserCollection()) {
                userCollectionUser.getTimetableCollection().add(timetable);
                userCollectionUser = em.merge(userCollectionUser);
            }
            for (Event eventCollectionEvent : timetable.getEventCollection()) {
                Timetable oldIdTimetableOfEventCollectionEvent = eventCollectionEvent.getIdTimetable();
                eventCollectionEvent.setIdTimetable(timetable);
                eventCollectionEvent = em.merge(eventCollectionEvent);
                if (oldIdTimetableOfEventCollectionEvent != null) {
                    oldIdTimetableOfEventCollectionEvent.getEventCollection().remove(eventCollectionEvent);
                    oldIdTimetableOfEventCollectionEvent = em.merge(oldIdTimetableOfEventCollectionEvent);
                }
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

    public void edit(Timetable timetable) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Timetable persistentTimetable = em.find(Timetable.class, timetable.getIdTimetable());
            Collection<User> userCollectionOld = persistentTimetable.getUserCollection();
            Collection<User> userCollectionNew = timetable.getUserCollection();
            Collection<Event> eventCollectionOld = persistentTimetable.getEventCollection();
            Collection<Event> eventCollectionNew = timetable.getEventCollection();
            List<String> illegalOrphanMessages = null;
            for (Event eventCollectionOldEvent : eventCollectionOld) {
                if (!eventCollectionNew.contains(eventCollectionOldEvent)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Event " + eventCollectionOldEvent + " since its idTimetable field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<User> attachedUserCollectionNew = new ArrayList<User>();
            for (User userCollectionNewUserToAttach : userCollectionNew) {
                userCollectionNewUserToAttach = em.getReference(userCollectionNewUserToAttach.getClass(), userCollectionNewUserToAttach.getIdUser());
                attachedUserCollectionNew.add(userCollectionNewUserToAttach);
            }
            userCollectionNew = attachedUserCollectionNew;
            timetable.setUserCollection(userCollectionNew);
            Collection<Event> attachedEventCollectionNew = new ArrayList<Event>();
            for (Event eventCollectionNewEventToAttach : eventCollectionNew) {
                eventCollectionNewEventToAttach = em.getReference(eventCollectionNewEventToAttach.getClass(), eventCollectionNewEventToAttach.getIdEvent());
                attachedEventCollectionNew.add(eventCollectionNewEventToAttach);
            }
            eventCollectionNew = attachedEventCollectionNew;
            timetable.setEventCollection(eventCollectionNew);
            timetable = em.merge(timetable);
            for (User userCollectionOldUser : userCollectionOld) {
                if (!userCollectionNew.contains(userCollectionOldUser)) {
                    userCollectionOldUser.getTimetableCollection().remove(timetable);
                    userCollectionOldUser = em.merge(userCollectionOldUser);
                }
            }
            for (User userCollectionNewUser : userCollectionNew) {
                if (!userCollectionOld.contains(userCollectionNewUser)) {
                    userCollectionNewUser.getTimetableCollection().add(timetable);
                    userCollectionNewUser = em.merge(userCollectionNewUser);
                }
            }
            for (Event eventCollectionNewEvent : eventCollectionNew) {
                if (!eventCollectionOld.contains(eventCollectionNewEvent)) {
                    Timetable oldIdTimetableOfEventCollectionNewEvent = eventCollectionNewEvent.getIdTimetable();
                    eventCollectionNewEvent.setIdTimetable(timetable);
                    eventCollectionNewEvent = em.merge(eventCollectionNewEvent);
                    if (oldIdTimetableOfEventCollectionNewEvent != null && !oldIdTimetableOfEventCollectionNewEvent.equals(timetable)) {
                        oldIdTimetableOfEventCollectionNewEvent.getEventCollection().remove(eventCollectionNewEvent);
                        oldIdTimetableOfEventCollectionNewEvent = em.merge(oldIdTimetableOfEventCollectionNewEvent);
                    }
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
                Integer id = timetable.getIdTimetable();
                if (findTimetable(id) == null) {
                    throw new NonexistentEntityException("The timetable with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Timetable timetable;
            try {
                timetable = em.getReference(Timetable.class, id);
                timetable.getIdTimetable();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The timetable with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            Collection<Event> eventCollectionOrphanCheck = timetable.getEventCollection();
            for (Event eventCollectionOrphanCheckEvent : eventCollectionOrphanCheck) {
               em.remove(eventCollectionOrphanCheckEvent);
            }
            Collection<User> userCollection = timetable.getUserCollection();
            for (User userCollectionUser : userCollection) {
                userCollectionUser.getTimetableCollection().remove(timetable);
                userCollectionUser = em.merge(userCollectionUser);
            }
            em.remove(timetable);
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

    public List<Timetable> findTimetableEntities() {
        return findTimetableEntities(true, -1, -1);
    }

    public List<Timetable> findTimetableEntities(int maxResults, int firstResult) {
        return findTimetableEntities(false, maxResults, firstResult);
    }

    private List<Timetable> findTimetableEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select object(o) from Timetable as o");
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Timetable findTimetable(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Timetable.class, id);
        } finally {
            em.close();
        }
    }

    public int getTimetableCount() {
        EntityManager em = getEntityManager();
        try {
            Query q = em.createQuery("select count(o) from Timetable as o");
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
      public List<Event> findEvents(Integer timetableId) {
        EntityManager em = getEntityManager();
        try {
             TypedQuery<Event> q = em.createNamedQuery("Timetable.findEvents", Event.class);
             q.setParameter("idTimetable", timetableId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
    
}
