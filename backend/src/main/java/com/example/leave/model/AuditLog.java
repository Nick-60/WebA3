package com.example.leave.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity", nullable = false, length = 50)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "timestamp", nullable = false, insertable = false, updatable = false)
    private Instant timestamp;

    @Column(name = "details", columnDefinition = "JSON")
    private String details;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Instant getTimestamp() { return timestamp; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}

