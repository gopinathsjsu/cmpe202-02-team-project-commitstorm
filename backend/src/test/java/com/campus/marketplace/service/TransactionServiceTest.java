package com.campus.marketplace.service;

import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.TransactionRepository;
import com.campus.marketplace.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private ListingRepository listingRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private MessageService messageService;
    
    @InjectMocks
    private TransactionService transactionService;
    
    private User seller;
    private User buyer;
    private Listing listing;
    private Transaction transaction;
    
    @BeforeEach
    void setUp() {
        seller = new User();
        seller.setId("seller-123");
        seller.setName("Test Seller");
        seller.setEmail("seller@test.com");
        
        buyer = new User();
        buyer.setId("buyer-123");
        buyer.setName("Test Buyer");
        buyer.setEmail("buyer@test.com");
        
        listing = new Listing();
        listing.setId("listing-123");
        listing.setTitle("Test Item");
        listing.setPrice(new BigDecimal("99.99"));
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listing.setSeller(seller);
        
        transaction = new Transaction();
        transaction.setId("transaction-123");
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(new BigDecimal("99.99"));
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
    }
    
    @Test
    void testRequestToBuy_Success() {
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("buyer-123")).thenReturn(Optional.of(buyer));
        when(transactionRepository.findByListingId("listing-123")).thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        
        Transaction result = transactionService.requestToBuy("listing-123", "buyer-123");
        
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.PENDING, result.getStatus());
        verify(listingRepository, times(1)).save(any(Listing.class));
        verify(messageService, times(1)).createSystemMessage(any(), any(), any(), anyString());
    }
    
    @Test
    void testRequestToBuy_ListingNotFound() {
        when(listingRepository.findById("listing-123")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.requestToBuy("listing-123", "buyer-123");
        });
    }
    
    @Test
    void testRequestToBuy_BuyerNotFound() {
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("buyer-123")).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.requestToBuy("listing-123", "buyer-123");
        });
    }
    
    @Test
    void testRequestToBuy_OwnListing() {
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("seller-123")).thenReturn(Optional.of(seller));
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.requestToBuy("listing-123", "seller-123");
        });
    }
    
    @Test
    void testRequestToBuy_ListingNotActive() {
        listing.setStatus(Listing.ListingStatus.SOLD);
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("buyer-123")).thenReturn(Optional.of(buyer));
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.requestToBuy("listing-123", "buyer-123");
        });
    }
    
    @Test
    void testMarkAsSold_Success() {
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        
        Transaction result = transactionService.markAsSold("transaction-123", "seller-123");
        
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        verify(listingRepository, times(1)).save(any(Listing.class));
        verify(messageService, times(1)).createSystemMessage(any(), any(), any(), anyString());
    }
    
    @Test
    void testMarkAsSold_WrongSeller() {
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.markAsSold("transaction-123", "wrong-seller-123");
        });
    }
    
    @Test
    void testMarkAsSold_NotPending() {
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.markAsSold("transaction-123", "seller-123");
        });
    }
    
    @Test
    void testRejectRequest_Success() {
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        
        Transaction result = transactionService.rejectRequest("transaction-123", "seller-123");
        
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.CANCELLED, result.getStatus());
        verify(listingRepository, times(1)).save(any(Listing.class));
        verify(messageService, times(1)).createSystemMessage(any(), any(), any(), anyString());
    }
    
    @Test
    void testRejectRequest_WrongSeller() {
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        
        assertThrows(RuntimeException.class, () -> {
            transactionService.rejectRequest("transaction-123", "wrong-seller-123");
        });
    }
}

