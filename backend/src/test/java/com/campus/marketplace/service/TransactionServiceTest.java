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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    
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
        seller.setName("Seller Name");
        seller.setEmail("seller@example.com");
        
        buyer = new User();
        buyer.setId("buyer-123");
        buyer.setName("Buyer Name");
        buyer.setEmail("buyer@example.com");
        
        listing = new Listing();
        listing.setId("listing-123");
        listing.setTitle("Test Listing");
        listing.setPrice(new BigDecimal("100.00"));
        listing.setSeller(seller);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        
        transaction = new Transaction();
        transaction.setId("transaction-123");
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(new BigDecimal("100.00"));
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
    }
    
    @Test
    void testCreateTransaction_Success() {
        // Arrange
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("buyer-123")).thenReturn(Optional.of(buyer));
        when(transactionRepository.findByListingId("listing-123")).thenReturn(Optional.empty());
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId("transaction-123");
            return t;
        });
        
        // Act
        Transaction result = transactionService.createTransaction(
            "listing-123", "buyer-123", new BigDecimal("90.00"));
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("listing-123", result.getListing().getId());
        assertEquals("buyer-123", result.getBuyer().getId());
        assertEquals(new BigDecimal("90.00"), result.getFinalPrice());
        assertEquals(Transaction.TransactionStatus.PENDING, result.getStatus());
        assertEquals(Listing.ListingStatus.PENDING, listing.getStatus());
        
        verify(listingRepository).findById("listing-123");
        verify(userRepository).findById("buyer-123");
        verify(transactionRepository).findByListingId("listing-123");
        verify(listingRepository).save(listing);
        verify(transactionRepository).save(any(Transaction.class));
    }
    
    @Test
    void testCreateTransaction_ListingNotFound() {
        // Arrange
        when(listingRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction("nonexistent", "buyer-123", new BigDecimal("100.00"));
        });
        
        assertEquals("Listing not found with id: nonexistent", exception.getMessage());
        verify(listingRepository).findById("nonexistent");
        verify(userRepository, never()).findById(anyString());
    }
    
    @Test
    void testCreateTransaction_BuyerNotFound() {
        // Arrange
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction("listing-123", "nonexistent", new BigDecimal("100.00"));
        });
        
        assertEquals("Buyer not found with id: nonexistent", exception.getMessage());
        verify(listingRepository).findById("listing-123");
        verify(userRepository).findById("nonexistent");
    }
    
    @Test
    void testCreateTransaction_ListingNotActive() {
        // Arrange
        listing.setStatus(Listing.ListingStatus.SOLD);
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("buyer-123")).thenReturn(Optional.of(buyer));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction("listing-123", "buyer-123", new BigDecimal("100.00"));
        });
        
        assertEquals("Listing is not available for purchase", exception.getMessage());
        verify(listingRepository).findById("listing-123");
        verify(userRepository).findById("buyer-123");
    }
    
    @Test
    void testCreateTransaction_ListingAlreadyHasTransaction() {
        // Arrange
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("buyer-123")).thenReturn(Optional.of(buyer));
        when(transactionRepository.findByListingId("listing-123")).thenReturn(Optional.of(transaction));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.createTransaction("listing-123", "buyer-123", new BigDecimal("100.00"));
        });
        
        assertEquals("Listing already has a transaction", exception.getMessage());
        verify(transactionRepository).findByListingId("listing-123");
    }
    
    @Test
    void testRequestToBuy_Success() {
        // Arrange
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("buyer-123")).thenReturn(Optional.of(buyer));
        when(transactionRepository.findByListingId("listing-123")).thenReturn(Optional.empty());
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId("transaction-123");
            return t;
        });
        
        // Act
        Transaction result = transactionService.requestToBuy("listing-123", "buyer-123");
        
        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getFinalPrice());
        assertEquals(Transaction.TransactionStatus.PENDING, result.getStatus());
        assertEquals(Listing.ListingStatus.PENDING, listing.getStatus());
        
        verify(messageService).createSystemMessage(
            eq(listing), eq(buyer), eq(seller), anyString());
    }
    
    @Test
    void testRequestToBuy_BuyerOwnsListing() {
        // Arrange
        listing.setSeller(buyer); // Buyer is the seller
        when(listingRepository.findById("listing-123")).thenReturn(Optional.of(listing));
        when(userRepository.findById("buyer-123")).thenReturn(Optional.of(buyer));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.requestToBuy("listing-123", "buyer-123");
        });
        
        assertEquals("Cannot request to buy your own listing", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void testUpdateTransactionStatus_ToCompleted() {
        // Arrange
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        
        // Act
        Transaction result = transactionService.updateTransactionStatus(
            "transaction-123", Transaction.TransactionStatus.COMPLETED);
        
        // Assert
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(Listing.ListingStatus.SOLD, listing.getStatus());
        verify(listingRepository).save(listing);
        verify(transactionRepository).save(transaction);
    }
    
    @Test
    void testUpdateTransactionStatus_ToCancelled() {
        // Arrange
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        
        // Act
        Transaction result = transactionService.updateTransactionStatus(
            "transaction-123", Transaction.TransactionStatus.CANCELLED);
        
        // Assert
        assertEquals(Transaction.TransactionStatus.CANCELLED, result.getStatus());
        assertEquals(Listing.ListingStatus.ACTIVE, listing.getStatus());
        verify(listingRepository).save(listing);
        verify(transactionRepository).save(transaction);
    }
    
    @Test
    void testUpdateTransactionStatus_NotFound() {
        // Arrange
        when(transactionRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.updateTransactionStatus("nonexistent", Transaction.TransactionStatus.COMPLETED);
        });
        
        assertEquals("Transaction not found with id: nonexistent", exception.getMessage());
        verify(transactionRepository).findById("nonexistent");
        verify(listingRepository, never()).save(any(Listing.class));
    }
    
    @Test
    void testMarkAsSold_Success() {
        // Arrange
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        
        // Act
        Transaction result = transactionService.markAsSold("transaction-123", "seller-123");
        
        // Assert
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        assertEquals(Listing.ListingStatus.SOLD, listing.getStatus());
        verify(messageService).createSystemMessage(
            eq(listing), eq(seller), eq(buyer), anyString());
    }
    
    @Test
    void testMarkAsSold_WrongSeller() {
        // Arrange
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.markAsSold("transaction-123", "wrong-seller");
        });
        
        assertEquals("Only the seller can mark this transaction as sold", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
    
    @Test
    void testMarkAsSold_NotPending() {
        // Arrange
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.markAsSold("transaction-123", "seller-123");
        });
        
        assertEquals("Only PENDING transactions can be marked as sold", exception.getMessage());
    }
    
    @Test
    void testRejectRequest_Success() {
        // Arrange
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        listing.setStatus(Listing.ListingStatus.PENDING);
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        when(listingRepository.save(any(Listing.class))).thenReturn(listing);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        
        // Act
        Transaction result = transactionService.rejectRequest("transaction-123", "seller-123");
        
        // Assert
        assertEquals(Transaction.TransactionStatus.CANCELLED, result.getStatus());
        assertEquals(Listing.ListingStatus.ACTIVE, listing.getStatus());
        verify(messageService).createSystemMessage(
            eq(listing), eq(seller), eq(buyer), anyString());
    }
    
    @Test
    void testRejectRequest_WrongSeller() {
        // Arrange
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.rejectRequest("transaction-123", "wrong-seller");
        });
        
        assertEquals("Only the seller can reject this purchase request", exception.getMessage());
    }
    
    @Test
    void testRejectRequest_NotPending() {
        // Arrange
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.rejectRequest("transaction-123", "seller-123");
        });
        
        assertEquals("Only PENDING transactions can be rejected", exception.getMessage());
    }
    
    @Test
    void testGetTransactionById() {
        // Arrange
        when(transactionRepository.findById("transaction-123")).thenReturn(Optional.of(transaction));
        
        // Act
        Optional<Transaction> result = transactionService.getTransactionById("transaction-123");
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("transaction-123", result.get().getId());
        verify(transactionRepository).findById("transaction-123");
    }
    
    @Test
    void testDeleteTransaction() {
        // Act
        transactionService.deleteTransaction("transaction-123");
        
        // Assert
        verify(transactionRepository).deleteById("transaction-123");
    }
}