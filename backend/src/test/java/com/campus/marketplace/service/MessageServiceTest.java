package com.campus.marketplace.service;

import com.campus.marketplace.dto.CreateMessageRequest;
import com.campus.marketplace.dto.MessageDTO;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.Message;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.MessageRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {
    
    @Mock
    private MessageRepository messageRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ListingRepository listingRepository;
    
    @InjectMocks
    private MessageService messageService;
    
    private User fromUser;
    private User toUser;
    private Listing listing;
    private Message message;
    private CreateMessageRequest createRequest;
    
    @BeforeEach
    void setUp() {
        fromUser = new User();
        fromUser.setId("user-1");
        fromUser.setName("User One");
        fromUser.setEmail("user1@example.com");
        
        toUser = new User();
        toUser.setId("user-2");
        toUser.setName("User Two");
        toUser.setEmail("user2@example.com");
        
        listing = new Listing();
        listing.setId("listing-123");
        listing.setTitle("Test Listing");
        listing.setSeller(toUser);
        
        message = new Message(listing, fromUser, toUser, "Hello, is this still available?");
        message.setId("message-123");
        message.setIsRead(false);
        
        createRequest = new CreateMessageRequest();
        createRequest.setListingId("listing-123");
        createRequest.setToUserId("user-2");
        createRequest.setContent("Hello, is this still available?");
    }
    
    @Test
    void testSendMessage_Success() {
        // Arrange
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("user-2")).thenReturn(Optional.of(toUser));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(fromUser));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message m = invocation.getArgument(0);
            m.setId("message-123");
            return m;
        });
        
        // Act
        MessageDTO result = messageService.sendMessage(createRequest, "user-1");
        
        // Assert
        assertNotNull(result);
        assertEquals("message-123", result.getId());
        assertEquals("Hello, is this still available?", result.getContent());
        verify(messageRepository).save(any(Message.class));
    }
    
    @Test
    void testSendMessage_ListingNotFound() {
        // Arrange
        when(listingRepository.findById("nonexistent")).thenReturn(Optional.empty());
        createRequest.setListingId("nonexistent");
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(createRequest, "user-1");
        });
        
        assertEquals("Listing not found", exception.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }
    
    @Test
    void testSendMessage_RecipientNotFound() {
        // Arrange
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());
        createRequest.setToUserId("nonexistent");
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(createRequest, "user-1");
        });
        
        assertEquals("Recipient user not found", exception.getMessage());
    }
    
    @Test
    void testSendMessage_SenderNotFound() {
        // Arrange
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("user-2")).thenReturn(Optional.of(toUser));
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(createRequest, "nonexistent");
        });
        
        assertEquals("Sender user not found", exception.getMessage());
    }
    
    @Test
    void testSendMessage_CannotSendToSelf() {
        // Arrange
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(fromUser));
        createRequest.setToUserId("user-1"); // Sending to self
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(createRequest, "user-1");
        });
        
        assertEquals("Cannot send message to yourself", exception.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }
    
    @Test
    void testGetMessageById_Success() {
        // Arrange
        when(messageRepository.findById("message-123")).thenReturn(Optional.of(message));
        when(messageRepository.existsByIdAndUserInvolved("message-123", "user-1")).thenReturn(true);
        
        // Act
        MessageDTO result = messageService.getMessageById("message-123", "user-1");
        
        // Assert
        assertNotNull(result);
        assertEquals("message-123", result.getId());
        verify(messageRepository).findById("message-123");
        verify(messageRepository).existsByIdAndUserInvolved("message-123", "user-1");
    }
    
    @Test
    void testGetMessageById_AccessDenied() {
        // Arrange
        when(messageRepository.findById("message-123")).thenReturn(Optional.of(message));
        when(messageRepository.existsByIdAndUserInvolved("message-123", "user-3")).thenReturn(false);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.getMessageById("message-123", "user-3");
        });
        
        assertEquals("Access denied: You don't have permission to view this message", exception.getMessage());
    }
    
    @Test
    void testCreateSystemMessage_Success() {
        // Arrange
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message m = invocation.getArgument(0);
            m.setId("system-message-123");
            return m;
        });
        
        // Act
        MessageDTO result = messageService.createSystemMessage(
            listing, fromUser, toUser, "System notification");
        
        // Assert
        assertNotNull(result);
        assertEquals("system-message-123", result.getId());
        assertEquals("System notification", result.getContent());
        verify(messageRepository).save(any(Message.class));
    }
    
    @Test
    void testMarkMessageAsRead_Success() {
        // Arrange
        message.setIsRead(false);
        when(messageRepository.findById("message-123")).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        
        // Act
        MessageDTO result = messageService.markMessageAsRead("message-123", "user-2");
        
        // Assert
        assertTrue(message.getIsRead());
        verify(messageRepository).findById("message-123");
        verify(messageRepository).save(message);
    }
    
    @Test
    void testMarkMessageAsRead_OnlyRecipientCanMark() {
        // Arrange
        when(messageRepository.findById("message-123")).thenReturn(Optional.of(message));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.markMessageAsRead("message-123", "user-1"); // Sender trying to mark
        });
        
        assertEquals("Access denied: Only the recipient can mark this message as read", exception.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }
    
    @Test
    void testDeleteMessage_Success() {
        // Arrange
        when(messageRepository.findById("message-123")).thenReturn(Optional.of(message));
        
        // Act
        messageService.deleteMessage("message-123", "user-1");
        
        // Assert
        verify(messageRepository).delete(message);
    }
    
    @Test
    void testDeleteMessage_OnlySenderCanDelete() {
        // Arrange
        when(messageRepository.findById("message-123")).thenReturn(Optional.of(message));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            messageService.deleteMessage("message-123", "user-2"); // Recipient trying to delete
        });
        
        assertEquals("Access denied: Only the sender can delete this message", exception.getMessage());
        verify(messageRepository, never()).delete(any(Message.class));
    }
    
    @Test
    void testGetUnreadMessageCount() {
        // Arrange
        when(messageRepository.countUnreadMessagesByUserId("user-2")).thenReturn(5L);
        
        // Act
        Long count = messageService.getUnreadMessageCount("user-2");
        
        // Assert
        assertEquals(5L, count);
        verify(messageRepository).countUnreadMessagesByUserId("user-2");
    }
    
    @Test
    void testMarkAllMessagesAsRead() {
        // Arrange
        List<Message> unreadMessages = Arrays.asList(message);
        when(messageRepository.findUnreadMessagesByUserId("user-2")).thenReturn(unreadMessages);
        when(messageRepository.saveAll(anyList())).thenReturn(unreadMessages);
        
        // Act
        messageService.markAllMessagesAsRead("user-2");
        
        // Assert
        assertTrue(message.getIsRead());
        verify(messageRepository).findUnreadMessagesByUserId("user-2");
        verify(messageRepository).saveAll(unreadMessages);
    }
    
    @Test
    void testGetMessagesSentByUser_WithPagination() {
        // Arrange
        Page<Message> messagePage = new PageImpl<>(Arrays.asList(message));
        when(messageRepository.findByFromUserIdOrderByCreatedAtDesc(
            "user-1", PageRequest.of(0, 20))).thenReturn(messagePage);
        
        // Act
        messageService.getMessagesSentByUser("user-1", 0, 20);
        
        // Assert
        verify(messageRepository).findByFromUserIdOrderByCreatedAtDesc("user-1", PageRequest.of(0, 20));
    }
}