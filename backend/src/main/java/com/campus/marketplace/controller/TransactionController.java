package com.campus.marketplace.controller;

import com.campus.marketplace.dto.TransactionDTO;
import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Transaction REST endpoints for create/read/update/delete and queries.
 */
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    
    @Autowired
    private TransactionService transactionService;
    
    /**
     * Create a transaction.
     * @param transactionDTO listingId, buyerId, finalPrice
     * @return 201 with created TransactionDTO or 400 on validation failure
     */
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        try {
            Transaction transaction = transactionService.createTransaction(
                transactionDTO.getListingId(),
                transactionDTO.getBuyerId(),
                transactionDTO.getFinalPrice()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new TransactionDTO(transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get a transaction by id.
     * @param id transaction id
     * @return 200 with TransactionDTO or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable String id) {
        return transactionService.getTransactionById(id)
                .map(transaction -> ResponseEntity.ok(new TransactionDTO(transaction)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get a transaction by listing id.
     * @param listingId listing id
     * @return 200 with TransactionDTO or 404 if none
     */
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<TransactionDTO> getTransactionByListingId(@PathVariable String listingId) {
        return transactionService.getTransactionByListingId(listingId)
                .map(transaction -> ResponseEntity.ok(new TransactionDTO(transaction)))
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * List all transactions.
     * @return 200 with list of TransactionDTO
     */
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        List<TransactionDTO> transactions = transactionService.getAllTransactions().stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * List transactions by buyer.
     * @param buyerId buyer id
     * @return 200 with list of TransactionDTO
     */
    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByBuyer(@PathVariable String buyerId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByBuyer(buyerId).stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * List transactions by status.
     * @param status transaction status
     * @return 200 with list of TransactionDTO
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsByStatus(@PathVariable Transaction.TransactionStatus status) {
        List<TransactionDTO> transactions = transactionService.getTransactionsByStatus(status).stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * List transactions by seller.
     * @param sellerId seller id
     * @return 200 with list of TransactionDTO
     */
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsBySeller(@PathVariable String sellerId) {
        List<TransactionDTO> transactions = transactionService.getTransactionsBySeller(sellerId).stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * List transactions by seller and status.
     * @param sellerId seller id
     * @param status transaction status
     * @return 200 with list of TransactionDTO
     */
    @GetMapping("/seller/{sellerId}/status/{status}")
    public ResponseEntity<List<TransactionDTO>> getTransactionsBySellerAndStatus(
            @PathVariable String sellerId, 
            @PathVariable Transaction.TransactionStatus status) {
        List<TransactionDTO> transactions = transactionService.getTransactionsBySellerAndStatus(sellerId, status).stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Update a transaction.
     * @param id transaction id
     * @param transactionDTO new finalPrice and status
     * @return 200 with updated TransactionDTO or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDTO> updateTransaction(@PathVariable String id, @Valid @RequestBody TransactionDTO transactionDTO) {
        return transactionService.getTransactionById(id)
                .map(existingTransaction -> {
                    existingTransaction.setFinalPrice(transactionDTO.getFinalPrice());
                    existingTransaction.setStatus(transactionDTO.getStatus());
                    
                    Transaction updatedTransaction = transactionService.updateTransaction(existingTransaction);
                    return ResponseEntity.ok(new TransactionDTO(updatedTransaction));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Update transaction status only.
     * @param id transaction id
     * @param status new status
     * @return 200 with updated TransactionDTO or 400 on failure
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<TransactionDTO> updateTransactionStatus(@PathVariable String id, @RequestParam Transaction.TransactionStatus status) {
        try {
            Transaction updatedTransaction = transactionService.updateTransactionStatus(id, status);
            return ResponseEntity.ok(new TransactionDTO(updatedTransaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Request to Buy: Buyer initiates a purchase request for a listing.
     * Creates a transaction with the listing's current price and sets status to PENDING.
     * 
     * @param listingId listing id
     * @param buyerId buyer id
     * @return 201 with created TransactionDTO or 400 on validation failure
     */
    @PostMapping("/request-to-buy")
    public ResponseEntity<TransactionDTO> requestToBuy(
            @RequestParam String listingId,
            @RequestParam String buyerId) {
        try {
            Transaction transaction = transactionService.requestToBuy(listingId, buyerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new TransactionDTO(transaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Mark transaction as SOLD (seller action - accepts the purchase request).
     * Updates transaction status to COMPLETED and listing status to SOLD.
     * 
     * @param transactionId transaction id
     * @param sellerId seller id (for authorization)
     * @return 200 with updated TransactionDTO or 400 on failure
     */
    @PatchMapping("/{transactionId}/mark-sold")
    public ResponseEntity<TransactionDTO> markAsSold(
            @PathVariable String transactionId,
            @RequestParam String sellerId) {
        try {
            Transaction updatedTransaction = transactionService.markAsSold(transactionId, sellerId);
            return ResponseEntity.ok(new TransactionDTO(updatedTransaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Reject purchase request (seller action - declines the purchase request).
     * Updates transaction status to CANCELLED and listing status back to ACTIVE.
     * This makes the listing available for other buyers again.
     * 
     * @param transactionId transaction id
     * @param sellerId seller id (for authorization)
     * @return 200 with updated TransactionDTO or 400 on failure
     */
    @PatchMapping("/{transactionId}/reject")
    public ResponseEntity<TransactionDTO> rejectRequest(
            @PathVariable String transactionId,
            @RequestParam String sellerId) {
        try {
            Transaction updatedTransaction = transactionService.rejectRequest(transactionId, sellerId);
            return ResponseEntity.ok(new TransactionDTO(updatedTransaction));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Delete a transaction.
     * @param id transaction id
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
