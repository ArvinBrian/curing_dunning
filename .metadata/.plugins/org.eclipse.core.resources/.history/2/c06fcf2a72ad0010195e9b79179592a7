package com.example.curingdunning.entity;

import java.time.LocalDateTime;

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
@Table(name = "curing_action")
public class CuringAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long actionId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private DunningEvent event;

    @Enumerated(EnumType.STRING)
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime executedAt;

    public enum ActionType {
        EMAIL,
        SMS,
        CALL,
        WAIVER
    }

    public enum Status {
        PENDING,
        COMPLETED,
        FAILED
    }

    // getters and setters
}
