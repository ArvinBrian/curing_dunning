package com.example.curingdunning.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.curingdunning.entity.DunningRule;
import com.example.curingdunning.entity.PlanType;

public interface DunningRuleRepository extends JpaRepository<DunningRule, Long> {
    List<DunningRule> findByServiceName(String serviceName);
    List<DunningRule> findByServiceNameAndPlanType(String serviceName, PlanType planType);
    List<DunningRule> findByOverdueDaysAndServiceNameAndPlanType(int overdueDays, String serviceName, String planType);
    List<DunningRule> findByOverdueDaysAndServiceNameAndPlanType(int daysOverdue, String serviceName, PlanType prepaid);


}