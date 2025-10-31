package com.example.curingdunning.dto;

import com.example.curingdunning.entity.PlanType;
import lombok.Data;
import java.time.LocalTime;

@Data
public class AdminDunningRuleDTO {
    private Long id;
    private String serviceName;
    private Integer overdueDays;
    private String action;
    private Integer priority;
    private LocalTime timeOfDay;
    private PlanType planType;
    private String description;
}