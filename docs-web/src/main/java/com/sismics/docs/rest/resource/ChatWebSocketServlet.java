package com.sismics.docs.rest.resource;

import com.sismics.docs.core.model.jpa.Message;
import com.sismics.docs.core.dao.MessageDao;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.transaction.Transactional;
import com.sismics.util.jpa.EMF;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.UUID;

@ServerEndpoint("/ws/chat/{username}")
public class ChatWebSocketServlet {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketServlet.class);
    private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private static final long PING_INTERVAL = 30000; // 30秒发送一次心跳

    /**
     * Returns a list of online users.
     *
     * @return List of usernames
     */
    public static Set<String> getOnlineUsers() {
        return sessions.keySet();
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessions.put(username, session);
        log.info("New WebSocket connection: {}", username);

        // 设置WebSocket配置
        session.setMaxIdleTimeout(120000); // 2分钟超时

        // 启动心跳
        startHeartbeat(session);
    }

    @OnMessage
    public void onMessage(String message, Session session, @PathParam("username") String fromUsername) {
        EntityManager em = null;
        EntityTransaction tx = null;

        try {
            if ("pong".equals(message)) {
                return;
            }

            log.info("收到来自 {} 的消息: {}", fromUsername, message);

            // 初始化 EntityManager 和事务
            em = EMF.get().createEntityManager();
            ThreadLocalContext context = ThreadLocalContext.get();
            context.setEntityManager(em);
            tx = em.getTransaction();
            tx.begin();

            try {
                JsonReader jsonReader = Json.createReader(new StringReader(message));
                JsonObject jsonMessage = jsonReader.readObject();
                jsonReader.close();

                // 添加空值检查
                if (!jsonMessage.containsKey("to") || !jsonMessage.containsKey("content")) {
                    log.error("消息格式错误: 缺少必需字段");
                    return;
                }

                String toUsername = jsonMessage.getString("to");
                String content = jsonMessage.getString("content");
                String messageId = jsonMessage.containsKey("id") ? jsonMessage.getString("id")
                        : UUID.randomUUID().toString();
                long timestamp = jsonMessage.containsKey("timestamp")
                        ? jsonMessage.getJsonNumber("timestamp").longValue()
                        : System.currentTimeMillis();

                // 保存消息到数据库
                Message dbMessage = new Message();
                dbMessage.setId(messageId);
                dbMessage.setFromUser(fromUsername);
                dbMessage.setToUser(toUsername);
                dbMessage.setContent(content);
                dbMessage.setStatus("SENT");

                MessageDao messageDao = new MessageDao();
                String savedId = messageDao.create(dbMessage);
                log.info("消息已保存到数据库 - ID: {}", savedId);

                // 提交事务
                tx.commit();

                // 构建发送的消息
                JsonObject outMessage = Json.createObjectBuilder()
                        .add("id", messageId)
                        .add("from", fromUsername)
                        .add("to", toUsername)
                        .add("content", content)
                        .add("timestamp", timestamp)
                        .add("status", "RECEIVED")
                        .build();

                // 发送消息给目标用户
                Session toSession = sessions.get(toUsername);
                if (toSession != null && toSession.isOpen()) {
                    log.info("发送消息给 {}", toUsername);
                    toSession.getBasicRemote().sendText(outMessage.toString());
                } else {
                    log.warn("目标用户 {} 不在线", toUsername);
                }
            } catch (Exception e) {
                log.error("处理JSON消息时出错", e);
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                // 发送错误消息回客户端
                JsonObject errorMessage = Json.createObjectBuilder()
                        .add("error", "消息处理失败")
                        .add("message", e.getMessage())
                        .build();
                session.getBasicRemote().sendText(errorMessage.toString());
            }
        } catch (Exception e) {
            log.error("消息处理过程中出错", e);
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        } finally {
            if (em != null) {
                ThreadLocalContext.cleanup();
                em.close();
            }
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessions.remove(username);
        log.info("WebSocket connection closed: {}", username);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket error", error);
    }

    private void startHeartbeat(Session session) {
        new Thread(() -> {
            while (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText("ping");
                    Thread.sleep(PING_INTERVAL);
                } catch (Exception e) {
                    log.error("心跳发送失败", e);
                    break;
                }
            }
        }).start();
    }
}