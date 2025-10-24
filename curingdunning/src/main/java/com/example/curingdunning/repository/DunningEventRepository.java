package com.example.curingdunning.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.curingdunning.entity.DunningEvent;

public interface DunningEventRepository extends JpaRepository<DunningEvent, Long> {
    List<DunningEvent> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<DunningEvent> findByCustomerCustomerIdAndStatus(Long customerId, String status);
}