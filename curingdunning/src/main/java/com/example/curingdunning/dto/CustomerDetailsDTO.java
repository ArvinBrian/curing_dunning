package com.example.curingdunning.dto;

import lombok.Data;
import java.util.List;

@Data
public class CustomerDetailsDTO {

    private Long customerId;
    private String name;
    private String email;
    private String phone;
    private String status;

    // Use the DTOs for nested lists to prevent recursion
    private List<SubscriptionDTO> subscriptions;
    private List<DunningEventDTO> dunningEvents;
}
