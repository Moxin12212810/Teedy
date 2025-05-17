package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.Message;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Message DAO.
 * 
 * @author jtremeaux
 */
public class MessageDao {
    private static final Logger log = LoggerFactory.getLogger(MessageDao.class);

    /**
     * Creates a new message.
     * 
     * @param message Message
     * @return Message ID
     */
    public String create(Message message) {
        log.debug("Creating new message");

        // Generate ID
        message.setId(UUID.randomUUID().toString());
        log.debug("Generated message ID: {}", message.getId());

        // Set creation date
        message.setCreateDate(new Date());

        // Set initial status
        if (message.getStatus() == null) {
            message.setStatus("SENT");
        }

        // Save the message
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(message);
        log.info("Message saved successfully - ID: {}, From: {}, To: {}",
                message.getId(), message.getFromUser(), message.getToUser());

        return message.getId();
    }

    /**
     * Gets chat history.
     * 
     * @param username Username
     * @param withUser Other user
     * @param limit    Maximum number of messages
     * @return List of messages
     */
    @SuppressWarnings("unchecked")
    public List<Message> getMessages(String username, String withUser, int limit) {
        log.debug("Getting message history - User: {}, With: {}, Limit: {}", username, withUser, limit);

        EntityManager em = ThreadLocalContext.get().getEntityManager();

        // Build query with named parameters
        String jpql = "SELECT m FROM Message m WHERE " +
                "(m.fromUser = :username AND m.toUser = :withUser) OR " +
                "(m.fromUser = :withUser AND m.toUser = :username) " +
                "ORDER BY m.createDate DESC";

        TypedQuery<Message> query = em.createQuery(jpql, Message.class);
        query.setParameter("username", username);
        query.setParameter("withUser", withUser);
        query.setMaxResults(limit);

        List<Message> messages = query.getResultList();
        log.info("Found {} messages", messages.size());
        return messages;
    }

    /**
     * Updates message status.
     * 
     * @param id     Message ID
     * @param status New status
     */
    public void updateStatus(String id, String status) {
        log.debug("Updating message status - ID: {}, New status: {}", id, status);

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        Message message = em.find(Message.class, id);
        if (message != null) {
            message.setStatus(status);
            em.merge(message);
            log.info("Message status updated successfully - ID: {}", id);
        } else {
            log.warn("Message not found - ID: {}", id);
        }
    }
}