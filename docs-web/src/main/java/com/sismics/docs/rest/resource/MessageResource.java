package com.sismics.docs.rest.resource;

import com.sismics.docs.core.model.jpa.Message;
import com.sismics.docs.core.dao.MessageDao;
import com.sismics.util.context.ThreadLocalContext;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Path("/message")
public class MessageResource {
    private static final Logger log = LoggerFactory.getLogger(MessageResource.class);

    /**
     * 获取聊天历史记录
     */
    @GET
    @Path("{username}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response getMessages(@PathParam("username") String username,
            @QueryParam("with") String withUser,
            @QueryParam("limit") @DefaultValue("50") int limit) {

        log.info("正在获取消息历史记录 - 用户: {}, 与用户: {}, 限制: {}", username, withUser, limit);

        // 开始数据库事务
        MessageDao messageDao = new MessageDao();
        List<Message> messages = messageDao.getMessages(username, withUser, limit);

        // log.info("查询到 {} 条消息记录", messages.size());

        JsonArrayBuilder messagesArray = Json.createArrayBuilder();
        for (Message message : messages) {
            // log.debug("处理消息 - ID: {}, From: {}, To: {}, Content: {}",
            //         message.getId(), message.getFromUser(), message.getToUser(), message.getContent());

            JsonObjectBuilder messageObject = Json.createObjectBuilder()
                    .add("id", message.getId())
                    .add("from", message.getFromUser().equals(username) ? "me" : message.getFromUser())
                    .add("to", message.getToUser())
                    .add("content", message.getContent())
                    .add("timestamp", message.getCreateDate().getTime())
                    .add("status", message.getStatus());
            messagesArray.add(messageObject);
        }

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("messages", messagesArray);
        return Response.ok().entity(response.build()).build();
    }

    /**
     * 保存新消息
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createMessage(Message message) {
        log.info("正在创建新消息 - From: {}, To: {}, Content: {}",
                message.getFromUser(), message.getToUser(), message.getContent());

        MessageDao messageDao = new MessageDao();
        String id = messageDao.create(message);

        log.info("消息创建成功 - ID: {}", id);

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("id", id)
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }

    /**
     * 更新消息状态（例如：已读）
     */
    @POST
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response updateMessageStatus(@PathParam("id") String id,
            @FormParam("status") String status) {
        log.info("正在更新消息状态 - ID: {}, 新状态: {}", id, status);

        MessageDao messageDao = new MessageDao();
        messageDao.updateStatus(id, status);

        log.info("消息状态更新成功");

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}