package com.example.curingdunning.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.curingdunning.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<Customer> findById(Long id);
    @Query("SELECT c FROM Customer c WHERE " +
            // If customerId is provided, match it
            "(:customerId IS NULL OR c.customerId = :customerId) AND " +
            // If phone is provided, match it (using LIKE for partial matching is common)
            "(:phone IS NULL OR c.phone LIKE CONCAT('%', :phone, '%'))")
     List<Customer> findByFilters(@Param("customerId") Long customerId, @Param("phone") String phone);
}
