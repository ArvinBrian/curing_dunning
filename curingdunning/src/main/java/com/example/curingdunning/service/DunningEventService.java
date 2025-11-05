package com.example.curingdunning.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.example.curingdunning.dto.DunningEventDTO;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.entity.DunningEvent;
import com.example.curingdunning.entity.DunningRule;
import com.example.curingdunning.entity.PlanType;
import com.example.curingdunning.entity.ServiceSubscription;
import com.example.curingdunning.repository.CustomerRepository;
import com.example.curingdunning.repository.DunningEventRepository;
import com.example.curingdunning.repository.DunningRuleRepository;
import com.example.curingdunning.repository.ServiceSubscriptionRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
public class DunningEventService {

    @Autowired 
    private DunningRuleRepository ruleRepo;
    
    @Autowired 
    private DunningEventRepository eventRepo;

    @Autowired 
    private ServiceSubscriptionRepository subRepo;

    @Autowired 
    private CustomerRepository customerRepo;
    
//    @Autowired
//    @Lazy
//    private BillService billService; // Enable this for EOC bill generation

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Generate events for all customers by scanning subscriptions and rules.
     * Supports mobile plans only, differentiates prepaid and postpaid.
     */
    
    private DunningEventDTO toDto(DunningEvent event) {
        DunningEventDTO dto = new DunningEventDTO();
        dto.setEventId(event.getId());
        dto.setServiceName(event.getSubscription() != null ? event.getSubscription().getServiceName() : "N/A");
        dto.setDaysOverdue(event.getDaysOverdue());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    
    public List<DunningEventDTO> getEventsForCustomer(Long customerId) {
        // Fetch all events for the given customer, ordered by creation date (This now uses the direct customer link)
        return eventRepo.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toDto) // Convert each event to a DTO
                .collect(Collectors.toList()); // Collect them into a list
    }
    
 // DunningEventService.java

    public void generateEventsForAllCustomers() {
        LocalDate today = LocalDate.now();
        List<ServiceSubscription> subscriptions;
        try {
            subscriptions = subRepo.findAllWithCustomer();
        } catch (Throwable t) {
            log.error("Fatal error fetching subscriptions", t);
            return;
        }

        for (ServiceSubscription subscription : subscriptions) {
            try {
                String status = subscription.getStatus();
                if ("INACTIVE".equals(status) || "RESOLVED".equals(status)) {
                    continue;
                }
                Customer customer = subscription.getCustomer();
                PlanType planType = subscription.getPlanType();

                if (planType == PlanType.POSTPAID) {
                    handlePostpaid(subscription, customer, today);
                } else if (planType == PlanType.PREPAID) {
                    handlePrepaid(subscription, customer);
                }
            } catch (Exception e) {
                log.error("Error processing subscription ID {} (Service: {}): {}", 
                    subscription.getId(), subscription.getServiceName(), e.getMessage(), e);
                // continue to next subscription
            } catch (Throwable t) {
                log.error("Fatal error processing subscription ID {} (Service: {}): {}", 
                    subscription.getId(), subscription.getServiceName(), t.getMessage(), t);
            }
        }
    }

    @Transactional
    public void handlePrepaid(ServiceSubscription sub, Customer customer) {
        LocalDate today = LocalDate.now();
        LocalDate nextDueDate = sub.getNextPaymentDate();
        
        if (nextDueDate == null) {
            log.warn("No next due date for customer={} subscription={}", 
                customer.getCustomerId(), sub.getServiceName());
            return;
        }

        long daysOverdue = ChronoUnit.DAYS.between(nextDueDate, today);
        
        // --- EOC Bill Generation for Prepaid ---
//        final int EOC_TRIGGER_DAYS_BEFORE = 5;
//        long daysUntilDue = ChronoUnit.DAYS.between(today, nextDueDate);
//        
//        if (daysUntilDue >= 0 && daysUntilDue <= EOC_TRIGGER_DAYS_BEFORE) {
//            if (!billService.billExists(customer, sub, nextDueDate)) {
//                billService.generateUpcomingPrepaidBill(customer, sub, nextDueDate);
//                log.info("Prepaid EOC Check: Bill generated for due date: {}", nextDueDate);
//            }
//        }

        // --- Dunning Event Logic ---
        if (daysOverdue < 0) {
            log.info("Subscription not overdue for customer={} subscription={} (days overdue: {})",
                     customer.getCustomerId(), sub.getServiceName(), daysOverdue);
            return;
        }

        // Find rules that match the exact number of overdue days for the subscription's service and plan type.
        List<DunningRule> rules = ruleRepo.findByOverdueDaysAndServiceNameAndPlanType(
                (int) daysOverdue,
                sub.getServiceName(),
                PlanType.PREPAID
        );

        if (rules.isEmpty()) {
            log.info("No applicable rule found for customer={} subscription={} on exact overdue day {}",
                     customer.getCustomerId(), sub.getServiceName(), daysOverdue);
            return;
        }

        // Process each rule that matches today's overdue count
        for (DunningRule rule : rules) {
            log.info("Found matching rule {} for subscription {} (days overdue: {})",
                rule.getId(), sub.getServiceName(), daysOverdue);

            // Check if an event for this specific subscription and rule combination already exists.
            boolean eventExists = eventRepo.existsBySubscriptionAndAppliedRule(sub, rule);

            if (eventExists) {
                log.info("Event for rule {} and subscription {} already exists. Skipping.",
                    rule.getId(),
                    sub.getId());
                continue; // Skip to the next rule
            }

            // Create new event
            DunningEvent ev = createDunningEvent(customer, sub, rule, daysOverdue, nextDueDate);
            eventRepo.save(ev);

            // Apply immediate actions if needed
            applyImmediateAction(sub, rule.getAction());

            log.info("Created dunning event: customer={} subscription={} rule={} action={}",
                customer.getCustomerId(), sub.getId(), rule.getId(), rule.getAction());
        }
    }

    @Transactional
    public void handlePostpaid(ServiceSubscription sub, Customer customer, LocalDate today) {
        LocalDate dueDate = sub.getNextPaymentDate();
        if (dueDate == null) {
            log.warn("No next due date for postpaid subscription ID {}", sub.getId());
            return;
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, today);
        long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);
        
        // --- EOC Bill Generation for Postpaid ---
//        final int EOC_TRIGGER_DAYS_BEFORE = 5;
//        
//        // Generate upcoming bill if within trigger window
//        if (daysUntilDue >= 0 && daysUntilDue <= EOC_TRIGGER_DAYS_BEFORE) {
//            if (!billService.billExists(customer, sub, dueDate)) {
//                billService.generateUpcomingPostpaidBill(customer, sub, dueDate);
//                log.info("Postpaid EOC: Generated upcoming bill for due date {}", dueDate);
//            }
//        }

        // --- Dunning Event Logic ---
        if (daysOverdue < 1) {
            log.debug("Postpaid subscription not overdue: customer={} service={} daysOverdue={}", 
                customer.getCustomerId(), sub.getServiceName(), daysOverdue);
            return;
        }

        // Find rules that match the exact number of overdue days for the subscription's service and plan type.
        List<DunningRule> rules = ruleRepo.findByOverdueDaysAndServiceNameAndPlanType(
                (int) daysOverdue,
                sub.getServiceName(),
                PlanType.POSTPAID
        );

        if (rules.isEmpty()) {
            log.info("No applicable rule for postpaid: service={} on exact overdue day {}",
                sub.getServiceName(), daysOverdue);
            return;
        }

        for (DunningRule rule : rules) {
            log.info("Found matching rule {} for postpaid {} (days overdue: {})",
                rule.getId(), sub.getServiceName(), daysOverdue);

            // Check if an event for this specific subscription and rule combination already exists.
            boolean eventExists = eventRepo.existsBySubscriptionAndAppliedRule(sub, rule);

            if (eventExists) {
                log.info("Event for rule {} and subscription {} already exists. Skipping.",
                    rule.getId(),
                    sub.getId());
                continue; // Skip to the next rule
            }

            // Create new dunning event
            DunningEvent ev = createDunningEvent(customer, sub, rule, daysOverdue, dueDate);
            eventRepo.save(ev);

            log.info("Created postpaid dunning event: customer={} service={} rule={}",
                customer.getCustomerId(), sub.getId(), rule.getId());

            // Apply immediate actions
            applyImmediateAction(sub, rule.getAction());
        }
    }

    private DunningEvent createDunningEvent(Customer customer, ServiceSubscription sub, DunningRule rule, long daysOverdue, LocalDate dueDate) {
        DunningEvent ev = new DunningEvent();
        ev.setCustomer(customer);
        ev.setSubscription(sub); // Set the direct subscription link
        ev.setDaysOverdue((int) daysOverdue);
        ev.setOriginalDueDate(dueDate.atStartOfDay());
        ev.setStatus("PENDING");
        ev.setTriggeredBy("SYSTEM");
        ev.setCreatedAt(LocalDateTime.now());
        ev.setAppliedRule(rule);
        return ev;
    }

    private void applyImmediateAction(ServiceSubscription sub, String action) {
        boolean blockSubscription = false;
        if ("THROTTLE_DATA".equals(action) || "BAR_OUTGOING_CALLS".equals(action) || "THROTTLE_SPEED".equals(action)) {
            blockSubscription = true;
        }

        if (blockSubscription) {
            // Only update if the status is not already BLOCKED
            if (!"BLOCKED".equals(sub.getStatus())) {
                sub.setStatus("BLOCKED");
                subRepo.save(sub);
                log.info("Applied action '{}' and set status to BLOCKED for subscription {}", action, sub.getId());
            } else {
                log.info("Action '{}' applicable, but subscription {} is already BLOCKED.", action, sub.getId());
            }
        }
    }

    @Transactional
    public void markResolved(Long eventId, Long ruleId) {
        // Fetch the event
        DunningEvent ev = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Fetch the applied rule
        DunningRule rule = ruleRepo.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        // Update event details
        ev.setStatus("RESOLVED");
        ev.setResolvedAt(LocalDateTime.now());
        ev.setAppliedRule(rule);
        eventRepo.save(ev);

        // Fetch customer's subscription
        List<ServiceSubscription> subs = subRepo.findByCustomerCustomerIdAndId(ev.getCustomer().getCustomerId(), ev.getSubscription().getId());

        if (subs.isEmpty()) {
            throw new RuntimeException("Subscription not found");
        }

        ServiceSubscription sub = subs.get(0);
        sub.setStatus("ACTIVE");
        subRepo.save(sub);

        log.info("Customer {} subscription {} reset to ACTIVE after resolving rule {}",
                 ev.getCustomer().getCustomerId(), sub.getId(), rule.getId());
    }

}
