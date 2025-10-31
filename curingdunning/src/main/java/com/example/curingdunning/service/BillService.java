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
}
