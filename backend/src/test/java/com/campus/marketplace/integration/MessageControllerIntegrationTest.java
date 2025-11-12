package com.campus.marketplace.integration;

import com.campus.marketplace.entity.Category;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.Message;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.CategoryRepository;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.MessageRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.MessageService;
import com.campus.marketplace.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MessageController endpoints.
 */
@AutoConfigureMockMvc
@Transactional
public class MessageControllerIntegrationTest extends IntegrationTestBase {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User sender;
    private User receiver;
    private Category category;
    private Listing listing;
    private String senderToken;
    private String receiverToken;
    
    @BeforeEach
    void setUp() {
        // Create sender
        sender = new User();
        sender.setName("Test Sender");
        sender.setEmail("sender@test.com");
        sender.setPassword(passwordEncoder.encode("password123"));
        sender.setRole(User.UserRole.USER);
        sender.setStatus(User.UserStatus.ACTIVE);
        sender = userRepository.save(sender);
        senderToken = jwtUtil.generateToken(sender.getEmail());
        
        // Create receiver
        receiver = new User();
        receiver.setName("Test Receiver");
        receiver.setEmail("receiver@test.com");
        receiver.setPassword(passwordEncoder.encode("password123"));
        receiver.setRole(User.UserRole.USER);
        receiver.setStatus(User.UserStatus.ACTIVE);
        receiver = userRepository.save(receiver);
        receiverToken = jwtUtil.generateToken(receiver.getEmail());
        
        // Create category
        category = new Category();
        category.setName("Electronics");
        category = categoryRepository.save(category);
        
        // Create listing
        listing = new Listing();
        listing.setTitle("Test Item");
        listing.setDescription("Test Description");
        listing.setPrice(new BigDecimal("99.99"));
        listing.setCondition(Listing.ItemCondition.NEW);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setSeller(receiver);
        listing.setCategory(category);
        listing = listingRepository.save(listing);
    }
    
    @Test
    void testSendMessage_Success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new CreateMessageRequest(
            listing.getId(), receiver.getId(), "Hello, is this still available?"
        ));
        
        mockMvc.perform(post("/api/messages")
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Hello, is this still available?"))
                .andExpect(jsonPath("$.fromUserId").value(sender.getId()))
                .andExpect(jsonPath("$.toUserId").value(receiver.getId()));
    }
    
    @Test
    void testSendMessage_ToSelf() throws Exception {
        String requestBody = objectMapper.writeValueAsString(new CreateMessageRequest(
            listing.getId(), sender.getId(), "Message to self"
        ));
        
        mockMvc.perform(post("/api/messages")
                .header("Authorization", "Bearer " + senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void testGetMessagesForListing() throws Exception {
        // Create a message first
        Message message = new Message();
        message.setListing(listing);
        message.setFromUser(sender);
        message.setToUser(receiver);
        message.setContent("Test message");
        messageRepository.save(message);
        
        mockMvc.perform(get("/api/messages/listing/{listingId}", listing.getId())
                .header("Authorization", "Bearer " + senderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].content").exists());
    }
    
    @Test
    void testGetUnreadMessageCount() throws Exception {
        // Create an unread message
        Message message = new Message();
        message.setListing(listing);
        message.setFromUser(sender);
        message.setToUser(receiver);
        message.setContent("Unread message");
        message.setIsRead(false);
        messageRepository.save(message);
        
        mockMvc.perform(get("/api/messages/unread/count/{userId}", receiver.getId())
                .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }
    
    @Test
    void testGetUnreadMessages() throws Exception {
        // Create an unread message
        Message message = new Message();
        message.setListing(listing);
        message.setFromUser(sender);
        message.setToUser(receiver);
        message.setContent("Unread message");
        message.setIsRead(false);
        messageRepository.save(message);
        
        mockMvc.perform(get("/api/messages/unread/{userId}", receiver.getId())
                .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].isRead").value(false));
    }
    
    @Test
    void testMarkMessageAsRead() throws Exception {
        // Create an unread message
        Message message = new Message();
        message.setListing(listing);
        message.setFromUser(sender);
        message.setToUser(receiver);
        message.setContent("Unread message");
        message.setIsRead(false);
        message = messageRepository.save(message);
        
        mockMvc.perform(patch("/api/messages/{messageId}/mark-read", message.getId())
                .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isRead").value(true));
        
        // Verify message is marked as read
        Message updatedMessage = messageRepository.findById(message.getId()).orElseThrow();
        assertTrue(updatedMessage.getIsRead());
    }
    
    @Test
    void testMarkAllMessagesAsRead() throws Exception {
        // Create multiple unread messages
        for (int i = 0; i < 3; i++) {
            Message message = new Message();
            message.setListing(listing);
            message.setFromUser(sender);
            message.setToUser(receiver);
            message.setContent("Unread message " + i);
            message.setIsRead(false);
            messageRepository.save(message);
        }
        
        mockMvc.perform(patch("/api/messages/mark-all-read/{userId}", receiver.getId())
                .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isOk());
        
        // Verify all messages are marked as read
        Long unreadCount = messageService.getUnreadMessageCount(receiver.getId());
        assertEquals(0, unreadCount);
    }
    
    @Test
    void testGetConversationBetweenUsers() throws Exception {
        // Create messages between users
        Message message1 = new Message();
        message1.setListing(listing);
        message1.setFromUser(sender);
        message1.setToUser(receiver);
        message1.setContent("Message 1");
        messageRepository.save(message1);
        
        Message message2 = new Message();
        message2.setListing(listing);
        message2.setFromUser(receiver);
        message2.setToUser(sender);
        message2.setContent("Message 2");
        messageRepository.save(message2);
        
        mockMvc.perform(get("/api/messages/conversation/{userId1}/{userId2}", 
                sender.getId(), receiver.getId())
                .header("Authorization", "Bearer " + senderToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    void testDeleteMessage() throws Exception {
        // Create a message
        Message message = new Message();
        message.setListing(listing);
        message.setFromUser(sender);
        message.setToUser(receiver);
        message.setContent("Message to delete");
        message = messageRepository.save(message);
        
        mockMvc.perform(delete("/api/messages/{id}", message.getId())
                .header("Authorization", "Bearer " + senderToken))
                .andExpect(status().isOk());
        
        // Verify message is deleted
        assertFalse(messageRepository.existsById(message.getId()));
    }
    
    @Test
    void testDeleteMessage_NotSender() throws Exception {
        // Create a message
        Message message = new Message();
        message.setListing(listing);
        message.setFromUser(sender);
        message.setToUser(receiver);
        message.setContent("Message to delete");
        message = messageRepository.save(message);
        
        // Try to delete as receiver (not sender)
        mockMvc.perform(delete("/api/messages/{id}", message.getId())
                .header("Authorization", "Bearer " + receiverToken))
                .andExpect(status().isBadRequest());
    }
    
    // Helper class for request DTO
    @SuppressWarnings("unused")
    private static class CreateMessageRequest {
        public String listingId;
        public String toUserId;
        public String content;
        
        public CreateMessageRequest(String listingId, String toUserId, String content) {
            this.listingId = listingId;
            this.toUserId = toUserId;
            this.content = content;
        }
    }
}

