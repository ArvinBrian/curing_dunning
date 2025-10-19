package com.example.curingdunning.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.entity.Otp;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findByCustomerAndOtpCodeAndStatus(Customer customer, String otpCode, Otp.Status status);
}

