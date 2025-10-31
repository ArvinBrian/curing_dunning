package com.example.curingdunning.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.curingdunning.dto.ChatResponse;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.repository.CustomerRepository;
import com.example.curingdunning.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> body,
                                  @AuthenticationPrincipal UserDetails userDetails) {

        // Ensure user is authenticated
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        // Find the customer using the authenticated user's email
        String email = userDetails.getUsername();
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found for email: " + email));

        // Extract message from request
        String message = body.get("message");

        // Process message for that specific customer
        ChatResponse response = chatService.handleUserMessage(message, customer.getCustomerId());
        return ResponseEntity.ok(response);
    }
}