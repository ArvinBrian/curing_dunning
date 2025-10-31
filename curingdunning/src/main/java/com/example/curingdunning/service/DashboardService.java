package com.example.curingdunning.service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.curingdunning.dto.CuringActionDTO;
import com.example.curingdunning.dto.DashboardDTO;
import com.example.curingdunning.dto.DunningEventDTO;
import com.example.curingdunning.dto.ServiceStatusDTO;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.entity.DunningRule;
import com.example.curingdunning.entity.ServiceSubscription;
import com.example.curingdunning.repository.CustomerRepository;
import com.example.curingdunning.repository.DunningRuleRepository;
import com.example.curingdunning.repository.ServiceSubscriptionRepository;

@Service
public class DashboardService {

    @Autowired 
    private CustomerRepository customerRepo;

    @Autowired 
    private ServiceSubscriptionRepository subRepo;

    @Autowired 
    private DunningEventService eventService;

    @Autowired 
    private CuringActionService actionService;

    @Autowired
    private DunningRuleRepository ruleRepo; // needed for mapping rules to events

    public DashboardDTO getDashboardForCustomer(String email) {
        // fetch customer
        Customer c = customerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        DashboardDTO dash = new DashboardDTO();
        dash.setCustomerId(c.getCustomerId());
        dash.setEmail(c.getEmail());

        // fetch services
        List<ServiceSubscription> subs = subRepo.findByCustomerCustomerId(c.getCustomerId());
        List<ServiceStatusDTO> serviceDTOs = subs.stream().map(s -> {
            ServiceStatusDTO sd = new ServiceStatusDTO();
            sd.setServiceName(s.getServiceName());
            sd.setNextDueDate(s.getNextDueDate());
            sd.setCurrentStatus(s.getStatus()); // ACTIVE / BLOCKED

         // <--- INSERT START -->

            // Determine the date to use for calculation (use nextDueDate if present, fallback to nextPaymentDate)
            LocalDate calculationDate = s.getNextDueDate() != null 
                                      ? s.getNextDueDate() 
                                      : s.getNextPaymentDate();

            long daysUntil = Integer.MAX_VALUE; // Default to a very large number (or 0) if no date is found
            
            if (calculationDate != null) {
                // days until due
                daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), calculationDate);
            }
            sd.setDaysUntilDue((int) daysUntil);
            
            // <--- INSERT END -->
            // blocked service handling
            if ("BLOCKED".equalsIgnoreCase(s.getStatus())) {
                sd.setStatusText("Service blocked due to overdue or limit reached");

                // pending event for this service
                Optional<DunningEventDTO> pendingEvent = eventService.getEventsForCustomer(c.getCustomerId())
                        .stream()
                        .filter(e -> e.getServiceName().equals(s.getServiceName()) && "PENDING".equalsIgnoreCase(e.getStatus()))
                        .findFirst();

                if (pendingEvent.isPresent()) {
                    DunningEventDTO event = pendingEvent.get();

                    // find matching rule that caused this event
                    List<DunningRule> rules = ruleRepo.findByServiceName(s.getServiceName());
                    List<DunningRule> applicableRules = rules.stream()
                            .filter(r -> r.getOverdueDays() != null && event.getDaysOverdue() >= r.getOverdueDays())
                            .sorted(Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : Integer.MAX_VALUE))
                            .collect(Collectors.toList());

                    if (!applicableRules.isEmpty()) {
                        sd.setPendingAction(applicableRules.get(0).getAction()); // recommended curing action
                    }
                }

            } else {
                // ACTIVE service
                sd.setStatusText(daysUntil < 0 ? Math.abs(daysUntil) + " days overdue" : "Due in " + daysUntil + " days");
                sd.setPendingAction(null);
            }

            return sd;
        }).collect(Collectors.toList());

        dash.setServices(serviceDTOs);

        // all events for customer
        List<DunningEventDTO> events = eventService.getEventsForCustomer(c.getCustomerId());
        dash.setEvents(events);

        // available actions
        List<CuringActionDTO> actions = actionService.getAvailableActionsForCustomer(c.getCustomerId());
        dash.setAvailableActions(actions);

        return dash;
    }
}
