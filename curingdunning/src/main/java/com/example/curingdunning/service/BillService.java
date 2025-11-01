package com.example.curingdunning.service;

import java.time.LocalDate;
import java.util.List;

import com.example.curingdunning.dto.BillDTO;
import com.example.curingdunning.entity.Bill;
import com.example.curingdunning.entity.Customer;
import com.example.curingdunning.entity.ServiceSubscription;

public interface BillService {
    Bill createBill(Bill bill);
    Bill getBillById(Long id);
    List<Bill> getBillsByCustomer(Long customerId);
    List<Bill> getAllBills();
//    Bill markBillAsPaid(Long billId);
    List<BillDTO> getCurrentBills(Long customerId);
    List<BillDTO> getPastBills(Long customerId);
    
    BillDTO toDTO(Bill bill);
	Bill markBillAsPaid(Bill bill);
	// BillService.java (Interface or Class)

	// Add this new public method signature
	public void generateUpcomingPostpaidBill(Customer customer, ServiceSubscription subscription, LocalDate dueDate);
    boolean billExists(Customer customer, ServiceSubscription sub, LocalDate dueDate);
    void generateUpcomingPrepaidBill(Customer customer, ServiceSubscription sub, LocalDate dueDate);

    //methods for mock bill payment:
    /**
     * Retrieves the associated Dunning Event ID for a given Bill.
     * Required for linking payment to curing action.
     */
    Long getDunningEventIdForBill(Long billId);

    /**
     * Marks a bill as paid, accepting a Long billId as argument.
     * This replaces your existing markBillAsPaid method signature if it existed.
     */
    void markBillAsPaid(Long billId);
}
