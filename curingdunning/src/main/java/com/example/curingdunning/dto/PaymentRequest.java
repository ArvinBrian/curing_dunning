package com.example.curingdunning.dto;

// PaymentRequest.java (in your DTO package)
import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long billId;
    private BigDecimal amount; // Use BigDecimal for currency
    private String currency;   // e.g., "INR"

    // Constructors, Getters, and Setters
    // (Ensure you have a default constructor and getters/setters for Jackson mapping)

}