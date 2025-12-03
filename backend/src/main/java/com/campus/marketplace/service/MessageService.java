package com.campus.marketplace.service;

import com.campus.marketplace.dto.CreateMessageRequest;
import com.campus.marketplace.dto.MessageDTO;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.Message;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.MessageRepository;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MessageService {
    
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    // Send a message
    public MessageDTO sendMessage(CreateMessageRequest request, String fromUserId) {
        // Validate that the listing exists
        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        
        // Validate that the recipient exists
        User toUser = userRepository.findById(request.getToUserId())
                .orElseThrow(() -> new RuntimeException("Recipient user not found"));
        
        // Validate that the sender exists
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("Sender user not found"));
        
        // Validate that user is not sending message to themselves
        if (fromUserId.equals(request.getToUserId())) {
            throw new RuntimeException("Cannot send message to yourself");
        }
        
        // Create and save the message
        Message message = new Message(listing, fromUser, toUser, request.getContent());
        Message savedMessage = messageRepository.save(message);
        
        return new MessageDTO(savedMessage);
    }
    
    // Get message by ID (with permission check)
    public MessageDTO getMessageById(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Check if user has permission to view this message
        if (!messageRepository.existsByIdAndUserInvolved(messageId, userId)) {
            throw new RuntimeException("Access denied: You don't have permission to view this message");
        }
        
        return new MessageDTO(message);
    }
    
    // Get messages for a listing
    public List<MessageDTO> getMessagesForListing(String listingId, String userId) {
        // Validate that the listing exists
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        
        // Check if user is the seller or has sent/received messages about this listing
        List<Message> messages = messageRepository.findByListingIdOrderByCreatedAtAsc(listingId);
        
        // Filter messages to only include those where the user is involved
        List<Message> userMessages = messages.stream()
                .filter(message -> message.getFromUser().getId().equals(userId) || 
                                 message.getToUser().getId().equals(userId))
                .collect(Collectors.toList());
        
        return userMessages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }
    
    // Get conversation between two users
    public List<MessageDTO> getConversationBetweenUsers(String userId1, String userId2, String currentUserId) {
        // Validate that current user is one of the participants
        if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
            throw new RuntimeException("Access denied: You can only view conversations you're part of");
        }
        
        List<Message> messages = messageRepository.findConversationBetweenUsers(userId1, userId2);
        
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }
    
    // Get conversation for a specific listing between two users
    public List<MessageDTO> getConversationForListing(String listingId, String userId1, String userId2, String currentUserId) {
        // Validate that current user is one of the participants
        if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
            throw new RuntimeException("Access denied: You can only view conversations you're part of");
        }
        
        List<Message> messages = messageRepository.findConversationForListing(listingId, userId1, userId2);
        
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }
    
    // Get conversation for a specific listing between two users with pagination
    public Page<MessageDTO> getConversationForListing(String listingId, String userId1, String userId2, String currentUserId, int page, int size) {
        // Validate that current user is one of the participants
        if (!currentUserId.equals(userId1) && !currentUserId.equals(userId2)) {
            throw new RuntimeException("Access denied: You can only view conversations you're part of");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findConversationForListing(listingId, userId1, userId2, pageable);
        
        return messages.map(MessageDTO::new);
    }
    
    // Get messages sent by user
    public List<MessageDTO> getMessagesSentByUser(String userId) {
        List<Message> messages = messageRepository.findByFromUserIdOrderByCreatedAtDesc(userId);
        
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }
    
    // Get messages received by user
    public List<MessageDTO> getMessagesReceivedByUser(String userId) {
        List<Message> messages = messageRepository.findByToUserIdOrderByCreatedAtDesc(userId);
        
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }
    
    // Get all messages for a user (sent and received)
    public List<MessageDTO> getAllMessagesForUser(String userId) {
        List<Message> messages = messageRepository.findAllMessagesForUser(userId);
        
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }
    
    // Get messages sent by user with pagination
    public Page<MessageDTO> getMessagesSentByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByFromUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return messages.map(MessageDTO::new);
    }
    
    // Get messages received by user with pagination
    public Page<MessageDTO> getMessagesReceivedByUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findByToUserIdOrderByCreatedAtDesc(userId, pageable);
        
        return messages.map(MessageDTO::new);
    }
    
    // Get all messages for a user with pagination
    public Page<MessageDTO> getAllMessagesForUser(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findAllMessagesForUser(userId, pageable);
        
        return messages.map(MessageDTO::new);
    }
    
    // Get conversation partners for a user
    public List<String> getConversationPartners(String userId) {
        return messageRepository.findConversationPartners(userId);
    }
    
    // Delete message (only sender can delete)
    public void deleteMessage(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Only the sender can delete the message
        if (!message.getFromUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Only the sender can delete this message");
        }
        
        messageRepository.delete(message);
    }
    
    // Get unread message count for a user
    public Long getUnreadMessageCount(String userId) {
        return messageRepository.countUnreadMessagesByUserId(userId);
    }
    
    // Get unread messages for a user
    public List<MessageDTO> getUnreadMessages(String userId) {
        List<Message> messages = messageRepository.findUnreadMessagesByUserId(userId);
        return messages.stream()
                .map(MessageDTO::new)
                .collect(Collectors.toList());
    }
    
    // Mark message as read
    public MessageDTO markMessageAsRead(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        
        // Only the recipient can mark a message as read
        if (!message.getToUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied: Only the recipient can mark this message as read");
        }
        
        message.setIsRead(true);
        Message savedMessage = messageRepository.save(message);
        
        return new MessageDTO(savedMessage);
    }
    
    // Mark all messages as read for a user
    public void markAllMessagesAsRead(String userId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessagesByUserId(userId);
        for (Message message : unreadMessages) {
            message.setIsRead(true);
        }
        messageRepository.saveAll(unreadMessages);
    }
    
    /**
     * Create a system message automatically (e.g., for transaction notifications).
     * This bypasses normal validation since it's an automated message.
     * 
     * @param listing The listing associated with the message
     * @param fromUser The user sending the message (buyer or seller)
     * @param toUser The user receiving the message
     * @param content The message content
     * @return The created message DTO
     */
    public MessageDTO createSystemMessage(Listing listing, User fromUser, User toUser, String content) {
        Message message = new Message(listing, fromUser, toUser, content);
        Message savedMessage = messageRepository.save(message);
        return new MessageDTO(savedMessage);
    }
}