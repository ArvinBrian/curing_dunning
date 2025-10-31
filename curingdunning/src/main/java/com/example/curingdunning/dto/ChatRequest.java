package com.example.curingdunning.dto;

public class ChatRequest {
    private Long customerId;
    private String input; // "1", "2", "5", or free-text query

    public ChatRequest() {}
    public ChatRequest(Long customerId, String input) {
        this.customerId = customerId;
        this.input = input;
    }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
}

