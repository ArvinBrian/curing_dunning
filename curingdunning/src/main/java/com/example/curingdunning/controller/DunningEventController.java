package com.example.curingdunning.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.curingdunning.dto.DunningEventDTO;
import com.example.curingdunning.service.DunningEventService;

@RestController
@RequestMapping("/dunning-events")
public class DunningEventController {

    @Autowired
    private DunningEventService service;

    // Get all dunning events for a customer
    @GetMapping("/{customerId}")
    public ResponseEntity<List<DunningEventDTO>> getEvents(@PathVariable Long customerId,
                                                           Authentication auth) {
        String email = auth.getName(); // email from JWT
        // Optional: validate customerId matches logged-in user
        return ResponseEntity.ok(service.getEventsForCustomer(customerId));
    }

    // Trigger dunning events manually
    @PostMapping("/trigger")
    public ResponseEntity<String> triggerEvents() {
        service.generateEventsForAllCustomers();
        return ResponseEntity.ok("Events triggered");
    }
}
