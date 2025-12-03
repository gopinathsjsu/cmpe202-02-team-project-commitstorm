package com.campus.marketplace.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.campus.marketplace.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    
    // Get messages for a specific listing
    List<Message> findByListingIdOrderByCreatedAtAsc(String listingId);
    
    // Get conversation between two users
    @Query("SELECT m FROM Message m WHERE " +
           "((m.fromUser.id = :userId1 AND m.toUser.id = :userId2) OR " +
           "(m.fromUser.id = :userId2 AND m.toUser.id = :userId1)) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversationBetweenUsers(@Param("userId1") String userId1, @Param("userId2") String userId2);
    
    // Get conversation for a specific listing between two users
    @Query("SELECT m FROM Message m WHERE m.listing.id = :listingId AND " +
           "((m.fromUser.id = :userId1 AND m.toUser.id = :userId2) OR " +
           "(m.fromUser.id = :userId2 AND m.toUser.id = :userId1)) " +
           "ORDER BY m.createdAt ASC")
    List<Message> findConversationForListing(@Param("listingId") String listingId, 
                                           @Param("userId1") String userId1, 
                                           @Param("userId2") String userId2);
    
    // Get conversation for a specific listing between two users with pagination
    @Query("SELECT m FROM Message m WHERE m.listing.id = :listingId AND " +
           "((m.fromUser.id = :userId1 AND m.toUser.id = :userId2) OR " +
           "(m.fromUser.id = :userId2 AND m.toUser.id = :userId1)) " +
           "ORDER BY m.createdAt ASC")
    Page<Message> findConversationForListing(@Param("listingId") String listingId, 
                                           @Param("userId1") String userId1, 
                                           @Param("userId2") String userId2,
                                           Pageable pageable);
    
    // Get messages sent by a user
    List<Message> findByFromUserIdOrderByCreatedAtDesc(String fromUserId);
    
    // Get messages received by a user
    List<Message> findByToUserIdOrderByCreatedAtDesc(String toUserId);
    
    // Get messages sent by a user with pagination
    Page<Message> findByFromUserIdOrderByCreatedAtDesc(String fromUserId, Pageable pageable);
    
    // Get messages received by a user with pagination
    Page<Message> findByToUserIdOrderByCreatedAtDesc(String toUserId, Pageable pageable);
    
    // Get all messages for a user (sent or received)
    @Query("SELECT m FROM Message m WHERE m.fromUser.id = :userId OR m.toUser.id = :userId ORDER BY m.createdAt DESC")
    List<Message> findAllMessagesForUser(@Param("userId") String userId);
    
    // Get all messages for a user with pagination
    @Query("SELECT m FROM Message m WHERE m.fromUser.id = :userId OR m.toUser.id = :userId ORDER BY m.createdAt DESC")
    Page<Message> findAllMessagesForUser(@Param("userId") String userId, Pageable pageable);
    
    // Check if user has permission to view message (either sender or receiver)
    @Query("SELECT COUNT(m) > 0 FROM Message m WHERE m.id = :messageId AND (m.fromUser.id = :userId OR m.toUser.id = :userId)")
    boolean existsByIdAndUserInvolved(@Param("messageId") String messageId, @Param("userId") String userId);
    
    // Get conversation partners for a user
    @Query("SELECT DISTINCT CASE " +
           "WHEN m.fromUser.id = :userId THEN m.toUser.id " +
           "ELSE m.fromUser.id " +
           "END FROM Message m WHERE m.fromUser.id = :userId OR m.toUser.id = :userId")
    List<String> findConversationPartners(@Param("userId") String userId);
    
    // Get unread messages count for a user
    @Query("SELECT COUNT(m) FROM Message m WHERE m.toUser.id = :userId AND m.isRead = false")
    Long countUnreadMessagesByUserId(@Param("userId") String userId);
    
    // Get unread messages for a user
    @Query("SELECT m FROM Message m WHERE m.toUser.id = :userId AND m.isRead = false ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesByUserId(@Param("userId") String userId);
}