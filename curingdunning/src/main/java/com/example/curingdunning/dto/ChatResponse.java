package com.example.curingdunning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String message;
    private boolean optionsVisible;
    private boolean isEnd;
}
