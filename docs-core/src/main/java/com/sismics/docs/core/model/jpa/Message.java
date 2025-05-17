package com.sismics.docs.core.model.jpa;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Message entity.
 * 
 * @author jtremeaux
 */
@Entity
@Table(name = "T_MESSAGE")
public class Message {
    /**
     * Message ID.
     */
    @Id
    @Column(name = "MSG_ID_C", length = 36)
    private String id;

    /**
     * Sender username.
     */
    @Column(name = "MSG_IDFROM_C", nullable = false, length = 50)
    private String fromUser;

    /**
     * Recipient username.
     */
    @Column(name = "MSG_IDTO_C", nullable = false, length = 50)
    private String toUser;

    /**
     * Message content.
     */
    @Column(name = "MSG_CONTENT_C", nullable = false, length = 4000)
    private String content;

    /**
     * Message status.
     */
    @Column(name = "MSG_STATUS_C", nullable = false, length = 20)
    private String status;

    /**
     * Creation date.
     */
    @Column(name = "MSG_CREATEDATE_D", nullable = false)
    private Date createDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public String toString() {
        return String.format("Message[id=%s, from=%s, to=%s]", id, fromUser, toUser);
    }
}