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
        dto.setServiceName(event.getServiceName());
        dto.setDaysOverdue(event.getDaysOverdue());
        dto.setStatus(event.getStatus());
        dto.setCreatedAt(event.getCreatedAt());
        return dto;
    }

    
    public List<DunningEventDTO> getEventsForCustomer(Long customerId) {
        // Fetch all events for the given customer, ordered by creation date
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
            subscriptions = subRepo.findAll();
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

        // Fixed: Get all applicable rules and sort by overdueDays ASC (less severe first)
        List<DunningRule> rules = ruleRepo.findByServiceNameAndPlanType(sub.getServiceName(), PlanType.PREPAID)
                .stream()
                .filter(r -> r.getOverdueDays() != null && daysOverdue >= r.getOverdueDays())
                .sorted(Comparator.comparing(DunningRule::getOverdueDays))
                .collect(Collectors.toList());

        if (rules.isEmpty()) {
            log.info("No applicable rules found for customer={} subscription={} (days overdue: {})",
                     customer.getCustomerId(), sub.getServiceName(), daysOverdue);
            return;
        }

        // Get the most appropriate rule (highest overdueDays that's still <= actual overdue days)
        DunningRule chosen = rules.get(rules.size() - 1);
        log.info("Selected rule {} for subscription {} (days overdue: {})", 
            chosen.getId(), sub.getServiceName(), daysOverdue);

        // Check for existing unresolved event with same action
        boolean exists = eventRepo.findByCustomerCustomerIdOrderByCreatedAtDesc(customer.getCustomerId())
                .stream()
                .anyMatch(e -> e.getServiceName().equals(sub.getServiceName())
                        && !"RESOLVED".equals(e.getStatus())
                        && e.getAppliedRule() != null
                        && e.getAppliedRule().getAction().equals(chosen.getAction()));

        if (exists) {
            log.info("Event already exists for customer={} subscription={} action={}", 
                customer.getCustomerId(), sub.getServiceName(), chosen.getAction());
            return;
        }

        // Create new event
        DunningEvent ev = new DunningEvent();
        ev.setCustomer(customer);
        ev.setServiceName(sub.getServiceName());
        ev.setDaysOverdue((int) daysOverdue);
        ev.setOriginalDueDate(nextDueDate.atStartOfDay());
        ev.setStatus("PENDING");
        ev.setTriggeredBy("SYSTEM");
        ev.setCreatedAt(LocalDateTime.now());
        ev.setAppliedRule(chosen);
        eventRepo.save(ev);

        // Apply immediate actions if needed
        if ("THROTTLE_DATA".equals(chosen.getAction()) || 
            "BAR_OUTGOING_CALLS".equals(chosen.getAction())) {
            sub.setStatus("BLOCKED");
            subRepo.save(sub);
        }

        log.info("Created dunning event: customer={} subscription={} rule={} action={}", 
            customer.getCustomerId(), sub.getServiceName(), chosen.getId(), chosen.getAction());
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

        // Get applicable rules sorted by severity (overdueDays ASC)
        List<DunningRule> rules = ruleRepo.findByServiceNameAndPlanType(sub.getServiceName(), PlanType.POSTPAID)
                .stream()
                .filter(r -> r.getOverdueDays() != null && daysOverdue >= r.getOverdueDays())
                .sorted(Comparator.comparingInt(DunningRule::getOverdueDays))
                .collect(Collectors.toList());

        if (rules.isEmpty()) {
            log.info("No applicable rules for postpaid: service={} daysOverdue={}", 
                sub.getServiceName(), daysOverdue);
            return;
        }

        // Get most severe applicable rule
        DunningRule chosen = rules.get(rules.size() - 1);
        log.info("Selected rule {} for postpaid {} (days overdue: {})", 
            chosen.getId(), sub.getServiceName(), daysOverdue);

        // Check for existing unresolved event
        boolean exists = eventRepo.findByCustomerCustomerIdOrderByCreatedAtDesc(customer.getCustomerId())
                .stream()
                .anyMatch(e -> e.getServiceName().equals(sub.getServiceName())
                        && !"RESOLVED".equals(e.getStatus())
                        && e.getAppliedRule() != null
                        && e.getAppliedRule().getAction().equals(chosen.getAction()));

        if (!exists) {
            // Create new dunning event
            DunningEvent ev = new DunningEvent();
            ev.setCustomer(customer);
            ev.setServiceName(sub.getServiceName());
            ev.setDaysOverdue((int) daysOverdue);
            ev.setStatus("PENDING");
            ev.setTriggeredBy("SYSTEM");
            ev.setCreatedAt(LocalDateTime.now());
            ev.setAppliedRule(chosen);
            ev.setOriginalDueDate(dueDate.atStartOfDay());
            eventRepo.save(ev);

            log.info("Created postpaid dunning event: customer={} service={} rule={}", 
                customer.getCustomerId(), sub.getServiceName(), chosen.getId());

            // Apply immediate actions
            if ("THROTTLE_SPEED".equals(chosen.getAction())) {
                sub.setStatus("BLOCKED");
                subRepo.save(sub);
                log.info("Applied THROTTLE_SPEED to subscription {}", sub.getId());
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
        List<ServiceSubscription> subs = subRepo.findByCustomerCustomerIdAndServiceName(
            ev.getCustomer().getCustomerId(),
            ev.getServiceName()
        );

        if (subs.isEmpty()) {
            throw new RuntimeException("Subscription not found");
        }

        ServiceSubscription sub = subs.get(0);
        sub.setStatus("ACTIVE");
        subRepo.save(sub);

        log.info("Customer {} subscription {} reset to ACTIVE after resolving rule {}",
                 ev.getCustomer().getCustomerId(), ev.getServiceName(), rule.getId());
    }



}
