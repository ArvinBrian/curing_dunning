package com.example.curingdunning.dto;

import com.example.curingdunning.entity.Customer;

public class CustomerDTO {
    private Long customerId;
    private String name;
    private String phone;
    private String email;
    private String status;

    // --- Constructor (This is correct) ---
    public CustomerDTO(Customer entity) {
        this.customerId = entity.getCustomerId();
        this.name = entity.getName();
        this.phone = entity.getPhone();
        this.email = entity.getEmail();
        // This is a great, safe way to handle the enum
        this.status = (entity.getStatus() != null) ? entity.getStatus().name() : null; 
    }

    // --- ⬇️ FIX: ADD THESE PUBLIC GETTERS ⬇️ ---
    // Jackson needs these to see the private fields
	public Long getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    // (No setters are needed for a DTO that is only used for reading)
}