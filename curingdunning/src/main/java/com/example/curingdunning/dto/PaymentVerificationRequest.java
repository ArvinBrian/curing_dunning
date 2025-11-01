package com.example.curingdunning.dto;

import lombok.Data;

@Data
public class PaymentVerificationRequest {
	private Long billId;
    private String razorpayOrderId;   // The order ID created in step A
    private String razorpayPaymentId; // The ID of the successful payment transaction
    private String razorpaySignature;
}
