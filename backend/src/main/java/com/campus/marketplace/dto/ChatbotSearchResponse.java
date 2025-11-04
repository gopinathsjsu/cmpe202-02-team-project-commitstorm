package com.campus.marketplace.dto;

import java.util.List;

/**
 * DTO for chatbot search responses
 */
public class ChatbotSearchResponse {
    
    private String interpretedQuery;
    private List<ListingDTO> results;
    private String message;
    private boolean usedFallback;
    
    // Constructors
    public ChatbotSearchResponse() {}
    
    public ChatbotSearchResponse(String interpretedQuery, List<ListingDTO> results, String message, boolean usedFallback) {
        this.interpretedQuery = interpretedQuery;
        this.results = results;
        this.message = message;
        this.usedFallback = usedFallback;
    }
    
    // Getters and Setters
    public String getInterpretedQuery() {
        return interpretedQuery;
    }
    
    public void setInterpretedQuery(String interpretedQuery) {
        this.interpretedQuery = interpretedQuery;
    }
    
    public List<ListingDTO> getResults() {
        return results;
    }
    
    public void setResults(List<ListingDTO> results) {
        this.results = results;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isUsedFallback() {
        return usedFallback;
    }
    
    public void setUsedFallback(boolean usedFallback) {
        this.usedFallback = usedFallback;
    }
}
