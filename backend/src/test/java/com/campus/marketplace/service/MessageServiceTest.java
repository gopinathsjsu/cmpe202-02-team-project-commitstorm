package com.campus.marketplace.service;

import com.campus.marketplace.dto.CreateMessageRequest;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MessageService.
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    
    @Mock
    private MessageRepository messageRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ListingRepository listingRepository;
    
    @InjectMocks
    private MessageService messageService;
    
    private User sender;
    private User receiver;
    private Listing listing;
    private Message message;
    
    @BeforeEach
    void setUp() {
        sender = new User();
        sender.setId("sender-123");
        sender.setName("Test Sender");
        sender.setEmail("sender@test.com");
        
        receiver = new User();
        receiver.setId("receiver-123");
        receiver.setName("Test Receiver");
        receiver.setEmail("receiver@test.com");
        
        listing = new Listing();
        listing.setId("listing-123");
        listing.setTitle("Test Item");
        
        message = new Message();
        message.setId("message-123");
        message.setListing(listing);
        message.setFromUser(sender);
        message.setToUser(receiver);
        message.setContent("Test message");
        message.setIsRead(false);
    }
    
    @Test
    void testSendMessage_Success() {
        CreateMessageRequest request = new CreateMessageRequest();
        request.setListingId("listing-123");
        request.setToUserId("receiver-123");
        request.setContent("Hello");
        
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("receiver-123")).thenReturn(Optional.of(receiver));
        when(userRepository.findById("sender-123")).thenReturn(Optional.of(sender));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        
        var result = messageService.sendMessage(request, "sender-123");
        
        assertNotNull(result);
        assertEquals("Test message", result.getContent());
        verify(messageRepository, times(1)).save(any(Message.class));
    }
    
    @Test
    void testSendMessage_ToSelf() {
        CreateMessageRequest request = new CreateMessageRequest();
        request.setListingId("listing-123");
        request.setToUserId("sender-123");
        request.setContent("Hello");
        
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("sender-123")).thenReturn(Optional.of(sender));
        
        assertThrows(RuntimeException.class, () -> {
            messageService.sendMessage(request, "sender-123");
        });
    }
    
    @Test
    void testGetUnreadMessageCount() {
        when(messageRepository.countUnreadMessagesByUserId("receiver-123")).thenReturn(5L);
        
        Long count = messageService.getUnreadMessageCount("receiver-123");
        
        assertEquals(5L, count);
    }
    
    @Test
    void testGetUnreadMessages() {
        Message unreadMessage1 = new Message();
        unreadMessage1.setIsRead(false);
        Message unreadMessage2 = new Message();
        unreadMessage2.setIsRead(false);
        
        when(messageRepository.findUnreadMessagesByUserId("receiver-123"))
                .thenReturn(Arrays.asList(unreadMessage1, unreadMessage2));
        
        var messages = messageService.getUnreadMessages("receiver-123");
        
        assertEquals(2, messages.size());
    }
    
    @Test
    void testMarkMessageAsRead_Success() {
        when(messageRepository.findById("message-123")).thenReturn(Optional.of(message));
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        
        var result = messageService.markMessageAsRead("message-123", "receiver-123");
        
        assertNotNull(result);
        assertTrue(message.getIsRead());
        verify(messageRepository, times(1)).save(any(Message.class));
    }
    
    @Test
    void testMarkMessageAsRead_NotRecipient() {
        when(messageRepository.findById("message-123")).thenReturn(Optional.of(message));
        
        assertThrows(RuntimeException.class, () -> {
            messageService.markMessageAsRead("message-123", "sender-123");
        });
    }
    
    @Test
    void testMarkAllMessagesAsRead() {
        Message unreadMessage1 = new Message();
        unreadMessage1.setIsRead(false);
        Message unreadMessage2 = new Message();
        unreadMessage2.setIsRead(false);
        
        when(messageRepository.findUnreadMessagesByUserId("receiver-123"))
                .thenReturn(Arrays.asList(unreadMessage1, unreadMessage2));
        when(messageRepository.saveAll(any(List.class))).thenReturn(Arrays.asList(unreadMessage1, unreadMessage2));
        
        messageService.markAllMessagesAsRead("receiver-123");
        
        assertTrue(unreadMessage1.getIsRead());
        assertTrue(unreadMessage2.getIsRead());
        verify(messageRepository, times(1)).saveAll(any(List.class));
    }
    
    @Test
    void testCreateSystemMessage() {
        when(messageRepository.save(any(Message.class))).thenReturn(message);
        
        var result = messageService.createSystemMessage(listing, sender, receiver, "System message");
        
        assertNotNull(result);
        verify(messageRepository, times(1)).save(any(Message.class));
    }
}

