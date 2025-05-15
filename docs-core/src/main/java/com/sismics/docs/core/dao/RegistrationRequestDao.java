package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.RegistrationRequest;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Registration request DAO.
 */
public class RegistrationRequestDao {
    /**
     * Creates a new registration request.
     *
     * @param request Registration request to create
     * @return New registration request ID
     */
    public String create(RegistrationRequest request) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        
        // Check if username already exists
        Query q = em.createQuery("select u from User u where u.username = :username and u.deleteDate is null");
        q.setParameter("username", request.getUsername());
        if (!q.getResultList().isEmpty()) {
            throw new RuntimeException("AlreadyExistingUsername");
        }
        
        // Check if email already exists
        q = em.createQuery("select u from User u where u.email = :email and u.deleteDate is null");
        q.setParameter("email", request.getEmail());
        if (!q.getResultList().isEmpty()) {
            throw new RuntimeException("AlreadyExistingEmail");
        }
        
        // Check if there's already a pending request for this username
        q = em.createQuery("select r from RegistrationRequest r where r.username = :username and r.status = 'PENDING'");
        q.setParameter("username", request.getUsername());
        if (!q.getResultList().isEmpty()) {
            throw new RuntimeException("AlreadyExistingRequest");
        }
        
        // Create the request
        request.setId(UUID.randomUUID().toString());
        request.setCreateDate(new Date());
        request.setStatus("PENDING");
        em.persist(request);
        
        return request.getId();
    }
    
    /**
     * Gets a registration request by ID.
     *
     * @param id Registration request ID
     * @return Registration request
     */
    public RegistrationRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(RegistrationRequest.class, id);
    }
    
    /**
     * Gets all pending registration requests.
     *
     * @return List of pending registration requests
     */
    @SuppressWarnings("unchecked")
    public List<RegistrationRequest> getPendingRequests() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Query q = em.createQuery("select r from RegistrationRequest r where r.status = 'PENDING' order by r.createDate");
        return q.getResultList();
    }
    
    /**
     * Updates a registration request.
     *
     * @param request Registration request to update
     */
    public void update(RegistrationRequest request) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.merge(request);
    }
}