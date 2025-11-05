package com.example.curingdunning.repository;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.curingdunning.dto.DunningEventDTO;
import com.example.curingdunning.entity.DunningEvent;
import com.example.curingdunning.entity.DunningRule;
import com.example.curingdunning.entity.PlanType;
import com.example.curingdunning.entity.ServiceSubscription;

public interface DunningEventRepository extends JpaRepository<DunningEvent, Long> {
    boolean existsByAppliedRuleId(Long ruleId);
    List<DunningEvent> findByCustomer_CustomerIdAndSubscription_ServiceNameAndStatus(Long customerId, String serviceName, String status);
    
 // Required by processScheduledBillingAndOverdues (Section 1)
    boolean existsByCustomer_CustomerIdAndSubscription_ServiceNameAndStatus(
        Long customerId, 
        String serviceName, 
        String status
    );
    
    List<DunningEvent> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);
    
    // Required by processScheduledBillingAndOverdues (Section 2)
    List<DunningEvent> findByStatus(String status);
//	Collection<DunningEventDTO> findByCustomerCustomerIdOrderByCreatedAtDesc(Long customerId);
//	Collection<DunningEventDTO> findByServiceNameAndPlanType(String serviceName, PlanType prepaid);
//    List<DunningRule> findByServiceNameAndPlanType(String serviceName, PlanType planType);
	List<DunningEvent> findBySubscription_Customer_CustomerId(Long customerId);
boolean existsBySubscriptionAndAppliedRule(ServiceSubscription subscription, DunningRule rule);
List<DunningEvent> findByCustomerCustomerId(Long customerId);

	
}