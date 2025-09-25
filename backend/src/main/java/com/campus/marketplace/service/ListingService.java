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
    
    public Listing createListing(Listing listing) {
        if (listing.getId() == null) {
            listing.setId(UUID.randomUUID().toString());
        }
        return listingRepository.save(listing);
    }
    
    public Optional<Listing> getListingById(String id) {
        return listingRepository.findById(id);
    }
    
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }
    
    public List<Listing> getListingsBySeller(String sellerId) {
        return listingRepository.findBySellerId(sellerId);
    }
    
    public List<Listing> getListingsByCategory(String categoryId) {
        return listingRepository.findByCategoryId(categoryId);
    }
    
    public List<Listing> getListingsByStatus(Listing.ListingStatus status) {
        return listingRepository.findByStatus(status);
    }
    
    public List<Listing> getListingsByCondition(Listing.ItemCondition condition) {
        return listingRepository.findByCondition(condition);
    }
    
    public List<Listing> getListingsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return listingRepository.findByPriceRange(minPrice, maxPrice);
    }
    
    public List<Listing> searchListings(String searchTerm) {
        return listingRepository.findByTitleOrDescriptionContaining(searchTerm, searchTerm);
    }
    
    public List<Listing> getListingsBySellerAndStatus(String sellerId, Listing.ListingStatus status) {
        return listingRepository.findBySellerIdAndStatus(sellerId, status);
    }
    
    public Page<Listing> getListingsByCategoryAndStatus(String categoryId, Listing.ListingStatus status, Pageable pageable) {
        return listingRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
    }
    
    public Page<Listing> getListingsByStatusOrderByCreatedAtDesc(Listing.ListingStatus status, Pageable pageable) {
        return listingRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
    
    public Page<Listing> getListingsBySellerOrderByCreatedAtDesc(String sellerId, Pageable pageable) {
        return listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
    }
    
    public Listing updateListing(Listing listing) {
        return listingRepository.save(listing);
    }
    
    public void deleteListing(String id) {
        listingRepository.deleteById(id);
    }
    
    public Listing updateListingStatus(String id, Listing.ListingStatus status) {
        Optional<Listing> listingOpt = listingRepository.findById(id);
        if (listingOpt.isPresent()) {
            Listing listing = listingOpt.get();
            listing.setStatus(status);
            return listingRepository.save(listing);
        }
        throw new RuntimeException("Listing not found with id: " + id);
    }
    
    public boolean isListingOwnedByUser(String listingId, String userId) {
        Optional<Listing> listingOpt = listingRepository.findById(listingId);
        return listingOpt.isPresent() && listingOpt.get().getSeller().getId().equals(userId);
    }
}
