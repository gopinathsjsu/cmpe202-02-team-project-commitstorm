package com.campus.marketplace.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling image JSON strings.
 * Converts between JSON string format and List<String> for easier manipulation.
 */
public class ImageUtil {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Parse JSON string of image URLs to List.
     * 
     * @param imagesJson JSON string like '["url1", "url2"]'
     * @return List of image URLs
     */
    public static List<String> parseImageUrls(String imagesJson) {
        if (imagesJson == null || imagesJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // If parsing fails, return empty list
            return new ArrayList<>();
        }
    }
    
    /**
     * Convert List of image URLs to JSON string.
     * 
     * @param imageUrls List of image URLs
     * @return JSON string
     */
    public static String toJsonString(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return "[]";
        }
        
        try {
            return objectMapper.writeValueAsString(imageUrls);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    /**
     * Add an image URL to existing JSON string.
     * 
     * @param imagesJson Existing JSON string
     * @param newImageUrl New image URL to add
     * @return Updated JSON string
     */
    public static String addImageUrl(String imagesJson, String newImageUrl) {
        List<String> urls = parseImageUrls(imagesJson);
        urls.add(newImageUrl);
        return toJsonString(urls);
    }
    
    /**
     * Remove an image URL from JSON string.
     * 
     * @param imagesJson Existing JSON string
     * @param imageUrlToRemove Image URL to remove
     * @return Updated JSON string
     */
    public static String removeImageUrl(String imagesJson, String imageUrlToRemove) {
        List<String> urls = parseImageUrls(imagesJson);
        urls.remove(imageUrlToRemove);
        return toJsonString(urls);
    }
    
    /**
     * Get first image URL from JSON string.
     * 
     * @param imagesJson JSON string
     * @return First image URL or null
     */
    public static String getFirstImageUrl(String imagesJson) {
        List<String> urls = parseImageUrls(imagesJson);
        return urls.isEmpty() ? null : urls.get(0);
    }
}

