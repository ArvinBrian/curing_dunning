package com.example.curingdunning.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.curingdunning.entity.CuringAction;

@Repository
public interface CuringActionRepository extends JpaRepository<CuringAction, Long> {

    // Correct method: access serviceName via the dunningEvent relationship
    List<CuringAction> findByDunningEventServiceName(String serviceName);

    // Filter by customer and service if needed
    List<CuringAction> findByCustomerCustomerIdAndDunningEventServiceName(Long customerId, String serviceName);

    // Example: get all actions for a customer
    List<CuringAction> findByCustomerCustomerId(Long customerId);
}
