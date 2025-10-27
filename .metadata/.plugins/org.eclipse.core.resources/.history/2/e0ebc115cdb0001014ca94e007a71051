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
        Customer c = customerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        DashboardDTO dash = new DashboardDTO();
        dash.setCustomerId(c.getCustomerId());
        dash.setEmail(c.getEmail());

        // services
        List<ServiceSubscription> subs = subRepo.findByCustomerCustomerId(c.getCustomerId());
        List<ServiceStatusDTO> serviceDTOs = subs.stream().map(s -> {
            ServiceStatusDTO sd = new ServiceStatusDTO();
            sd.setServiceName(s.getServiceName());
            sd.setNextDueDate(s.getNextDueDate());
            sd.setCurrentStatus(s.getStatus()); // ACTIVE / BLOCKED

            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), s.getNextDueDate());
            sd.setDaysUntilDue((int) daysUntil);

            if ("BLOCKED".equals(s.getStatus())) {
                sd.setStatusText("Service blocked due to overdue or limit reached");

                // check if there is a pending dunning event
                Optional<DunningEventDTO> pendingEvent = eventService.getEventsForCustomer(c.getCustomerId())
                        .stream()
                        .filter(e -> e.getServiceName().equals(s.getServiceName()) && "PENDING".equals(e.getStatus()))
                        .findFirst();

                if (pendingEvent.isPresent()) {
                    // find the rule that caused this event
                    List<DunningRule> rules = ruleRepo.findByServiceName(s.getServiceName());
                    rules = rules.stream()
                            .filter(r -> pendingEvent.get().getDaysOverdue() >= (r.getOverdueDays() != null ? r.getOverdueDays() : 0))
                            .sorted(Comparator.comparingInt(r -> r.getPriority() != null ? r.getPriority() : Integer.MAX_VALUE))
                            .collect(Collectors.toList());

                    if (!rules.isEmpty()) {
                        sd.setPendingAction(rules.get(0).getAction()); // recommended curing action
                    }
                }

            } else {
                sd.setStatusText(daysUntil < 0 ? Math.abs(daysUntil) + " days overdue" : "Due in " + daysUntil + " days");
                sd.setPendingAction(null);
            }

            return sd;
        }).collect(Collectors.toList());
        dash.setServices(serviceDTOs);

        // events
        List<DunningEventDTO> events = eventService.getEventsForCustomer(c.getCustomerId());
        dash.setEvents(events);

        // available actions: for each service collect actions
        List<CuringActionDTO> actions = actionService.getAvailableActionsForCustomer(c.getCustomerId());
        dash.setAvailableActions(actions);

        return dash;
    }
}
