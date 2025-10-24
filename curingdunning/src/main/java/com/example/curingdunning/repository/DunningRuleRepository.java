package com.example.curingdunning.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.curingdunning.entity.DunningRule;
import com.example.curingdunning.entity.PlanType;

public interface DunningRuleRepository extends JpaRepository<DunningRule, Long> {
    List<DunningRule> findByServiceName(String serviceName);
    List<DunningRule> findByServiceNameAndPlanType(String serviceName, PlanType planType);

}