package com.example.curingdunning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.curingdunning.entity.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {
    List<Bill> findByCustomerCustomerId(Long customerId);
    List<Bill> findByStatus(String status);
    List<Bill> findByCustomerCustomerIdAndStatus(Long customerId, String status);

}
