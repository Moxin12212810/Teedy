package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.Message;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessageDao {

    /**
     * 创建新消息
     */
    public String create(Message message) {
        // 生成ID
        message.setId(UUID.randomUUID().toString());

        // 设置创建时间
        message.setCreateDate(new Date());

        // 设置初始状态
        if (message.getStatus() == null) {
            message.setStatus("SENT");
        }

        // 保存消息
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(message);

        return message.getId();
    }

    /**
     * 获取聊天历史记录
     */
    @SuppressWarnings("unchecked")
    public List<Message> getMessages(String username, String withUser, int limit) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        StringBuilder sb = new StringBuilder("select m from Message m where ");
        sb.append("(m.fromUser = :username and m.toUser = :withUser) ");
        sb.append("or (m.fromUser = :withUser and m.toUser = :username) ");
        sb.append("order by m.createDate desc");

        Query q = em.createQuery(sb.toString());
        q.setParameter("username", username);
        q.setParameter("withUser", withUser);
        q.setMaxResults(limit);

        return q.getResultList();
    }

    /**
     * 更新消息状态
     */
    public void updateStatus(String id, String status) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();

        Query q = em.createQuery("update Message m set m.status = :status where m.id = :id");
        q.setParameter("status", status);
        q.setParameter("id", id);
        q.executeUpdate();
    }
}