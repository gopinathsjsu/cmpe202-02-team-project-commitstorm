package com.campus.marketplace.controller;

import com.campus.marketplace.dto.PresignedUrlRequest;
import com.campus.marketplace.dto.PresignedUrlResponse;
import com.campus.marketplace.service.S3Service;
import com.campus.marketplace.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for image upload operations using S3 presigned URLs.
 */
@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
@Tag(name = "Images", description = "Image upload and management API")
public class ImageController {
    
    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${aws.s3.presigned-url-expiry-minutes:15}")
    private int presignedUrlExpiryMinutes;
    
    /**
     * Generate presigned URL for uploading an image.
     * 
     * Flow:
     * 1. Client calls this endpoint with file name and content type
     * 2. Backend generates presigned URL and returns it
     * 3. Client uploads directly to S3 using presigned URL
     * 4. Client stores the public URL in listing images field
     * 
     * @param request File name and content type
     * @param authHeader JWT token for authentication
     * @return Presigned URL and public URL
     */
    @PostMapping("/presigned-url")
    @Operation(
        summary = "Generate presigned URL for image upload",
        description = "Generate a presigned URL that allows direct upload to S3. " +
                      "Client should upload the image to the presigned URL, then use the publicUrl " +
                      "in the listing images field."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> generatePresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Verify authentication
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
            }
            
            // Generate presigned URL
            S3Service.PresignedUrlResult s3Result = s3Service.generatePresignedUploadUrl(
                request.getFileName(),
                request.getContentType()
            );
            
            PresignedUrlResponse response = new PresignedUrlResponse(
                s3Result.getPresignedUrl(),
                s3Result.getObjectKey(),
                s3Result.getPublicUrl(),
                presignedUrlExpiryMinutes
            );
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating presigned URL: " + e.getMessage());
        }
    }
    
    /**
     * Generate multiple presigned URLs for batch upload.
     * 
     * @param requests Array of file name and content type pairs
     * @param authHeader JWT token
     * @return Array of presigned URLs
     */
    @PostMapping("/presigned-url/batch")
    @Operation(
        summary = "Generate multiple presigned URLs",
        description = "Generate presigned URLs for multiple images at once"
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> generateBatchPresignedUrls(
            @Valid @RequestBody PresignedUrlRequest[] requests,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Verify authentication
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
            }
            
            // Limit batch size
            if (requests.length > 10) {
                return ResponseEntity.badRequest()
                    .body("Maximum 10 images per batch");
            }
            
            PresignedUrlResponse[] responses = new PresignedUrlResponse[requests.length];
            for (int i = 0; i < requests.length; i++) {
                S3Service.PresignedUrlResult s3Result = s3Service.generatePresignedUploadUrl(
                    requests[i].getFileName(),
                    requests[i].getContentType()
                );
                responses[i] = new PresignedUrlResponse(
                    s3Result.getPresignedUrl(),
                    s3Result.getObjectKey(),
                    s3Result.getPublicUrl(),
                    presignedUrlExpiryMinutes
                );
            }
            
            return ResponseEntity.ok(responses);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating presigned URLs: " + e.getMessage());
        }
    }
    
    /**
     * Delete an image from S3.
     * 
     * @param imageUrl Public S3 URL of the image to delete
     * @param authHeader JWT token
     * @return Success message
     */
    @DeleteMapping
    @Operation(
        summary = "Delete image from S3",
        description = "Delete an image from S3 using its public URL. " +
                      "Only the owner of the listing or admin can delete images."
    )
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> deleteImage(
            @RequestParam String imageUrl,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Verify authentication
            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired token");
            }
            
            String objectKey = s3Service.extractObjectKeyFromUrl(imageUrl);
            if (objectKey == null) {
                return ResponseEntity.badRequest()
                    .body("Invalid image URL");
            }
            
            s3Service.deleteImage(objectKey);
            
            return ResponseEntity.ok("Image deleted successfully");
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting image: " + e.getMessage());
        }
    }
}

