package com.poornima.lab.model;

import java.sql.Timestamp;

public class AuditLog {
    private int logId;
    private Integer userId;
    private String username;
    private String action;
    private String entity;
    private Integer entityId;
    private String details;
    private Timestamp timestamp;

    public AuditLog() {}

    public AuditLog(int logId, Integer userId, String username, String action, 
                    String entity, Integer entityId, String details, Timestamp timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.username = username;
        this.action = action;
        this.entity = entity;
        this.entityId = entityId;
        this.details = details;
        this.timestamp = timestamp;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
