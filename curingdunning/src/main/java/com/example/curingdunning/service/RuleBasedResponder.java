package com.example.curingdunning.service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.curingdunning.dto.ChatResponse;
import com.example.curingdunning.entity.Bill;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.entity.ServiceSubscription;
import com.example.curingdunning.repository.BillRepository;
import com.example.curingdunning.repository.CustomerRepository;
import com.example.curingdunning.repository.ServiceSubscriptionRepository;

@Service
public class RuleBasedResponder {
	@Autowired
    private  BillRepository billRepository;
	
	@Autowired
    private  CustomerRepository customerRepository;
	
	@Autowired
    private  ServiceSubscriptionRepository serviceSubscriptionRepository;

//    public RuleBasedResponder(BillRepository billRepository, CustomerRepository customerRepository) {
//        this.billRepository = billRepository;
//        this.customerRepository = customerRepository;
//    }

    public ChatResponse handleOption(Long customerId, int option) {
        switch (option) {
            case 1:
                return currentBills(customerId);
            case 2:
                return currentPlan(customerId);
            case 3:
                return totalDue(customerId);
            case 4:
                return upcomingOrOverdue(customerId);
            default:
                return new ChatResponse("Invalid option. Please enter a number between 1 and 5.", false, false);
        }
    }

    private ChatResponse currentBills(Long customerId) {
        List<Bill> bills = billRepository.findCurrentBillsByCustomerId(customerId);
        if (bills == null || bills.isEmpty()) {
            return new ChatResponse("You have no current bills.", false, true);
        }
        String msg = "Your current bills: " + bills.stream()
                .map(b -> b.getDescription() + " ₹" + b.getAmount())
                .collect(Collectors.joining(", "));
        // After rule response, show menu again (frontend will show menu) — we include endConversation=false
        return new ChatResponse(msg, false, false);
    }

    private ChatResponse currentPlan(Long customerId) {
        Optional<Customer> optCustomer = customerRepository.findById(customerId);
        if (optCustomer.isEmpty()) {
            return new ChatResponse("Could not find your plan information.", false, false);
        }

        Customer customer = optCustomer.get();
        List<ServiceSubscription> subscriptions = serviceSubscriptionRepository.findByCustomerCustomerId(customerId);

        if (subscriptions == null || subscriptions.isEmpty()) {
            return new ChatResponse("No active service subscriptions found for your account.", false, false);
        }

        // If multiple, pick the most recent or active one
        ServiceSubscription activeSub = subscriptions.stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()))
                .findFirst()
                .orElse(subscriptions.get(0));

        String msg = String.format(
            "You are subscribed to the %s plan (%s). Status: %s. Your next due date is %s, with a due amount of ₹%.2f.",
            activeSub.getServiceName(),
            activeSub.getPlanType(),
            activeSub.getStatus(),
            activeSub.getNextDueDate(),
            activeSub.getDueAmount()
        );

        return new ChatResponse(msg, false, false);
    }

    private ChatResponse totalDue(Long customerId) {
        double total = billRepository.getTotalDueForCustomer(customerId);
        return new ChatResponse(String.format("Your total due is ₹%.2f.", total), false, false);
    }

    private ChatResponse upcomingOrOverdue(Long customerId) {
        List<Bill> bills = billRepository.findByCustomerCustomerId(customerId);

        if (bills == null || bills.isEmpty()) {
            return new ChatResponse("You have no bills at the moment.", false, false);
        }

        // Filter only upcoming (PENDING) or overdue bills
        List<Bill> filtered = bills.stream()
                .filter(b -> "PENDING".equalsIgnoreCase(b.getStatus()) || "OVERDUE".equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return new ChatResponse("You have no upcoming or overdue bills.", false, false);
        }

        // Format bill info neatly
        String message = filtered.stream()
                .map(b -> {
                    String status = "OVERDUE".equalsIgnoreCase(b.getStatus()) ? " (OVERDUE)" : "";
                    return String.format("%s: ₹%.2f due on %s%s",
                            b.getServiceName(),
                            b.getAmount(),
                            b.getDueDate().toLocalDate(),
                            status);
                })
                .collect(Collectors.joining("; "));

        return new ChatResponse("Upcoming or Overdue Bills → " + message, false, false);
    }
}
