package com.sismics.docs.rest.resource;

import com.sismics.docs.core.model.jpa.Message;
import com.sismics.docs.core.dao.MessageDao;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/message")
public class MessageResource {

    /**
     * 获取聊天历史记录
     */
    @GET
    @Path("{username}")
    public Response getMessages(@PathParam("username") String username,
            @QueryParam("with") String withUser,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        MessageDao messageDao = new MessageDao();
        List<Message> messages = messageDao.getMessages(username, withUser, limit);

        JsonArrayBuilder messagesArray = Json.createArrayBuilder();
        for (Message message : messages) {
            JsonObjectBuilder messageObject = Json.createObjectBuilder()
                    .add("id", message.getId())
                    .add("from", message.getFromUser())
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
    public Response createMessage(Message message) {
        MessageDao messageDao = new MessageDao();
        String id = messageDao.create(message);

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
    public Response updateMessageStatus(@PathParam("id") String id,
            @FormParam("status") String status) {
        MessageDao messageDao = new MessageDao();
        messageDao.updateStatus(id, status);

        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("status", "ok");
        return Response.ok().entity(response.build()).build();
    }
}