package com.example.curingdunning.service;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.curingdunning.dto.JwtResponse;
import com.example.curingdunning.dto.LoginRequest;
import com.example.curingdunning.dto.SignupRequest;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.repository.CustomerRepository;
import com.example.curingdunning.security.JwtUtil;

@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private JwtUtil jwtUtil;
	
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<String> signup(SignupRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>("Email already registered", HttpStatus.BAD_REQUEST);
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setStatus(Customer.Status.ACTIVE);
        customer.setUpdatedAt(java.time.LocalDateTime.now());
        customer.setCreatedAt(java.time.LocalDateTime.now());

        // hash password
        customer.setPassword(passwordEncoder.encode(request.getPassword()));

        customerRepository.save(customer);
        return new ResponseEntity<>("Signup successful", HttpStatus.CREATED);
    }
    @Override
    public ResponseEntity<JwtResponse> login(LoginRequest request) {
        Optional<Customer> existingCustomerOpt = customerRepository.findByEmail(request.getEmail());

        if (existingCustomerOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Customer customer = existingCustomerOpt.get();

        // Check if account is ACTIVE
        if (customer.getStatus() != Customer.Status.ACTIVE) {
            throw new RuntimeException("Account not verified. Please verify OTP first.");
        }

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Generate JWT
        String token = jwtUtil.generateToken(customer.getEmail());

        // Return JWT + customer info
        JwtResponse response = new JwtResponse(token, customer.getEmail(), customer.getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
