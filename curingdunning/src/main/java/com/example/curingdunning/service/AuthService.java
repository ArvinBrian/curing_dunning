package com.example.curingdunning.service;

import org.springframework.http.ResponseEntity;

import com.example.curingdunning.dto.JwtResponse;
import com.example.curingdunning.dto.LoginRequest;
import com.example.curingdunning.dto.SignupRequest;

public interface AuthService {
    ResponseEntity<String> signup(SignupRequest request);
    ResponseEntity<JwtResponse> login(LoginRequest request);
}
