package com.example.curingdunning.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "dunning_event")
public class DunningEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "rule_id", nullable = false)
    private DunningRule rule;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime triggeredAt = LocalDateTime.now();
    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "event")
    private List<CuringAction> actions;

    public enum Status {
        PENDING,
        PROCESSED,
        FAILED
    }

    // getters and setters
}
