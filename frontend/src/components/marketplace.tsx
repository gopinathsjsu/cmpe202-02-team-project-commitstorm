import React, { useState, useEffect, useRef, useCallback } from "react";
import { useSearchParams } from "react-router";
import Post from "./post.tsx";
import type {ListingDetailProps} from "./post.tsx";
import { getListings } from "../services/listingsService";
import '../css/Marketplace.css';

/**
 * Map API listing response to Post component props
 */
const mapListingToPostProps = (listing) => {
  // Parse images JSON string - format: "[\"https://example.com/iphone1.jpg\", \"https://example.com/iphone2.jpg\"]"
  let imageUrl = "https://via.placeholder.com/400x200?text=No+Image"; // default placeholder
  
  if (listing.images) {
    try {
      let images;
      
      // Check if images is already an array
      if (Array.isArray(listing.images)) {
        images = listing.images;
      } else if (typeof listing.images === 'string') {
        // Trim the string first in case of whitespace
        const trimmedImages = listing.images.trim();
        
        // Try to parse JSON string
        if (trimmedImages.startsWith('[') || trimmedImages.startsWith('"')) {
          images = JSON.parse(trimmedImages);
        } else {
          // If it's a single URL string (not JSON), use it directly
          images = [trimmedImages];
        }
      }
      
      if (Array.isArray(images) && images.length > 0) {
        // Filter out empty strings and null values, then get the first valid URL
        const validImages = images.filter(img => img && typeof img === 'string' && img.trim() !== '');
        if (validImages.length > 0) {
          imageUrl = validImages[0].trim();
        }
      }
      
      console.log('Parsed images for listing:', listing.title, 'Image URL:', imageUrl);
    } catch (e) {
      console.error('Error parsing images for listing:', listing.title, e);
      console.log('Raw images value:', listing.images, 'Type:', typeof listing.images);
      
      // Fallback: if images is a string but not valid JSON, try using it as a single URL
      if (typeof listing.images === 'string' && listing.images.trim() !== '') {
        const trimmed = listing.images.trim();
        // Basic URL validation
        if (trimmed.startsWith('http://') || trimmed.startsWith('https://')) {
          imageUrl = trimmed;
          console.log('Using images string as direct URL:', imageUrl);
        }
      }
    }
  } else {
    console.log('No images field for listing:', listing.title);
  }

  // Convert UUID string id to number for Post component (using hash)
  const numericId = listing.id ? parseInt(listing.id.replace(/-/g, '').substring(0, 15), 16) % 1000000 : 0;
  const numericUserId = listing.sellerId ? parseInt(listing.sellerId.replace(/-/g, '').substring(0, 15), 16) % 1000000 : 0;

  return {
    id: numericId,
    userId: numericUserId,
    username: listing.sellerName || 'Unknown Seller',
    description: listing.description || '',
    imageUrl: imageUrl,
    createdAt: listing.createdAt || new Date().toISOString(),
    updatedAt: listing.updatedAt || new Date().toISOString(),
    price: parseFloat(listing.price) || 0,
    condition: listing.condition || 'GOOD',
    category: listing.categoryName || 'Uncategorized',
    title: listing.title || '',
    sellerId: listing.sellerId,
    listingId: listing.id,
  };
};

export const Marketplace = () => {
  const [searchParams] = useSearchParams();
  const [allListings, setAllListings] = useState<ListingDetailProps[]>([]);
  const [displayedListings, setDisplayedListings] = useState<ListingDetailProps[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const observerTarget = useRef<HTMLDivElement>(null);

  const ITEMS_PER_PAGE = 12;

  useEffect(() => {
    const fetchListings = async (searchQuery = '') => {
      setLoading(true);
      setError(null);
      try {
        console.log('Fetching listings from API...', searchQuery ? `with search: ${searchQuery}` : '');
        const data = await getListings(searchQuery);
        console.log('Listings fetched successfully:', data);
        
        // Filter listings by ACTIVE status only (no user filtering - marketplace is public)
        const activeListings = data
          .filter((listing) => {
            return listing.status === 'ACTIVE';
          })
          .map(mapListingToPostProps);
        
        setAllListings(activeListings);
        // Display first batch
        setDisplayedListings(activeListings.slice(0, ITEMS_PER_PAGE));
        setHasMore(activeListings.length > ITEMS_PER_PAGE);
      } catch (err: any) {
        console.error('Error fetching listings:', err);
        let errorMessage = 'Failed to load listings. Please try again later.';
        
        if (err.status === 403) {
          errorMessage = 'Access forbidden. Please ensure you are logged in or the listings endpoint is publicly accessible.';
        } else if (err.status === 401) {
          errorMessage = 'Authentication required. Please log in to view listings.';
        } else if (err.status === 404) {
          errorMessage = 'Listings endpoint not found. Please check the API configuration.';
        } else if (err.message) {
          errorMessage = err.message;
        }
        
        setError(errorMessage);
      } finally {
        setLoading(false);
      }
    };

    // Get search query from URL params
    const searchQuery = searchParams.get('search') || '';
    fetchListings(searchQuery);
  }, [searchParams]);

  const loadMore = useCallback(() => {
    if (loadingMore || !hasMore) return;

    setLoadingMore(true);
    
    // Simulate slight delay for smooth loading
    setTimeout(() => {
      const currentCount = displayedListings.length;
      const nextBatch = allListings.slice(currentCount, currentCount + ITEMS_PER_PAGE);
      
      if (nextBatch.length > 0) {
        setDisplayedListings(prev => [...prev, ...nextBatch]);
        setHasMore(currentCount + ITEMS_PER_PAGE < allListings.length);
      } else {
        setHasMore(false);
      }
      
      setLoadingMore(false);
    }, 300);
  }, [loadingMore, hasMore, displayedListings.length, allListings]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loadingMore) {
          loadMore();
        }
      },
      { threshold: 0.1 }
    );

    const currentTarget = observerTarget.current;
    if (currentTarget) {
      observer.observe(currentTarget);
    }

    return () => {
      if (currentTarget) {
        observer.unobserve(currentTarget);
      }
    };
  }, [hasMore, loadingMore, loadMore]);

  if (loading) {
    return (
      <div className="marketplace-container loading-container">
        <div className="loading-spinner"></div>
        <p style={{ marginTop: '1rem' }}>Loading listings...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="marketplace-container error-container">
        <p>Error: {error}</p>
        <button onClick={() => window.location.reload()}>
          Retry
        </button>
      </div>
    );
  }

  if (displayedListings.length === 0) {
    return (
      <div className="marketplace-container empty-container">
        <p>No active listings found.</p>
      </div>
    );
  }

  return (
    <>
      <div className="marketplace-container">
        {displayedListings.map((listing) => (
          <Post key={listing.listingId || listing.id} {...listing} />
        ))}
      </div>
      {hasMore && (
        <div ref={observerTarget} className="loading-container" style={{ padding: '2rem' }}>
          {loadingMore && (
            <>
              <div className="loading-spinner"></div>
              <p style={{ marginTop: '1rem' }}>Loading more listings...</p>
            </>
          )}
        </div>
      )}
    </>
  );
}