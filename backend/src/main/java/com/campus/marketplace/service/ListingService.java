package com.campus.marketplace.service;

import com.campus.marketplace.entity.Listing;
import com.campus.marketplace.repository.ListingRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ListingService {
    
    @Autowired
    private ListingRepository listingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    /**
     * Create a listing, generating id if absent.
     * @param listing listing entity
     * @return saved Listing
     */
    public Listing createListing(Listing listing) {
        if (listing.getId() == null) {
            listing.setId(UUID.randomUUID().toString());
        }
        return listingRepository.save(listing);
    }
    
    /**
     * Get listing by id.
     * @param id listing id
     * @return Optional listing
     */
    public Optional<Listing> getListingById(String id) {
        return listingRepository.findById(id);
    }
    
    /**
     * List all listings.
     * @return list of listings
     */
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }
    
    /**
     * List listings by seller.
     * @param sellerId seller id
     * @return list of listings
     */
    public List<Listing> getListingsBySeller(String sellerId) {
        return listingRepository.findBySellerId(sellerId);
    }
    
    /**
     * List listings by category.
     * @param categoryId category id
     * @return list of listings
     */
    public List<Listing> getListingsByCategory(String categoryId) {
        return listingRepository.findByCategoryId(categoryId);
    }
    
    /**
     * List listings by status.
     * @param status listing status
     * @return list of listings
     */
    public List<Listing> getListingsByStatus(Listing.ListingStatus status) {
        return listingRepository.findByStatus(status);
    }
    
    /**
     * List listings by item condition.
     * @param condition condition enum
     * @return list of listings
     */
    public List<Listing> getListingsByCondition(Listing.ItemCondition condition) {
        return listingRepository.findByCondition(condition);
    }
    
    /**
     * List listings within price range.
     * @param minPrice min
     * @param maxPrice max
     * @return list of listings
     */
    public List<Listing> getListingsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return listingRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    /**
     * Search listings by term across title/description.
     * @param searchTerm query
     * @return list of listings
     */
    public List<Listing> searchListings(String searchTerm) {
        return listingRepository.findByTitleOrDescriptionContaining(searchTerm, searchTerm);
    }
    
    /**
     * List listings by seller and status.
     * @param sellerId seller id
     * @param status status
     * @return list of listings
     */
    public List<Listing> getListingsBySellerAndStatus(String sellerId, Listing.ListingStatus status) {
        return listingRepository.findBySellerIdAndStatus(sellerId, status);
    }
    
    /**
     * Page listings by category and status.
     * @param categoryId category id
     * @param status status
     * @param pageable paging
     * @return Page<Listing>
     */
    public Page<Listing> getListingsByCategoryAndStatus(String categoryId, Listing.ListingStatus status, Pageable pageable) {
        return listingRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
    }
    
    /**
     * Page listings by status ordered by creation time desc.
     * @param status status
     * @param pageable paging
     * @return Page<Listing>
     */
    public Page<Listing> getListingsByStatusOrderByCreatedAtDesc(Listing.ListingStatus status, Pageable pageable) {
        return listingRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
    
    /**
     * Page listings by seller ordered by creation time desc.
     * @param sellerId seller id
     * @param pageable paging
     * @return Page<Listing>
     */
    public Page<Listing> getListingsBySellerOrderByCreatedAtDesc(String sellerId, Pageable pageable) {
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
    }
    
    /**
     * Update listing fields.
     * @param listing listing entity
     * @return saved Listing
     */
    public Listing updateListing(Listing listing) {
        return listingRepository.save(listing);
    }
    
    /**
     * Delete listing by id.
     * @param id listing id
     */
    public void deleteListing(String id) {
        listingRepository.deleteById(id);
    }
    
    /**
     * Update listing status only.
     * @param id listing id
     * @param status new status
     * @return updated Listing
     * @throws RuntimeException if not found
     */
    public Listing updateListingStatus(String id, Listing.ListingStatus status) {
        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();
            listing.setStatus(status);
            return listingRepository.save(listing);
        }
        throw new RuntimeException("Listing not found with id: " + id);
    }
    
    /**
     * Check if a listing is owned by a user.
     * @param listingId listing id
     * @param userId user id
     * @return true if owned by user
     */
    public boolean isListingOwnedByUser(String listingId, String userId) {
        Optional<Listing> listingOpt = listingRepository.findById(listingId);
        return listingOpt.isPresent() && listingOpt.get().getSeller().getId().equals(userId);
    }
}
