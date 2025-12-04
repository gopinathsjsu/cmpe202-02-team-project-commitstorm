import React, { useState, useEffect, useRef, useCallback } from "react";
import { useSearchParams } from "react-router";
import Post from "./post.tsx";
import type {ListingDetailProps} from "./post.tsx";
import { 
  getListings, 
  getListingsBySeller,
  getListingsByCategory,
  getListingsByStatus,
  getListingsByCondition,
  getListingsByPriceRange
} from "../services/listingsService";
import Loader from "./Loader";
import ListingFilters from "./ListingFilters";
import '../css/Marketplace.css';

type MarketplaceProps = {
  onMessageVendor?: (data: { listingId: string, recipientId: string, recipientName: string, listingTitle: string }) => void;
  onReportPost?: (data: { listingId: string, listingTitle: string }) => void;
};

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
          images = [JSON.parse(trimmedImages)];
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
    sellerId: listing.sellerId, // This is the seller's user ID (userId1)
    listingId: listing.id, // This is the actual listing ID from API
  };
};

export const Marketplace = ({ onMessageVendor, onReportPost }: MarketplaceProps) => {
  const [searchParams] = useSearchParams();
  const [allListings, setAllListings] = useState<ListingDetailProps[]>([]);
  const [displayedListings, setDisplayedListings] = useState<ListingDetailProps[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const [filters, setFilters] = useState({
    status: '',
    condition: '',
    categoryId: '',
    sellerId: '',
    minPrice: '',
    maxPrice: '',
  });
  const observerTarget = useRef<HTMLDivElement>(null);

  const ITEMS_PER_PAGE = 12;
  const filterTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Fetch listings with filters
  useEffect(() => {
    // Clear any existing timeout
    if (filterTimeoutRef.current) {
      clearTimeout(filterTimeoutRef.current);
    }

    // For price filters, debounce the API call
    const hasPriceInput = filters.minPrice || filters.maxPrice;
    const delay = hasPriceInput ? 800 : 0; // Wait 800ms for price inputs, immediate for others

    filterTimeoutRef.current = setTimeout(() => {
      const fetchListings = async () => {
        setLoading(true);
        setError(null);
        try {
          const searchQuery = searchParams.get('search') || '';
          let data: any[] = [];

          // Determine which API endpoint to use based on active filters
          // Price range requires both min and max to be set
          const hasPriceRange = filters.minPrice && filters.maxPrice;
          const hasFilters = filters.status || filters.condition || filters.categoryId || filters.sellerId || hasPriceRange;

        if (hasFilters) {
          // Apply filters sequentially (combine results)
          const filterPromises: Promise<any[]>[] = [];

          if (filters.sellerId) {
            filterPromises.push(getListingsBySeller(filters.sellerId));
          }
          if (filters.categoryId) {
            filterPromises.push(getListingsByCategory(filters.categoryId));
          }
          if (filters.status) {
            filterPromises.push(getListingsByStatus(filters.status));
          }
          if (filters.condition) {
            filterPromises.push(getListingsByCondition(filters.condition));
          }
          // Only call price range API if both min and max are provided
          if (hasPriceRange) {
            const minPrice = Number(filters.minPrice);
            const maxPrice = Number(filters.maxPrice);
            filterPromises.push(getListingsByPriceRange(minPrice, maxPrice));
          }

          // If multiple filters, get results from each and find intersection
          if (filterPromises.length > 0) {
            const results: any[][] = await Promise.all(filterPromises);
            // Find listings that match ALL filters (intersection)
            if (results.length === 1) {
              data = results[0];
            } else {
              // Find common listings across all filter results
              const listingIds = results.map(result => 
                new Set(result.map((listing: any) => listing.id))
              );
              const commonIds = listingIds.reduce((acc, curr) => {
                return new Set([...acc].filter(id => curr.has(id)));
              });
              data = results[0].filter((listing: any) => commonIds.has(listing.id));
            }
          } else {
            // No filters, use regular search
            data = await getListings(searchQuery);
          }
        } else {
          // No filters, use regular search
          data = await getListings(searchQuery);
        }

        console.log('Listings fetched successfully:', data);
        
        // Map listings to Post component props
        const mappedListings = data.map(mapListingToPostProps);
        
        setAllListings(mappedListings);
        // Display first batch
        setDisplayedListings(mappedListings.slice(0, ITEMS_PER_PAGE));
        setHasMore(mappedListings.length > ITEMS_PER_PAGE);
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

      fetchListings();
    }, delay);

    return () => {
      if (filterTimeoutRef.current) {
        clearTimeout(filterTimeoutRef.current);
      }
    };
  }, [searchParams, filters]);

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
      <div className="min-h-screen flex items-center justify-center">
        <Loader text="Loading listings..." />
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

  const handleFilterChange = (newFilters: any) => {
    setFilters(newFilters);
    // Reset pagination when filters change
    setDisplayedListings([]);
    setHasMore(true);
  };

  const handleClearFilters = () => {
    setFilters({
      status: '',
      condition: '',
      categoryId: '',
      sellerId: '',
      minPrice: '',
      maxPrice: '',
    });
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <ListingFilters 
        filters={filters}
        onFilterChange={handleFilterChange}
        onClearFilters={handleClearFilters}
      />
      
      <div className="marketplace-container">
        {displayedListings.map((listing) => (
          <Post key={listing.listingId || listing.id} {...listing} onMessageVendor={onMessageVendor} onReportPost={onReportPost} />
        ))}
      </div>
      {hasMore && (
        <div ref={observerTarget} style={{ padding: '2rem' }}>
          {loadingMore && (
            <Loader text="Loading more listings..." />
          )}
        </div>
      )}
    </div>
  );
}