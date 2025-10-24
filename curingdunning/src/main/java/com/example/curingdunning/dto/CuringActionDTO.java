package com.example.curingdunning.dto;

import java.time.LocalDateTime;

import com.example.curingdunning.entity.ActionStatus;
import com.example.curingdunning.entity.ActionType;

public class CuringActionDTO {

    private Long actionId;
    private Long eventId;
    private Long customerId;
    private String serviceName;

    private ActionType actionType;
    private ActionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    public CuringActionDTO() {}

    // --- Add this constructor (matches conversion in service) ---
    public CuringActionDTO(Long actionId,
                           Long eventId,
                           Long customerId,
                           String serviceName,
                           ActionType actionType,
                           ActionStatus status,
                           LocalDateTime createdAt,
                           LocalDateTime resolvedAt) {
        this.actionId = actionId;
        this.eventId = eventId;
        this.customerId = customerId;
        this.serviceName = serviceName;
        this.actionType = actionType;
        this.status = status;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    // Getters and setters...
    public Long getActionId() { return actionId; }
    public void setActionId(Long actionId) { this.actionId = actionId; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public ActionStatus getStatus() { return status; }
    public void setStatus(ActionStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
