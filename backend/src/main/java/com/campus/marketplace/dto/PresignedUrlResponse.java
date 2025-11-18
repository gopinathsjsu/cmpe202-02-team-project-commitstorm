package com.campus.marketplace.dto;

/**
 * Response DTO for presigned URL generation.
 */
public class PresignedUrlResponse {
    
    private String presignedUrl;
    private String objectKey;
    private String publicUrl;
    private int expiresInMinutes;
    
    public PresignedUrlResponse() {}
    
    public PresignedUrlResponse(String presignedUrl, String objectKey, String publicUrl, int expiresInMinutes) {
        this.presignedUrl = presignedUrl;
        this.objectKey = objectKey;
        this.publicUrl = publicUrl;
        this.expiresInMinutes = expiresInMinutes;
    }
    
    public String getPresignedUrl() {
        return presignedUrl;
    }
    
    public void setPresignedUrl(String presignedUrl) {
        this.presignedUrl = presignedUrl;
    }
    
    public String getObjectKey() {
        return objectKey;
    }
    
    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }
    
    public String getPublicUrl() {
        return publicUrl;
    }
    
    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }
    
    public int getExpiresInMinutes() {
        return expiresInMinutes;
    }
    
    public void setExpiresInMinutes(int expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }
}

