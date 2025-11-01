package com.example.curingdunning.service;

// MockPaymentGateway.java (in your Service package)

import java.math.BigDecimal;

public interface MockPaymentGateway {
    
    /**
     * Simulates creating an Order. In a real system, this calls the Razorpay API.
     * @return A unique, simulated order ID.
     */
    String createOrder(BigDecimal amount, String currency);

    /**
     * Simulates verifying the payment signature and ID.
     * @return True if verification passes, false otherwise.
     */
    boolean verifySignature(String orderId, String paymentId, String signature, String secret);
}