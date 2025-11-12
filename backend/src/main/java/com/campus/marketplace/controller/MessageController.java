package com.campus.marketplace.controller;

import com.campus.marketplace.dto.CreateMessageRequest;
import com.campus.marketplace.dto.MessageDTO;
import com.campus.marketplace.service.MessageService;
import com.campus.marketplace.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
@Tag(name = "Messages", description = "Messaging API endpoints")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private com.campus.marketplace.service.UserService userService;
    
    // Helper method to get user ID from JWT token
    private String getUserIdFromToken(String authHeader) {
        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);
        return userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email))
                .getId();
    }
    
    // Send a message
    @PostMapping
    @Operation(summary = "Send a message", description = "Send a message to another user about a listing")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> sendMessage(
            @Valid @RequestBody CreateMessageRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String fromUserId = getUserIdFromToken(authHeader);
            
            MessageDTO message = messageService.sendMessage(request, fromUserId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get message by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get message by ID", description = "Get a specific message by its ID")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getMessageById(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = getUserIdFromToken(authHeader);
            
            MessageDTO message = messageService.getMessageById(id, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get messages for a listing
    @GetMapping("/listing/{listingId}")
    @Operation(summary = "Get messages for a listing", description = "Get all messages related to a specific listing")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getMessagesForListing(
            @PathVariable String listingId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = getUserIdFromToken(authHeader);
            
            List<MessageDTO> messages = messageService.getMessagesForListing(listingId, userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get conversation between two users
    @GetMapping("/conversation/{userId1}/{userId2}")
    @Operation(summary = "Get conversation between users", description = "Get conversation between two specific users")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getConversationBetweenUsers(
            @PathVariable String userId1,
            @PathVariable String userId2,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            List<MessageDTO> messages = messageService.getConversationBetweenUsers(userId1, userId2, currentUserId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get conversation for a specific listing between two users
    @GetMapping("/conversation/listing/{listingId}/{userId1}/{userId2}")
    @Operation(summary = "Get conversation for listing", description = "Get conversation between two users about a specific listing")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getConversationForListing(
            @PathVariable String listingId,
            @PathVariable String userId1,
            @PathVariable String userId2,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            List<MessageDTO> messages = messageService.getConversationForListing(listingId, userId1, userId2, currentUserId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get messages sent by user
    @GetMapping("/sent/{userId}")
    @Operation(summary = "Get messages sent by user", description = "Get all messages sent by a specific user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getMessagesSentByUser(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            // Users can only view their own sent messages
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You can only view your own sent messages");
            }
            
            if (page >= 0 && size > 0) {
                Page<MessageDTO> messages = messageService.getMessagesSentByUser(userId, page, size);
                return ResponseEntity.ok(messages);
            } else {
                List<MessageDTO> messages = messageService.getMessagesSentByUser(userId);
                return ResponseEntity.ok(messages);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get messages received by user
    @GetMapping("/received/{userId}")
    @Operation(summary = "Get messages received by user", description = "Get all messages received by a specific user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getMessagesReceivedByUser(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            // Users can only view their own received messages
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You can only view your own received messages");
            }
            
            if (page >= 0 && size > 0) {
                Page<MessageDTO> messages = messageService.getMessagesReceivedByUser(userId, page, size);
                return ResponseEntity.ok(messages);
            } else {
                List<MessageDTO> messages = messageService.getMessagesReceivedByUser(userId);
                return ResponseEntity.ok(messages);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get all messages for a user (sent and received)
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all messages for user", description = "Get all messages (sent and received) for a specific user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getAllMessagesForUser(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            // Users can only view their own messages
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You can only view your own messages");
            }
            
            if (page >= 0 && size > 0) {
                Page<MessageDTO> messages = messageService.getAllMessagesForUser(userId, page, size);
                return ResponseEntity.ok(messages);
            } else {
                List<MessageDTO> messages = messageService.getAllMessagesForUser(userId);
                return ResponseEntity.ok(messages);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get conversation partners for a user
    @GetMapping("/partners/{userId}")
    @Operation(summary = "Get conversation partners", description = "Get list of users that the specified user has conversations with")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getConversationPartners(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            // Users can only view their own conversation partners
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You can only view your own conversation partners");
            }
            
            List<String> partners = messageService.getConversationPartners(userId);
            return ResponseEntity.ok(partners);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Delete message
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete message", description = "Delete a message (only sender can delete)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> deleteMessage(
            @PathVariable String id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = getUserIdFromToken(authHeader);
            
            messageService.deleteMessage(id, userId);
            return ResponseEntity.ok("Message deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get unread message count
    @GetMapping("/unread/count/{userId}")
    @Operation(summary = "Get unread message count", description = "Get the count of unread messages for a user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getUnreadMessageCount(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            // Users can only view their own unread count
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You can only view your own unread count");
            }
            
            Long count = messageService.getUnreadMessageCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Get unread messages
    @GetMapping("/unread/{userId}")
    @Operation(summary = "Get unread messages", description = "Get all unread messages for a user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getUnreadMessages(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            // Users can only view their own unread messages
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You can only view your own unread messages");
            }
            
            List<MessageDTO> messages = messageService.getUnreadMessages(userId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Mark message as read
    @PatchMapping("/{messageId}/mark-read")
    @Operation(summary = "Mark message as read", description = "Mark a specific message as read")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> markMessageAsRead(
            @PathVariable String messageId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String userId = getUserIdFromToken(authHeader);
            
            MessageDTO message = messageService.markMessageAsRead(messageId, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    // Mark all messages as read
    @PatchMapping("/mark-all-read/{userId}")
    @Operation(summary = "Mark all messages as read", description = "Mark all unread messages as read for a user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> markAllMessagesAsRead(
            @PathVariable String userId,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String currentUserId = getUserIdFromToken(authHeader);
            
            // Users can only mark their own messages as read
            if (!currentUserId.equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: You can only mark your own messages as read");
            }
            
            messageService.markAllMessagesAsRead(userId);
            return ResponseEntity.ok("All messages marked as read");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}