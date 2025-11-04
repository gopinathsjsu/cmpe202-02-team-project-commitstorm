package com.campus.marketplace.service;

import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.ListingRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for processing conversational search queries using ChatGPT
 */
@Service
@Transactional
public class ChatbotSearchService {
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Value("${openai.api-key}")
    private String apiKey;
    
    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;
    
    @Value("${openai.temperature:0.3}")
    private Double temperature;
    
    /**
     * Process a conversational query and extract search terms using ChatGPT.
     * Falls back to keyword search if ChatGPT processing fails.
     * 
     * @param query natural language query (e.g., "textbook for CMPE 202?")
     * @return list of relevant listings
     */
    public ChatbotSearchResult processQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ChatbotSearchResult("", false);
        }
        
        String interpretedQuery = null;
        boolean usedFallback = false;
        
        // Try to use ChatGPT to interpret the query
        try {
            if (apiKey != null && !apiKey.isEmpty() && !apiKey.equals("your-api-key-here")) {
                System.out.println("DEBUG: Attempting to use ChatGPT with API key: " + (apiKey != null ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "null"));
                interpretedQuery = interpretQueryWithChatGPT(query);
                System.out.println("DEBUG: ChatGPT returned: " + interpretedQuery);
            } else {
                System.out.println("DEBUG: ChatGPT skipped - apiKey is: " + (apiKey == null ? "null" : apiKey.isEmpty() ? "empty" : apiKey));
            }
        } catch (Exception e) {
            // Log error and fall back to keyword search
            System.err.println("ChatGPT processing failed: " + e.getMessage());
            e.printStackTrace();
            usedFallback = true;
        }
        
        // If ChatGPT failed or wasn't configured, use fallback keyword extraction
        if (interpretedQuery == null || interpretedQuery.trim().isEmpty()) {
            interpretedQuery = extractKeywords(query);
            usedFallback = true;
        }
        
        // Search listings using the interpreted query
        List<Listing> listings = listingRepository.findByTitleOrDescriptionContaining(
            interpretedQuery, interpretedQuery);
        
        // Filter to only active listings
        listings = listings.stream()
            .filter(l -> l.getStatus() == Listing.ListingStatus.ACTIVE)
            .collect(Collectors.toList());
        
        return new ChatbotSearchResult(interpretedQuery, listings, usedFallback);
    }
    
    /**
     * Use ChatGPT to interpret natural language query and extract search terms.
     */
    private String interpretQueryWithChatGPT(String query) {
        OpenAiService service = new OpenAiService(apiKey, Duration.ofSeconds(30));
        
        // Get available categories for context
        List<String> categoryNames = categoryRepository.findAll().stream()
            .map(c -> c.getName())
            .collect(Collectors.toList());
        
        String systemPrompt = String.format(
            "You are a search assistant for a campus marketplace. " +
            "Extract the most important search keywords from the user's query. " +
            "Focus on: product/item names, course codes (e.g., CMPE 202), book titles, categories, and specific terms. " +
            "Available categories: %s. " +
            "Return ONLY a comma-separated list of search keywords, nothing else. " +
            "If the query mentions a course code, include variations (e.g., CMPE202, CMPE 202). " +
            "Keep keywords concise (2-3 words max per keyword). " +
            "Example: 'textbook for CMPE 202?' -> 'CMPE 202, textbook, CMPE202'",
            String.join(", ", categoryNames)
        );
        
        ChatMessage systemMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt);
        ChatMessage userMessage = new ChatMessage(ChatMessageRole.USER.value(), query);
        
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
            .model(model)
            .messages(Arrays.asList(systemMessage, userMessage))
            .temperature(temperature)
            .maxTokens(100)
            .build();
        
        String response = service.createChatCompletion(chatCompletionRequest)
            .getChoices().get(0).getMessage().getContent();
        
        service.shutdownExecutor();
        
        // Clean up the response - take the first line and remove any extra formatting
        String cleaned = response.trim().split("\n")[0].trim();
        
        // If response looks good, use it; otherwise fall back
        if (cleaned.length() > 0 && cleaned.length() < 200) {
            // Combine keywords into a single search term (OR logic)
            // Take the first significant keyword for primary search
            String[] keywords = cleaned.split(",");
            if (keywords.length > 0) {
                return keywords[0].trim();
            }
            return cleaned;
        }
        
        throw new RuntimeException("Invalid ChatGPT response");
    }
    
    /**
     * Fallback keyword extraction from natural language query.
     * Removes common stop words and question words.
     */
    private String extractKeywords(String query) {
        if (query == null || query.isEmpty()) {
            return "";
        }
        
        // Remove question marks and common question words
        String cleaned = query.toLowerCase()
            .replace("?", "")
            .replace("do you have", "")
            .replace("do you", "")
            .replace("have", "")
            .replace("for", "")
            .replace("a ", "")
            .replace("an ", "")
            .replace("the ", "")
            .trim();
        
        // Extract course codes (e.g., CMPE 202, CS 146, etc.)
        String courseCode = extractCourseCode(query);
        if (courseCode != null && !courseCode.isEmpty()) {
            cleaned = courseCode + " " + cleaned;
        }
        
        // Split into words and take the most meaningful ones (non-stop words)
        String[] words = cleaned.split("\\s+");
        List<String> keywords = new ArrayList<>();
        List<String> stopWords = Arrays.asList("is", "are", "what", "where", "when", "who", "how", "can", "could", "would", "should", "will");
        
        for (String word : words) {
            if (!stopWords.contains(word) && word.length() > 2) {
                keywords.add(word);
            }
        }
        
        // Return up to 3 most important keywords combined
        int maxKeywords = Math.min(3, keywords.size());
        return String.join(" ", keywords.subList(0, maxKeywords));
    }
    
    /**
     * Extract course code patterns (e.g., "CMPE 202", "CS146", "MATH 151")
     */
    private String extractCourseCode(String query) {
        // Pattern: letters followed by numbers (with optional space)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "([A-Z]{2,5})\\s*(\\d{3})", 
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = pattern.matcher(query);
        
        if (matcher.find()) {
            String dept = matcher.group(1).toUpperCase();
            String num = matcher.group(2);
            return dept + " " + num;
        }
        
        return null;
    }
    
    /**
     * Internal result class for the service.
     */
    public static class ChatbotSearchResult {
        private String interpretedQuery;
        private List<Listing> listings;
        private boolean usedFallback;
        
        public ChatbotSearchResult(String interpretedQuery, List<Listing> listings, boolean usedFallback) {
            this.interpretedQuery = interpretedQuery;
            this.listings = listings;
            this.usedFallback = usedFallback;
        }
        
        public ChatbotSearchResult(String interpretedQuery, boolean usedFallback) {
            this(interpretedQuery, new ArrayList<>(), usedFallback);
        }
        
        public String getInterpretedQuery() {
            return interpretedQuery;
        }
        
        public List<Listing> getListings() {
            return listings;
        }
        
        public boolean isUsedFallback() {
            return usedFallback;
        }
    }
}