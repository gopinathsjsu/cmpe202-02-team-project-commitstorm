package com.campus.marketplace.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

import java.time.Duration;
import java.util.UUID;

/**
 * Service for S3 operations including presigned URL generation for image uploads.
 */
@Service
public class S3Service {
    
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    
    @Value("${aws.s3.bucket-name:}")
    private String bucketName;
    
    @Value("${aws.s3.region:us-west-2}")
    private String region;
    
    @Value("${aws.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;
    
    @Value("${aws.s3.presigned-url-expiry-minutes:15}")
    private int presignedUrlExpiryMinutes;
    
    /**
     * Generate a presigned URL for uploading an image to S3.
     * 
     * @param fileName Original file name (for extension detection)
     * @param contentType MIME type of the image (e.g., "image/jpeg")
     * @return Presigned URL and object key
     */
    public PresignedUrlResult generatePresignedUploadUrl(String fileName, String contentType) {
        if (bucketName == null || bucketName.isEmpty()) {
            throw new RuntimeException("S3 bucket name not configured");
        }
        
        // Validate content type
        if (!isValidImageType(contentType)) {
            throw new RuntimeException("Invalid image type. Allowed types: image/jpeg, image/png, image/webp, image/gif");
        }
        
        // Generate unique object key
        String extension = getFileExtension(fileName);
        String objectKey = "listings/" + UUID.randomUUID().toString() + extension;
        
        try {
            S3Presigner presigner = createS3Presigner();
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    // Note: ACL is set after upload since bucket may have ACLs disabled
                    .build();
            
            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                    .putObjectRequest(putObjectRequest)
                    .build();
            
            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();
            
            presigner.close();
            
            logger.info("Generated presigned URL for object: {}", objectKey);
            
            // Note: After upload, call makeObjectPublic() to set public-read ACL
            // if the bucket supports ACLs, or configure bucket policy for public access
            
            return new PresignedUrlResult(presignedUrl, objectKey, getPublicUrl(objectKey));
            
        } catch (Exception e) {
            logger.error("Error generating presigned URL", e);
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage());
        }
    }
    
    /**
     * Internal result class for presigned URL generation.
     */
    public static class PresignedUrlResult {
        private String presignedUrl;
        private String objectKey;
        private String publicUrl;
        
        public PresignedUrlResult(String presignedUrl, String objectKey, String publicUrl) {
            this.presignedUrl = presignedUrl;
            this.objectKey = objectKey;
            this.publicUrl = publicUrl;
        }
        
        public String getPresignedUrl() {
            return presignedUrl;
        }
        
        public String getObjectKey() {
            return objectKey;
        }
        
        public String getPublicUrl() {
            return publicUrl;
        }
    }
    
    /**
     * Get public URL for an S3 object (for storing in database).
     * 
     * @param objectKey S3 object key
     * @return Public URL
     */
    public String getPublicUrl(String objectKey) {
        if (bucketName == null || bucketName.isEmpty()) {
            return null;
        }
        // Format: https://bucket-name.s3.region.amazonaws.com/object-key
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);
    }
    
    /**
     * Set an object's ACL to public-read (for existing objects that were uploaded without public access).
     * 
     * @param objectKey S3 object key
     */
    public void makeObjectPublic(String objectKey) {
        if (bucketName == null || bucketName.isEmpty()) {
            return;
        }
        
        try {
            S3Client s3Client = createS3Client();
            s3Client.putObjectAcl(b -> b
                .bucket(bucketName)
                .key(objectKey)
                .acl(ObjectCannedACL.PUBLIC_READ)
            );
            s3Client.close();
            logger.info("Set public-read ACL for S3 object: {}", objectKey);
        } catch (Exception e) {
            logger.error("Error setting ACL for S3 object: {}", objectKey, e);
        }
    }
    
    /**
     * Delete an image from S3.
     * 
     * @param objectKey S3 object key
     */
    public void deleteImage(String objectKey) {
        if (bucketName == null || bucketName.isEmpty()) {
            return;
        }
        
        try {
            S3Client s3Client = createS3Client();
            s3Client.deleteObject(b -> b.bucket(bucketName).key(objectKey));
            s3Client.close();
            logger.info("Deleted S3 object: {}", objectKey);
        } catch (Exception e) {
            logger.error("Error deleting S3 object: {}", objectKey, e);
            // Don't throw - deletion failures shouldn't break the flow
        }
    }
    
    /**
     * Extract object key from public URL.
     * 
     * @param publicUrl Public S3 URL
     * @return Object key or null if invalid
     */
    public String extractObjectKeyFromUrl(String publicUrl) {
        if (publicUrl == null || !publicUrl.contains(bucketName)) {
            return null;
        }
        // Extract key from URL like: https://bucket.s3.region.amazonaws.com/listings/uuid.jpg
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        if (publicUrl.startsWith(prefix)) {
            return publicUrl.substring(prefix.length());
        }
        return null;
    }
    
    private S3Presigner createS3Presigner() {
        if (accessKeyId != null && !accessKeyId.isEmpty() && 
            secretAccessKey != null && !secretAccessKey.isEmpty()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            return S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
        } else {
            // Use default credentials provider (IAM role, environment variables, etc.)
            return S3Presigner.builder()
                    .region(Region.of(region))
                    .build();
        }
    }
    
    private S3Client createS3Client() {
        if (accessKeyId != null && !accessKeyId.isEmpty() && 
            secretAccessKey != null && !secretAccessKey.isEmpty()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .httpClient(ApacheHttpClient.builder()
                            .connectionTimeout(Duration.ofSeconds(10))
                            .build())
                    .build();
        } else {
            // Use default credentials provider
            return S3Client.builder()
                    .region(Region.of(region))
                    .httpClient(ApacheHttpClient.builder()
                            .connectionTimeout(Duration.ofSeconds(10))
                            .build())
                    .build();
        }
    }
    
    private boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/jpg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/webp") ||
               contentType.equals("image/gif");
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".jpg"; // Default extension
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0 && lastDot < fileName.length() - 1) {
            return fileName.substring(lastDot);
        }
        return ".jpg"; // Default extension
    }
    
}
