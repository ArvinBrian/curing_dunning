package com.example.curingdunning.service;

// PaymentService.java (New Service)


import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.curingdunning.dto.ApplyActionDTO;
import com.example.curingdunning.dto.OrderResponse;
import com.example.curingdunning.entity.ActionType;

import jakarta.transaction.Transactional;

@Service
public class PaymentService {

    @Autowired
    private MockPaymentGateway mockGateway; // Inject the mock interface

    @Autowired
    private BillService billService;
    
    @Autowired
    private CuringActionService curingActionService; 
    
    // You'll need to define BillService and CuringActionService
    // or inject their implementations (e.g., BillServiceImpl)


    // ... (Your other services and repos) ...

    public OrderResponse createRazorpayOrder(Long billId, BigDecimal amount, String currency, String keyId, String keySecret) {
        // 1. Call Mock Gateway to get the simulated Order ID
        String orderId = mockGateway.createOrder(amount, currency);

        // 2. Return the necessary data to the frontend for the payment popup
        OrderResponse response = new OrderResponse();
        response.setOrderId(orderId);
        response.setBillId(billId);
        response.setAmount(amount);
        response.setKeyId(keyId); // Still send the key ID for the frontend to use
        
        return response;
    }

    @Transactional
    public void verifyAndCureDunning(Long billId, String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, String secret) {
        // 1. Verify Payment Signature using the Mock
        boolean verified = mockGateway.verifySignature(
            razorpayOrderId, 
            razorpayPaymentId, 
            razorpaySignature, 
            secret
        );

        if (!verified) {
            throw new SecurityException("Payment verification failed! Invalid signature.");
        }

        // 2. Fetch the Dunning Event ID linked to the Bill
        Long dunningEventId = billService.getDunningEventIdForBill(billId); // <-- NOW WORKS

        // 3. Mark the Bill as Paid
        billService.markBillAsPaid(billId); // <-- NOW WORKS

        // 4. Trigger the Curing Action
        ApplyActionDTO actionDto = new ApplyActionDTO();
        actionDto.setEventId(dunningEventId);
        actionDto.setActionType(ActionType.MAKE_PAYMENT); // Assuming this enum value exists
        
        // This handles saving to curing_actions and marking the Dunning Event as RESOLVED
        curingActionService.applyAction(actionDto);
        
        // Success
    }
}