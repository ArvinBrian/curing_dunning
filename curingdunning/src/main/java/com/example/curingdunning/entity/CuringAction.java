package com.example.curingdunning.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "curing_actions")
public class CuringAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionId;

    @ManyToOne
    @JoinColumn(name = "event_id") // Can be null if payment is not from a dunning event
    private DunningEvent dunningEvent;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ActionStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne
    private Bill paidBill;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    private LocalDateTime curedAt = LocalDateTime.now();

    // JPA requires a no-arg constructor
    public CuringAction() {}

    // --- Add this constructor (matches service usage) ---
    public CuringAction(DunningEvent dunningEvent, Customer customer, ActionType actionType) {
        this.dunningEvent = dunningEvent;
        this.customer = customer;
        this.actionType = actionType;
//        this.status = ActionStatus.INITIATED;
//        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters (keep existing ones) ...
    public Long getActionId() { return actionId; }
    public DunningEvent getDunningEvent() { return dunningEvent; }
    public void setDunningEvent(DunningEvent dunningEvent) { this.dunningEvent = dunningEvent; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }
    public ActionStatus getStatus() { return status; }
    public void setStatus(ActionStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
