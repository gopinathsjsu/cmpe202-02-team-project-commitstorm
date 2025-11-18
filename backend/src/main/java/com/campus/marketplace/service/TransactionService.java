package com.campus.marketplace.service;

import com.campus.marketplace.entity.Transaction;
import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.entity.User;
import com.campus.marketplace.repository.TransactionRepository;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
/**
 * Service layer handling business logic for transactions in the Campus Marketplace.
 *
 * This service coordinates creation, retrieval, update, status transitions,
 * and deletion of transactions. It also ensures related listing state is kept
 * in sync with the transaction lifecycle (e.g., marking listings PENDING or SOLD).
 */
public class TransactionService {
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageService messageService;
    
	/**
	 * Creates a new transaction for a listing and a buyer with a negotiated final price.
	 *
	 * - Validates that the listing and buyer exist
	 * - Ensures the listing is available (ACTIVE) and not already associated with a transaction
	 * - Creates the transaction in PENDING status
	 * - Updates the listing status to PENDING to lock it for other buyers
	 *
	 * @param listingId The identifier of the listing being purchased
	 * @param buyerId The identifier of the buyer initiating the transaction
	 * @param finalPrice The agreed final price for the listing
	 * @return The created Transaction entity
	 * @throws RuntimeException If the listing or buyer does not exist, listing is not ACTIVE,
	 *                          or a transaction already exists for the listing
	 */
	public Transaction createTransaction(String listingId, String buyerId, BigDecimal finalPrice) {
        Optional<Listing> listingOpt = listingRepository.findById(listingId);
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        
        if (listingOpt.isEmpty()) {
            throw new RuntimeException("Listing not found with id: " + listingId);
        }
        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found with id: " + buyerId);
        }
        
        // Check if listing is available for purchase
        Listing listing = listingOpt.get();
        if (listing.getStatus() != Listing.ListingStatus.ACTIVE) {
            throw new RuntimeException("Listing is not available for purchase");
        }
        
        // Check if listing already has a transaction
        if (transactionRepository.findByListingId(listingId).isPresent()) {
            throw new RuntimeException("Listing already has a transaction");
        }
        
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setListing(listing);
        transaction.setBuyer(buyerOpt.get());
        transaction.setFinalPrice(finalPrice);
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        // Update listing status to pending
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        
        return transactionRepository.save(transaction);
    }
    
	/**
	 * Retrieves a transaction by its unique identifier.
	 *
	 * @param id The transaction identifier
	 * @return Optional containing the transaction if found, otherwise empty
	 */
	public Optional<Transaction> getTransactionById(String id) {
        return transactionRepository.findById(id);
    }
    
	/**
	 * Retrieves a transaction by the associated listing identifier.
	 *
	 * @param listingId The listing identifier
	 * @return Optional containing the transaction if present, otherwise empty
	 */
	public Optional<Transaction> getTransactionByListingId(String listingId) {
        return transactionRepository.findByListingId(listingId);
    }
    
	/**
	 * Retrieves all transactions.
	 *
	 * @return A list of all transactions
	 */
	public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
    
	/**
	 * Retrieves all transactions for a given buyer, ordered from newest to oldest.
	 *
	 * @param buyerId The buyer identifier
	 * @return A list of transactions where the user is the buyer
	 */
	public List<Transaction> getTransactionsByBuyer(String buyerId) {
        return transactionRepository.findByBuyerIdOrderByCreatedAtDesc(buyerId);
    }
    
	/**
	 * Retrieves all transactions by status, ordered from newest to oldest.
	 *
	 * @param status The transaction status filter
	 * @return A list of transactions matching the provided status
	 */
	public List<Transaction> getTransactionsByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
	/**
	 * Retrieves all transactions for a given seller, ordered from newest to oldest.
	 *
	 * @param sellerId The seller identifier
	 * @return A list of transactions where the user is the seller
	 */
	public List<Transaction> getTransactionsBySeller(String sellerId) {
        return transactionRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
    }
    
	/**
	 * Retrieves all transactions for a given seller filtered by status, ordered newest to oldest.
	 *
	 * @param sellerId The seller identifier
	 * @param status The status filter
	 * @return A list of transactions matching the seller and status
	 */
	public List<Transaction> getTransactionsBySellerAndStatus(String sellerId, Transaction.TransactionStatus status) {
        return transactionRepository.findBySellerIdAndStatusOrderByCreatedAtDesc(sellerId, status);
    }
    
	/**
	 * Persists updates to an existing transaction.
	 *
	 * @param transaction The transaction entity with updated fields
	 * @return The saved (updated) transaction
	 */
	public Transaction updateTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }
    
	/**
	 * Updates only the status of a transaction and synchronizes the related listing status.
	 *
	 * - COMPLETED → listing becomes SOLD
	 * - CANCELLED → listing becomes ACTIVE
	 *
	 * @param transactionId The transaction identifier
	 * @param status The new transaction status
	 * @return The updated transaction entity
	 * @throws RuntimeException If the transaction does not exist
	 */
	public Transaction updateTransactionStatus(String transactionId, Transaction.TransactionStatus status) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found with id: " + transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        transaction.setStatus(status);
        
        // Update listing status based on transaction status
        Listing listing = transaction.getListing();
        if (status == Transaction.TransactionStatus.COMPLETED) {
            listing.setStatus(Listing.ListingStatus.SOLD);
        } else if (status == Transaction.TransactionStatus.CANCELLED) {
            listing.setStatus(Listing.ListingStatus.ACTIVE);
        }
        listingRepository.save(listing);
        
        return transactionRepository.save(transaction);
    }
    
	/**
	 * Request to Buy: Creates a transaction with the listing's current price.
	 * This is a convenience method for buyers to initiate a purchase request.
	 *
	 * @param listingId The identifier of the listing being purchased
	 * @param buyerId The identifier of the buyer initiating the transaction
	 * @return The created Transaction entity
	 * @throws RuntimeException If the listing or buyer does not exist, listing is not ACTIVE,
	 *                          or a transaction already exists for the listing
	 */
	public Transaction requestToBuy(String listingId, String buyerId) {
        Optional<Listing> listingOpt = listingRepository.findById(listingId);
        Optional<User> buyerOpt = userRepository.findById(buyerId);
        
        if (listingOpt.isEmpty()) {
            throw new RuntimeException("Listing not found with id: " + listingId);
        }
        if (buyerOpt.isEmpty()) {
            throw new RuntimeException("Buyer not found with id: " + buyerId);
        }
        
        Listing listing = listingOpt.get();
        
        // Check if buyer is trying to buy their own listing
        if (listing.getSeller().getId().equals(buyerId)) {
            throw new RuntimeException("Cannot request to buy your own listing");
        }
        
        // Check if listing is available for purchase
        if (listing.getStatus() != Listing.ListingStatus.ACTIVE) {
            throw new RuntimeException("Listing is not available for purchase");
        }
        
        // Check if listing already has a transaction
        if (transactionRepository.findByListingId(listingId).isPresent()) {
            throw new RuntimeException("Listing already has a transaction");
        }
        
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setListing(listing);
        transaction.setBuyer(buyerOpt.get());
        transaction.setFinalPrice(listing.getPrice()); // Use listing price as initial price
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        
        // Update listing status to pending
        listing.setStatus(Listing.ListingStatus.PENDING);
        listingRepository.save(listing);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Send automatic message to seller
        User buyer = buyerOpt.get();
        User seller = listing.getSeller();
        String messageContent = String.format("I'm interested in buying \"%s\" for $%.2f. Please let me know if you'd like to proceed with the sale.", 
                listing.getTitle(), listing.getPrice());
        messageService.createSystemMessage(listing, buyer, seller, messageContent);
        
        return savedTransaction;
    }
    
    /**
     * Mark transaction as SOLD (seller action - accepts the purchase request).
     * Updates transaction status to COMPLETED and listing status to SOLD.
     *
     * @param transactionId The transaction identifier
     * @param sellerId The seller identifier (for authorization)
     * @return The updated transaction entity
     * @throws RuntimeException If the transaction does not exist, seller doesn't match, or transaction is not PENDING
     */
    public Transaction markAsSold(String transactionId, String sellerId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found with id: " + transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        Listing listing = transaction.getListing();
        
        // Verify seller owns the listing
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Only the seller can mark this transaction as sold");
        }
        
        // Verify transaction is in PENDING status
        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new RuntimeException("Only PENDING transactions can be marked as sold");
        }
        
        // Update transaction and listing status
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        listing.setStatus(Listing.ListingStatus.SOLD);
        listingRepository.save(listing);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Send automatic message to buyer
        User seller = listing.getSeller();
        User buyer = transaction.getBuyer();
        String messageContent = String.format("Great news! I've accepted your purchase request for \"%s\". The item is now marked as sold. Please contact me to arrange pickup/payment.", 
                listing.getTitle());
        messageService.createSystemMessage(listing, seller, buyer, messageContent);
        
        return savedTransaction;
    }
    
    /**
     * Reject/Cancel purchase request (seller action - declines the purchase request).
     * Updates transaction status to CANCELLED and listing status back to ACTIVE.
     * This allows the listing to be available for other buyers.
     *
     * @param transactionId The transaction identifier
     * @param sellerId The seller identifier (for authorization)
     * @return The updated transaction entity
     * @throws RuntimeException If the transaction does not exist, seller doesn't match, or transaction is not PENDING
     */
    public Transaction rejectRequest(String transactionId, String sellerId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found with id: " + transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        Listing listing = transaction.getListing();
        
        // Verify seller owns the listing
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Only the seller can reject this purchase request");
        }
        
        // Verify transaction is in PENDING status
        if (transaction.getStatus() != Transaction.TransactionStatus.PENDING) {
            throw new RuntimeException("Only PENDING transactions can be rejected");
        }
        
        // Update transaction and listing status
        transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
        listing.setStatus(Listing.ListingStatus.ACTIVE);
        listingRepository.save(listing);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Send automatic message to buyer
        User seller = listing.getSeller();
        User buyer = transaction.getBuyer();
        String messageContent = String.format("I'm sorry, but I've decided not to proceed with the sale of \"%s\" at this time. The listing is now available again for other buyers.", 
                listing.getTitle());
        messageService.createSystemMessage(listing, seller, buyer, messageContent);
        
        return savedTransaction;
    }
    
	/**
	 * Deletes a transaction by its identifier.
	 *
	 * @param id The transaction identifier
	 */
	public void deleteTransaction(String id) {
        transactionRepository.deleteById(id);
    }
}
