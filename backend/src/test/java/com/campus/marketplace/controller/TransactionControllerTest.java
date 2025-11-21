package com.campus.marketplace.controller;

import com.campus.marketplace.dto.TransactionDTO;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.service.TransactionService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {
    
    @Mock
    private TransactionService transactionService;
    
    @InjectMocks
    private TransactionController transactionController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private Transaction transaction;
    private TransactionDTO transactionDTO;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();
        
        User seller = new User();
        seller.setId("seller-123");
        seller.setName("Seller Name");
        
        User buyer = new User();
        buyer.setId("buyer-123");
        buyer.setName("Buyer Name");
        
        Listing listing = new Listing();
        listing.setId("listing-123");
        listing.setTitle("Test Listing");
        listing.setSeller(seller);
        
        transaction = new Transaction();
        transaction.setId("transaction-123");
        transaction.setListing(listing);
        transaction.setBuyer(buyer);
        transaction.setFinalPrice(new BigDecimal("100.00"));
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        transactionDTO = new TransactionDTO(transaction);
    }
    
    @Test
    void testCreateTransaction_Success() throws Exception {
        // Arrange
        when(transactionService.createTransaction(
            anyString(), anyString(), any(BigDecimal.class))).thenReturn(transaction);
        
        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("transaction-123"))
                .andExpect(jsonPath("$.finalPrice").value(100.00));
        
        verify(transactionService).createTransaction(
            transactionDTO.getListingId(), transactionDTO.getBuyerId(), transactionDTO.getFinalPrice());
    }
    
    @Test
    void testCreateTransaction_ServiceException() throws Exception {
        // Arrange
        when(transactionService.createTransaction(anyString(), anyString(), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Listing not found"));
        
        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionDTO)))
                .andExpect(status().isBadRequest());
        
        verify(transactionService).createTransaction(anyString(), anyString(), any(BigDecimal.class));
    }
    
    @Test
    void testGetTransactionById_Success() throws Exception {
        // Arrange
        when(transactionService.getTransactionById("transaction-123"))
                .thenReturn(Optional.of(transaction));
        
        // Act & Assert
        mockMvc.perform(get("/api/transactions/transaction-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("transaction-123"));
        
        verify(transactionService).getTransactionById("transaction-123");
    }
    
    @Test
    void testGetTransactionById_NotFound() throws Exception {
        // Arrange
        when(transactionService.getTransactionById("nonexistent"))
                .thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/transactions/nonexistent"))
                .andExpect(status().isNotFound());
        
        verify(transactionService).getTransactionById("nonexistent");
    }
    
    @Test
    void testGetAllTransactions() throws Exception {
        // Arrange
        List<Transaction> transactions = Arrays.asList(transaction);
        when(transactionService.getAllTransactions()).thenReturn(transactions);
        
        // Act & Assert
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("transaction-123"));
        
        verify(transactionService).getAllTransactions();
    }
    
    @Test
    void testUpdateTransactionStatus_Success() throws Exception {
        // Arrange
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId("transaction-123");
        updatedTransaction.setListing(transaction.getListing());
        updatedTransaction.setBuyer(transaction.getBuyer());
        updatedTransaction.setFinalPrice(transaction.getFinalPrice());
        updatedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionService.updateTransactionStatus(
            "transaction-123", Transaction.TransactionStatus.COMPLETED))
                .thenReturn(updatedTransaction);
        
        // Act & Assert
        mockMvc.perform(patch("/api/transactions/transaction-123/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        
        verify(transactionService).updateTransactionStatus(
            "transaction-123", Transaction.TransactionStatus.COMPLETED);
    }
    
    @Test
    void testUpdateTransactionStatus_ServiceException() throws Exception {
        // Arrange
        when(transactionService.updateTransactionStatus(anyString(), any()))
                .thenThrow(new RuntimeException("Transaction not found"));
        
        // Act & Assert
        mockMvc.perform(patch("/api/transactions/nonexistent/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isBadRequest());
        
        verify(transactionService).updateTransactionStatus(anyString(), any());
    }
    
    @Test
    void testRequestToBuy_Success() throws Exception {
        // Arrange
        when(transactionService.requestToBuy("listing-123", "buyer-123"))
                .thenReturn(transaction);
        
        // Act & Assert
        mockMvc.perform(post("/api/transactions/request-to-buy")
                .param("listingId", "listing-123")
                .param("buyerId", "buyer-123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("transaction-123"));
        
        verify(transactionService).requestToBuy("listing-123", "buyer-123");
    }
    
    @Test
    void testRequestToBuy_ServiceException() throws Exception {
        // Arrange
        when(transactionService.requestToBuy(anyString(), anyString()))
                .thenThrow(new RuntimeException("Listing not found"));
        
        // Act & Assert
        mockMvc.perform(post("/api/transactions/request-to-buy")
                .param("listingId", "nonexistent")
                .param("buyerId", "buyer-123"))
                .andExpect(status().isBadRequest());
        
        verify(transactionService).requestToBuy(anyString(), anyString());
    }
    
    @Test
    void testMarkAsSold_Success() throws Exception {
        // Arrange
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId("transaction-123");
        updatedTransaction.setListing(transaction.getListing());
        updatedTransaction.setBuyer(transaction.getBuyer());
        updatedTransaction.setFinalPrice(transaction.getFinalPrice());
        updatedTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        when(transactionService.markAsSold("transaction-123", "seller-123"))
                .thenReturn(updatedTransaction);
        
        // Act & Assert
        mockMvc.perform(patch("/api/transactions/transaction-123/mark-sold")
                .param("sellerId", "seller-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        
        verify(transactionService).markAsSold("transaction-123", "seller-123");
    }
    
    @Test
    void testMarkAsSold_ServiceException() throws Exception {
        // Arrange
        when(transactionService.markAsSold(anyString(), anyString()))
                .thenThrow(new RuntimeException("Transaction not found"));
        
        // Act & Assert
        mockMvc.perform(patch("/api/transactions/nonexistent/mark-sold")
                .param("sellerId", "seller-123"))
                .andExpect(status().isBadRequest());
        
        verify(transactionService).markAsSold(anyString(), anyString());
    }
    
    @Test
    void testRejectRequest_Success() throws Exception {
        // Arrange
        Transaction updatedTransaction = new Transaction();
        updatedTransaction.setId("transaction-123");
        updatedTransaction.setListing(transaction.getListing());
        updatedTransaction.setBuyer(transaction.getBuyer());
        updatedTransaction.setFinalPrice(transaction.getFinalPrice());
        updatedTransaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        when(transactionService.rejectRequest("transaction-123", "seller-123"))
                .thenReturn(updatedTransaction);
        
        // Act & Assert
        mockMvc.perform(patch("/api/transactions/transaction-123/reject")
                .param("sellerId", "seller-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        
        verify(transactionService).rejectRequest("transaction-123", "seller-123");
    }
    
    @Test
    void testDeleteTransaction() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/transactions/transaction-123"))
                .andExpect(status().isNoContent());
        
        verify(transactionService).deleteTransaction("transaction-123");
    }
}