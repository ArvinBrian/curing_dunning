package com.example.curingdunning.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionDTO {
    private Long subscriptionId;
    private String serviceName;
    private String status;
    private BigDecimal dueAmount;
    private LocalDate nextPaymentDate;
}
