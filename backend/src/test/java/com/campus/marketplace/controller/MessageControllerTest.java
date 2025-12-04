package com.campus.marketplace.controller;

import com.campus.marketplace.dto.CreateMessageRequest;
import com.campus.marketplace.dto.MessageDTO;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.Message;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.MessageService;
import com.campus.marketplace.service.UserService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest {
    
    @Mock
    private MessageService messageService;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private MessageController messageController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private User testUser;
    private Message message;
    private MessageDTO messageDTO;
    private CreateMessageRequest createRequest;
    private String authToken;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
        objectMapper = new ObjectMapper();
        
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("user@example.com");
        
        Listing listing = new Listing();
        listing.setId("listing-123");
        
        User fromUser = new User();
        fromUser.setId("user-1");
        
        User toUser = new User();
        toUser.setId("user-2");
        
        message = new Message(listing, fromUser, toUser, "Test message");
        message.setId("message-123");
        messageDTO = new MessageDTO(message);
        
        createRequest = new CreateMessageRequest("listing-123", "user-2", "Test message");
        authToken = "Bearer test-jwt-token";
    }
    
    @Test
    void testSendMessage_Success() throws Exception {
        // Arrange
        when(jwtUtil.extractUsername("test-jwt-token")).thenReturn("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(messageService.sendMessage(any(CreateMessageRequest.class), anyString()))
                .thenReturn(messageDTO);
        
        // Act & Assert
        mockMvc.perform(post("/api/messages")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("message-123"));
        
        verify(messageService).sendMessage(any(CreateMessageRequest.class), eq("user-123"));
    }
    
    @Test
    void testSendMessage_ServiceException() throws Exception {
        // Arrange
        when(jwtUtil.extractUsername("test-jwt-token")).thenReturn("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(messageService.sendMessage(any(CreateMessageRequest.class), anyString()))
                .thenThrow(new RuntimeException("Listing not found"));
        
        // Act & Assert
        mockMvc.perform(post("/api/messages")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error: Listing not found"));
    }
    
    @Test
    void testGetMessageById_Success() throws Exception {
        // Arrange
        when(jwtUtil.extractUsername("test-jwt-token")).thenReturn("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(messageService.getMessageById("message-123", "user-123")).thenReturn(messageDTO);
        
        // Act & Assert
        mockMvc.perform(get("/api/messages/message-123")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("message-123"));
        
        verify(messageService).getMessageById("message-123", "user-123");
    }
    
    @Test
    void testGetMessagesForListing_Success() throws Exception {
        // Arrange
        List<MessageDTO> messages = Arrays.asList(messageDTO);
        when(jwtUtil.extractUsername("test-jwt-token")).thenReturn("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(messageService.getMessagesForListing("listing-123", "user-123")).thenReturn(messages);
        
        // Act & Assert
        mockMvc.perform(get("/api/messages/listing/listing-123")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("message-123"));
        
        verify(messageService).getMessagesForListing("listing-123", "user-123");
    }
    
    @Test
    void testMarkMessageAsRead_Success() throws Exception {
        // Arrange
        when(jwtUtil.extractUsername("test-jwt-token")).thenReturn("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(messageService.markMessageAsRead("message-123", "user-123")).thenReturn(messageDTO);
        
        // Act & Assert
        mockMvc.perform(patch("/api/messages/message-123/mark-read")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("message-123"));
        
        verify(messageService).markMessageAsRead("message-123", "user-123");
    }
    
    @Test
    void testDeleteMessage_Success() throws Exception {
        // Arrange
        when(jwtUtil.extractUsername("test-jwt-token")).thenReturn("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(messageService).deleteMessage("message-123", "user-123");
        
        // Act & Assert
        mockMvc.perform(delete("/api/messages/message-123")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(content().string("Message deleted successfully"));
        
        verify(messageService).deleteMessage("message-123", "user-123");
    }
    
    @Test
    void testGetUnreadMessageCount_Success() throws Exception {
        // Arrange
        when(jwtUtil.extractUsername("test-jwt-token")).thenReturn("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        when(messageService.getUnreadMessageCount("user-123")).thenReturn(5L);
        
        // Act & Assert
        mockMvc.perform(get("/api/messages/unread/count/user-123")
                .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
        
        verify(messageService).getUnreadMessageCount("user-123");
    }
    
    @Test
    void testGetUnreadMessageCount_AccessDenied() throws Exception {
        // Arrange
        when(jwtUtil.extractUsername("test-jwt-token")).thenReturn("user@example.com");
        when(userService.getUserByEmail("user@example.com")).thenReturn(Optional.of(testUser));
        
        // Act & Assert
        mockMvc.perform(get("/api/messages/unread/count/other-user")
                .header("Authorization", authToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied: You can only view your own unread count"));
        
        verify(messageService, never()).getUnreadMessageCount(anyString());
    }
}