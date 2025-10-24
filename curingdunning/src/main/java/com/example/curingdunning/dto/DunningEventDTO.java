package com.example.curingdunning.dto;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DunningEventDTO {
    private Long eventId;
    private String serviceName;
    private int daysOverdue;
    private String status;
    private LocalDateTime createdAt;
}