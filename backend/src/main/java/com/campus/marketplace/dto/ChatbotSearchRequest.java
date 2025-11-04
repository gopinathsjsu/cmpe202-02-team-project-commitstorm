package com.campus.marketplace.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for chatbot search requests
 */
public class ChatbotSearchRequest {
    
    @NotBlank(message = "Query is required")
    private String query;
    
    // Constructors
    public ChatbotSearchRequest() {}
    
    public ChatbotSearchRequest(String query) {
        this.query = query;
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
}
