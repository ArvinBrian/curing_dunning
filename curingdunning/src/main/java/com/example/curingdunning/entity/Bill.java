package com.example.curingdunning.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billId;
    
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "dunning_event_id", nullable = true)
    private DunningEvent dunningEvent;


    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private Boolean paid = false;

    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private String status; // PENDING, PAID, OVERDUE

    private Integer overdueDays = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
}
