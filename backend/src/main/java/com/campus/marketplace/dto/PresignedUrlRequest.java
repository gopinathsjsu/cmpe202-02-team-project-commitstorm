package com.campus.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for generating presigned upload URL.
 */
public class PresignedUrlRequest {
    
    @NotBlank(message = "File name is required")
    private String fileName;
    
    @NotBlank(message = "Content type is required")
    @Pattern(regexp = "image/(jpeg|jpg|png|webp|gif)", 
            message = "Content type must be a valid image type (image/jpeg, image/png, image/webp, image/gif)")
    private String contentType;
    
    public PresignedUrlRequest() {}
    
    public PresignedUrlRequest(String fileName, String contentType) {
        this.fileName = fileName;
        this.contentType = contentType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

