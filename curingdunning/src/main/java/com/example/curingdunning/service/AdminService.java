package com.example.curingdunning.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.curingdunning.entity.Admin;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.entity.DunningRule;
import com.example.curingdunning.entity.ServiceSubscription;
import com.example.curingdunning.repository.AdminRepository;
import com.example.curingdunning.repository.CustomerRepository;
import com.example.curingdunning.repository.DunningRuleRepository;
import com.example.curingdunning.repository.ServiceSubscriptionRepository;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepo;

    @Autowired
    private DunningRuleRepository ruleRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ServiceSubscriptionRepository subRepo;

    // Admin login
    public boolean login(String email, String password) {
        Admin admin = adminRepo.findByEmail(email);
        return admin != null && admin.getPassword().equals(password);
    }

    // Update a Dunning Rule
    public void updateRule(Long ruleId, DunningRule newRuleData) {
        DunningRule rule = ruleRepo.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        if (newRuleData.getServiceName() != null)
            rule.setServiceName(newRuleData.getServiceName());
        if (newRuleData.getOverdueDays() != 0)
            rule.setOverdueDays(newRuleData.getOverdueDays());
        if (newRuleData.getAction() != null)
            rule.setAction(newRuleData.getAction());
        if (newRuleData.getPriority() != null)
            rule.setPriority(newRuleData.getPriority());

        ruleRepo.save(rule);
    }
    
 // ---------------- Create a new Dunning Rule ----------------
    public DunningRule createRule(DunningRule rule) {
        // Optional: validate rule fields here
        return ruleRepo.save(rule);
    }

    // ---------------- Delete a Dunning Rule ----------------
    public void deleteRule(Long ruleId) {
        if (!ruleRepo.existsById(ruleId)) {
            throw new RuntimeException("Rule not found");
        }
        ruleRepo.deleteById(ruleId);
    }

    // Override a specific subscription (per-service)
    public void overrideSubscriptionAttributes(Long subscriptionId, BigDecimal newDueAmount, String newStatus) {
        ServiceSubscription sub = subRepo.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (newDueAmount != null) sub.setDueAmount(newDueAmount);
        if (newStatus != null) sub.setStatus(newStatus);

        subRepo.save(sub);
    }

    // Override all subscriptions of a customer for a specific service
    public void overrideCustomerSubscription(Long customerId, String serviceName, BigDecimal newDueAmount, String newStatus) {
        Optional<ServiceSubscription> subOpt = subRepo.findByCustomerCustomerIdAndServiceName(customerId, serviceName);

        ServiceSubscription s = subOpt.orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (newDueAmount != null) s.setDueAmount(newDueAmount);
        if (newStatus != null) s.setStatus(newStatus);

        subRepo.save(s);
    }

}
