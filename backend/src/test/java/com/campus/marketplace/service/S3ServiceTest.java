package com.campus.marketplace.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class S3ServiceTest {
    
    @InjectMocks
    private S3Service s3Service;
    
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "region", "us-west-2");
        ReflectionTestUtils.setField(s3Service, "accessKeyId", "test-access-key");
        ReflectionTestUtils.setField(s3Service, "secretAccessKey", "test-secret-key");
        ReflectionTestUtils.setField(s3Service, "presignedUrlExpiryMinutes", 15);
    }
    
    @Test
    void testGeneratePresignedUploadUrl_BucketNotConfigured() {
        // Arrange
        ReflectionTestUtils.setField(s3Service, "bucketName", "");
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            s3Service.generatePresignedUploadUrl("test.jpg", "image/jpeg");
        });
        
        assertTrue(exception.getMessage().contains("S3 bucket name not configured"));
    }
    
    @Test
    void testGeneratePresignedUploadUrl_InvalidImageType() {
        // Arrange
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            s3Service.generatePresignedUploadUrl("test.pdf", "application/pdf");
        });
        
        assertTrue(exception.getMessage().contains("Invalid image type"));
    }
    
    @Test
    void testGetPublicUrl() {
        // Arrange
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "region", "us-west-2");
        
        // Act
        String publicUrl = s3Service.getPublicUrl("listings/test-image.jpg");
        
        // Assert
        assertNotNull(publicUrl);
        assertTrue(publicUrl.contains("test-bucket"));
        assertTrue(publicUrl.contains("listings/test-image.jpg"));
    }
    
    @Test
    void testGetPublicUrl_BucketNotConfigured() {
        // Arrange
        ReflectionTestUtils.setField(s3Service, "bucketName", "");
        
        // Act
        String publicUrl = s3Service.getPublicUrl("listings/test-image.jpg");
        
        // Assert
        assertNull(publicUrl);
    }
    
    @Test
    void testExtractObjectKeyFromUrl_ValidUrl() {
        // Arrange
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(s3Service, "region", "us-west-2");
        String publicUrl = "https://test-bucket.s3.us-west-2.amazonaws.com/listings/test-image.jpg";
        
        // Act
        String objectKey = s3Service.extractObjectKeyFromUrl(publicUrl);
        
        // Assert
        assertEquals("listings/test-image.jpg", objectKey);
    }
    
    @Test
    void testExtractObjectKeyFromUrl_InvalidUrl() {
        // Arrange
        ReflectionTestUtils.setField(s3Service, "bucketName", "test-bucket");
        String invalidUrl = "https://other-bucket.s3.us-west-2.amazonaws.com/listings/test-image.jpg";
        
        // Act
        String objectKey = s3Service.extractObjectKeyFromUrl(invalidUrl);
        
        // Assert
        assertNull(objectKey);
    }
    
    @Test
    void testExtractObjectKeyFromUrl_NullUrl() {
        // Act
        String objectKey = s3Service.extractObjectKeyFromUrl(null);
        
        // Assert
        assertNull(objectKey);
    }
    
    @Test
    void testDeleteImage_BucketNotConfigured() {
        // Arrange
        ReflectionTestUtils.setField(s3Service, "bucketName", "");
        
        // Act - Should not throw
        assertDoesNotThrow(() -> {
            s3Service.deleteImage("listings/test-image.jpg");
        });
    }
    
    // Note: Testing actual AWS SDK interactions (presigned URL generation, S3 client operations)
    // would require mocking the AWS SDK clients, which is complex. The above tests cover
    // the validation and utility methods. Integration tests would be needed for full AWS testing.
}