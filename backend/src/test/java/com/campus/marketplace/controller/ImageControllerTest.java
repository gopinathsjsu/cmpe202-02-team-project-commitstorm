package com.campus.marketplace.controller;

import com.campus.marketplace.dto.PresignedUrlRequest;
import com.campus.marketplace.service.S3Service;
import com.campus.marketplace.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ImageControllerTest {
    
    @Mock
    private S3Service s3Service;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private ImageController imageController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private PresignedUrlRequest presignedUrlRequest;
    private S3Service.PresignedUrlResult s3Result;
    private String authToken;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
        objectMapper = new ObjectMapper();
        
        presignedUrlRequest = new PresignedUrlRequest("test.jpg", "image/jpeg");
        
        s3Result = new S3Service.PresignedUrlResult(
            "https://presigned-url.com/upload",
            "listings/test-uuid.jpg",
            "https://bucket.s3.region.amazonaws.com/listings/test-uuid.jpg"
        );
        
        authToken = "Bearer valid-jwt-token";
    }
    
    @Test
    void testGeneratePresignedUrl_Success() throws Exception {
        // Arrange
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        when(s3Service.generatePresignedUploadUrl("test.jpg", "image/jpeg")).thenReturn(s3Result);
        
        // Act & Assert
        mockMvc.perform(post("/api/images/presigned-url")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(presignedUrlRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.presignedUrl").exists())
                .andExpect(jsonPath("$.objectKey").exists())
                .andExpect(jsonPath("$.publicUrl").exists());
        
        verify(jwtUtil).validateToken("valid-jwt-token");
        verify(s3Service).generatePresignedUploadUrl("test.jpg", "image/jpeg");
    }
    
    @Test
    void testGeneratePresignedUrl_InvalidToken() throws Exception {
        // Arrange
        when(jwtUtil.validateToken("invalid-token")).thenReturn(false);
        
        // Act & Assert
        mockMvc.perform(post("/api/images/presigned-url")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(presignedUrlRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid or expired token"));
        
        verify(jwtUtil).validateToken("invalid-token");
        verify(s3Service, never()).generatePresignedUploadUrl(anyString(), anyString());
    }
    
    @Test
    void testGeneratePresignedUrl_ServiceException() throws Exception {
        // Arrange
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        when(s3Service.generatePresignedUploadUrl(anyString(), anyString()))
                .thenThrow(new RuntimeException("S3 bucket name not configured"));
        
        // Act & Assert
        mockMvc.perform(post("/api/images/presigned-url")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(presignedUrlRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: S3 bucket name not configured"));
    }
    
    @Test
    void testGenerateBatchPresignedUrls_Success() throws Exception {
        // Arrange
        PresignedUrlRequest[] requests = new PresignedUrlRequest[]{
            new PresignedUrlRequest("test1.jpg", "image/jpeg"),
            new PresignedUrlRequest("test2.jpg", "image/jpeg")
        };
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        when(s3Service.generatePresignedUploadUrl(anyString(), anyString())).thenReturn(s3Result);
        
        // Act & Assert
        mockMvc.perform(post("/api/images/presigned-url/batch")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
        
        verify(s3Service, times(2)).generatePresignedUploadUrl(anyString(), anyString());
    }
    
    @Test
    void testGenerateBatchPresignedUrls_TooManyFiles() throws Exception {
        // Arrange
        PresignedUrlRequest[] requests = new PresignedUrlRequest[11]; // Exceeds limit of 10
        for (int i = 0; i < 11; i++) {
            requests[i] = new PresignedUrlRequest("test" + i + ".jpg", "image/jpeg");
        }
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        
        // Act & Assert
        mockMvc.perform(post("/api/images/presigned-url/batch")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Maximum 10 images per batch"));
        
        verify(s3Service, never()).generatePresignedUploadUrl(anyString(), anyString());
    }
    
    @Test
    void testDeleteImage_Success() throws Exception {
        // Arrange
        String imageUrl = "https://bucket.s3.region.amazonaws.com/listings/test-image.jpg";
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        when(s3Service.extractObjectKeyFromUrl(imageUrl)).thenReturn("listings/test-image.jpg");
        doNothing().when(s3Service).deleteImage("listings/test-image.jpg");
        
        // Act & Assert
        mockMvc.perform(delete("/api/images")
                .header("Authorization", authToken)
                .param("imageUrl", imageUrl))
                .andExpect(status().isOk())
                .andExpect(content().string("Image deleted successfully"));
        
        verify(s3Service).extractObjectKeyFromUrl(imageUrl);
        verify(s3Service).deleteImage("listings/test-image.jpg");
    }
    
    @Test
    void testDeleteImage_InvalidUrl() throws Exception {
        // Arrange
        String invalidUrl = "invalid-url";
        when(jwtUtil.validateToken("valid-jwt-token")).thenReturn(true);
        when(s3Service.extractObjectKeyFromUrl(invalidUrl)).thenReturn(null);
        
        // Act & Assert
        mockMvc.perform(delete("/api/images")
                .header("Authorization", authToken)
                .param("imageUrl", invalidUrl))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid image URL"));
        
        verify(s3Service).extractObjectKeyFromUrl(invalidUrl);
        verify(s3Service, never()).deleteImage(anyString());
    }
}