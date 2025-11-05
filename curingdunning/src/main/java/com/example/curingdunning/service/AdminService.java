package com.example.curingdunning.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.curingdunning.dto.AllEventsViewDTO;
import com.example.curingdunning.dto.CustomerDTO;
import com.example.curingdunning.dto.CustomerDetailsDTO;
import com.example.curingdunning.dto.DunningEventDTO;
import com.example.curingdunning.dto.SubscriptionDTO;
import com.example.curingdunning.entity.Admin;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.entity.DunningEvent;
import com.example.curingdunning.entity.DunningRule;
import com.example.curingdunning.entity.ServiceSubscription;
import com.example.curingdunning.repository.AdminRepository;
import com.example.curingdunning.repository.CustomerRepository;
import com.example.curingdunning.repository.DunningEventRepository;
import com.example.curingdunning.repository.DunningRuleRepository;
import com.example.curingdunning.repository.ServiceSubscriptionRepository;
import com.example.curingdunning.security.JwtUtil;

@Service
@Transactional
public class AdminService {

    @Autowired
    private AdminRepository adminRepo;

    @Autowired private DunningRuleRepository ruleRepo;
    @Autowired private ServiceSubscriptionRepository subRepo;
    @Autowired private CustomerRepository customerRepo;
    @Autowired private DunningEventRepository eventRepo;

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder; // Inject the password encoder


    // ---------------- Admin login ----------------

    public Admin login(String email, String password) {
        Admin admin = adminRepo.findByEmail(email);
        if (admin != null && admin.getPassword().equals(password)) {
            return admin;
        }
        return null; // Invalid credentials
    }
    
    public List<DunningEvent> getAllDunningEvents(String status, String planType) {
        // In a real-world scenario with lots of data, you would use Specifications
        // or dynamic queries. For now, we can filter in-memory.
        List<DunningEvent> allEvents = eventRepo.findAll();

        if (status != null && !status.trim().isEmpty()) {
            allEvents.removeIf(event -> !event.getStatus().equalsIgnoreCase(status));
        }

        if (planType != null && !planType.trim().isEmpty() && !planType.equalsIgnoreCase("ALL")) {
            // This assumes DunningEvent has access to the rule's planType.
            // You may need to adjust this based on your entity relationships.
            allEvents.removeIf(event -> event.getAppliedRule() == null || !event.getAppliedRule().getPlanType().name().equalsIgnoreCase(planType));
        }

        return allEvents;
    }
    
    public List<AllEventsViewDTO> getAllDunningEvents(String status, String planType, String serviceName) {
        List<DunningEvent> allEvents = eventRepo.findAll();

        // Filter the events based on the provided criteria
        List<DunningEvent> filteredEvents = allEvents.stream()
            .filter(event -> status == null || status.trim().isEmpty() || event.getStatus().equalsIgnoreCase(status))
            .filter(event -> serviceName == null || serviceName.trim().isEmpty() || (
                event.getSubscription() != null && 
                event.getSubscription().getServiceName().equalsIgnoreCase(serviceName))
            )
            .filter(event -> {
                if (planType == null || planType.trim().isEmpty() || planType.equalsIgnoreCase("ALL")) {
                    return true; // No planType filter, so include the event
                }
                // Check if the event has a rule and if the planType matches
                return event.getAppliedRule() != null && event.getAppliedRule().getPlanType().name().equalsIgnoreCase(planType);
            })
            .collect(Collectors.toList());

        // Map the filtered entities to our new AllEventsViewDTO
        return filteredEvents.stream().map(event -> {
            AllEventsViewDTO dto = new AllEventsViewDTO();
            dto.setId(event.getId());
            if (event.getSubscription() != null) {
                dto.setServiceName(event.getSubscription().getServiceName());
            }
            dto.setStatus(event.getStatus());
            dto.setCreatedAt(event.getCreatedAt());

            // Safely get data from related entities
            if (event.getCustomer() != null) {
                dto.setCustomerId(event.getCustomer().getCustomerId());
                dto.setCustomerName(event.getCustomer().getName());
            }
            if (event.getAppliedRule() != null) {
                dto.setPlanType(event.getAppliedRule().getPlanType().name());
            }

            return dto;
        }).collect(Collectors.toList());
    }

    // 1. Rule Management
    public DunningRule createRule(DunningRule rule) {
        validateRule(rule);
        return ruleRepo.save(rule);
    }

    public DunningRule updateRule(Long id, DunningRule newRule) {
        DunningRule existing = ruleRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Rule not found: " + id));
        
        validateRule(newRule);
        
        // Update fields
        existing.setServiceName(newRule.getServiceName());
        existing.setOverdueDays(newRule.getOverdueDays());
        existing.setAction(newRule.getAction());
        existing.setPriority(newRule.getPriority());
        existing.setTimeOfDay(newRule.getTimeOfDay());
        existing.setPlanType(newRule.getPlanType());
        
        return ruleRepo.save(existing);
    }

    public void deleteRule(Long id) {
        // Check if rule is in use
        if (eventRepo.existsByAppliedRuleId(id)) {
            throw new RuntimeException("Cannot delete rule that is referenced by events");
        }
        ruleRepo.deleteById(id);
    }

    public List<DunningRule> getAllRules() {
        return ruleRepo.findAll();
    }

    // 2. Subscription Management
    @Transactional
    public void updateSubscription(Long subscriptionId, Map<String, Object> updates) {
        ServiceSubscription sub = subRepo.findById(subscriptionId)
            .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Update fields based on the map
        if (updates.containsKey("status")) {
            String newStatus = (String) updates.get("status");
            validateStatus(newStatus);
            sub.setStatus(newStatus);
        }

        if (updates.containsKey("dueAmount")) {
            BigDecimal newAmount = new BigDecimal(updates.get("dueAmount").toString());
            sub.setDueAmount(newAmount);
        }

        if (updates.containsKey("nextPaymentDate")) {
            LocalDate newDate = LocalDate.parse(updates.get("nextPaymentDate").toString());
            sub.setNextPaymentDate(newDate);
        }

        // Save changes
        subRepo.save(sub);
        
        // Log the change
        //log.info("Admin updated subscription {}: {}", subscriptionId, updates);
    }

    // 3. Service Status Management
    @Transactional
    public void updateServiceStatus(Long customerId, String serviceName, String newStatus) {
        List<ServiceSubscription> subs = subRepo.findByCustomerCustomerIdAndServiceName(customerId, serviceName);
        if (subs.isEmpty()) {
            throw new RuntimeException("No subscription found");
        }

        validateStatus(newStatus);

        for (ServiceSubscription sub : subs) {
            sub.setStatus(newStatus);
            subRepo.save(sub);

            // If reactivating service, resolve any pending dunning events
            if ("ACTIVE".equals(newStatus)) {
                resolvePendingDunningEvents(customerId, serviceName);
            }
        }
    }

    // Helper Methods
    private void validateRule(DunningRule rule) {
        if (rule.getServiceName() == null || rule.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name is required");
        }
        if (rule.getOverdueDays() == null || rule.getOverdueDays() < 0) {
            throw new IllegalArgumentException("Valid overdue days required");
        }
        if (rule.getPlanType() == null) {
            throw new IllegalArgumentException("Plan type is required");
        }
        // Add more validations as needed
    }

    private void validateStatus(String status) {
        List<String> validStatuses = Arrays.asList("ACTIVE", "BLOCKED", "SUSPENDED", "INACTIVE");
        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private void resolvePendingDunningEvents(Long customerId, String serviceName) {
        List<DunningEvent> events = eventRepo.findByCustomer_CustomerIdAndSubscription_ServiceNameAndStatus(
            customerId, serviceName, "PENDING"
        );
        
        for (DunningEvent event : events) {
            event.setStatus("RESOLVED");
            event.setResolvedAt(LocalDateTime.now());
            eventRepo.save(event);
        }
    }
    
 // **NEW METHOD TO GET ALL CUSTOMERS (Fallback)**
    public List<Customer> getAllCustomers() {
        return customerRepo.findAll();
    }
    
    public List<CustomerDTO> getCustomersByFilters(Long customerId, String phoneNumber) {
        boolean hasCustomerId = (customerId != null);
        boolean hasPhoneNumber = (phoneNumber != null && !phoneNumber.trim().isEmpty());

        List<Customer> customers; // <-- Declare list

        if (!hasCustomerId && !hasPhoneNumber) {
            customers = customerRepo.findAll(); 
        } else {
            String phoneFilter = hasPhoneNumber ? phoneNumber.trim() : null;
            customers = customerRepo.findByFilters(customerId, phoneFilter);
        }

        // --- MAP THE ENTITY LIST TO A DTO LIST ---
        return customers.stream()
            .map(CustomerDTO::new) // Uses the constructor we made
            .collect(Collectors.toList());
    }
    
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCustomers", customerRepo.count());
        stats.put("totalRules", ruleRepo.count());
        stats.put("totalSubscriptions", subRepo.count());
        return stats;
    }
    
    public Customer createCustomer(Customer customerData) {
        // Check if email already exists
        if (customerRepo.findByEmail(customerData.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Customer with email " + customerData.getEmail() + " already exists.");
        }

        Customer newCustomer = new Customer();
        newCustomer.setName(customerData.getName());
        newCustomer.setEmail(customerData.getEmail());
        newCustomer.setPhone(customerData.getPhone());
        
        // Hash the password before saving
        newCustomer.setPassword(passwordEncoder.encode(customerData.getPassword()));
        
        newCustomer.setStatus(Customer.Status.ACTIVE); // Default to ACTIVE
        newCustomer.setRole("CUSTOMER");
        newCustomer.setCreatedAt(LocalDateTime.now());
        newCustomer.setUpdatedAt(LocalDateTime.now());

        return customerRepo.save(newCustomer);
    }

 // In AdminService.java

    public CustomerDetailsDTO getCustomerDetailsById(Long customerId) {
        // 1. Fetch the core customer entity
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + customerId));

        // 2. Fetch related entities
        List<ServiceSubscription> subscriptions = subRepo.findByCustomerCustomerId(customerId);
        List<DunningEvent> dunningEvents = eventRepo.findByCustomerCustomerId(customerId);

        // 3. Map ServiceSubscription entities to SubscriptionDTOs
        List<SubscriptionDTO> subscriptionDTOs = subscriptions.stream().map(sub -> {
            SubscriptionDTO subDto = new SubscriptionDTO();
            subDto.setSubscriptionId(sub.getId());
            subDto.setServiceName(sub.getServiceName());
            subDto.setStatus(sub.getStatus());
            subDto.setDueAmount(sub.getDueAmount());
            subDto.setNextPaymentDate(sub.getNextPaymentDate());
            return subDto;
        }).collect(Collectors.toList());

        // 4. Map DunningEvent entities to your existing DunningEventDTOs
        List<DunningEventDTO> dunningEventDTOs = dunningEvents.stream().map(event -> {
            DunningEventDTO eventDto = new DunningEventDTO();
            eventDto.setEventId(event.getId());
            if (event.getSubscription() != null) {
                eventDto.setServiceName(event.getSubscription().getServiceName());
            }
            eventDto.setDaysOverdue(event.getDaysOverdue());
            eventDto.setStatus(event.getStatus());
            eventDto.setCreatedAt(event.getCreatedAt());
            return eventDto;
        }).collect(Collectors.toList());

        // 5. Build the final CustomerDetailsDTO
        CustomerDetailsDTO detailsDto = new CustomerDetailsDTO();
        detailsDto.setCustomerId(customer.getCustomerId());
        detailsDto.setName(customer.getName());
        detailsDto.setEmail(customer.getEmail());
        detailsDto.setPhone(customer.getPhone());
        detailsDto.setStatus(customer.getStatus() != null ? customer.getStatus().name() : "UNKNOWN");
        
        detailsDto.setSubscriptions(subscriptionDTOs);
        detailsDto.setDunningEvents(dunningEventDTOs);

        return detailsDto;
    }

}
