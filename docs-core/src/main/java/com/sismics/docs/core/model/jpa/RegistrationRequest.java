package com.sismics.docs.core.model.jpa;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Registration request entity.
 */
@Entity
@Table(name = "T_REGISTRATION_REQUEST")
public class RegistrationRequest {
    /**
     * Registration request ID.
     */
    @Id
    @Column(name = "RRE_ID_C", length = 36)
    private String id;

    /**
     * Email.
     */
    @Column(name = "RRE_EMAIL_C", length = 255, nullable = false)
    private String email;

    /**
     * Username.
     */
    @Column(name = "RRE_USERNAME_C", length = 50, nullable = false)
    private String username;

    /**
     * Password.
     */
    @Column(name = "RRE_PASSWORD_C", length = 255, nullable = false)
    private String password;

    /**
     * Status.
     */
    @Column(name = "RRE_STATUS_C", length = 20, nullable = false)
    private String status;

    /**
     * Creation date.
     */
    @Column(name = "RRE_CREATEDATE_D", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

    /**
     * Update date.
     */
    @Column(name = "RRE_UPDATEDATE_D", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateDate;

    /**
     * Reason for rejection.
     */
    @Column(name = "RRE_REASON_C", length = 1000)
    private String reason;

    /**
     * Processed by user ID.
     */
    @Column(name = "RRE_PROCESSEDBY_C", length = 36)
    private String processedBy;

    /**
     * Process date.
     */
    @Column(name = "RRE_PROCESSDATE_D")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processDate;

    /**
     * Comment.
     */
    @Column(name = "RRE_COMMENT_C", length = 1000)
    private String comment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }

    public Date getProcessDate() {
        return processDate;
    }

    public void setProcessDate(Date processDate) {
        this.processDate = processDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}