package com.example.curingdunning.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.curingdunning.dto.JwtResponse;
import com.example.curingdunning.dto.LoginRequest;
import com.example.curingdunning.dto.SignupRequest;
import com.example.curingdunning.service.AuthService;
import com.example.curingdunning.service.OtpService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        log.info("POST Request Signup for: {}",request.getEmail());
    	return authService.signup(request);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
    
    @Autowired
    private OtpService otpService;

    @PostMapping("/generate-otp")
    public ResponseEntity<String> generateOtp(@RequestParam String email) {
        otpService.generateOtp(email);
        return ResponseEntity.ok("OTP sent (check DB for now)");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String code) {
        boolean valid = otpService.verifyOtp(email, code);
        if (valid) {
            return ResponseEntity.ok("OTP verified successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<String> getCustomerInfo(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok("Authenticated user: " + email);
    }


}
