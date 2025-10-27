package com.example.curingdunning.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.curingdunning.dto.DashboardDTO;
import com.example.curingdunning.service.DashboardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService service;

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(service.getDashboardForCustomer(email));
    }
}
