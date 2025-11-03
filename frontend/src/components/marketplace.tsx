import React, { useState, useEffect } from "react";
import Post from "./post.tsx";
import type {ListingDetailProps} from "./post.tsx";
import { getListings } from "../services/listingsService";
import '../css/Marketplace.css';

/**
 * Map API listing response to Post component props
 */
const mapListingToPostProps = (listing) => {
  // Parse images JSON string
  let imageUrl = "https://via.placeholder.com/150"; // default placeholder
  if (listing.images) {
    try {
      const images = JSON.parse(listing.images);
      imageUrl = Array.isArray(images) && images.length > 0 ? images[0] : imageUrl;
    } catch (e) {
      console.error('Error parsing images:', e);
    }
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
  const [listings, setListings] = useState<ListingDetailProps[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchListings = async () => {
      setLoading(true);
      setError(null);
      try {
        // Get logged-in user's ID from localStorage
        const userData = localStorage.getItem('auth.user');
        const user = userData ? JSON.parse(userData) : null;
        const userId = user?.id;

        if (!userId) {
          setError('Please log in to view your listings.');
          setLoading(false);
          return;
        }

        console.log('Fetching listings from API...');
        const data = await getListings();
        console.log('Listings fetched successfully:', data);
        
        // Filter listings by logged-in user's sellerId and ACTIVE status
        const userListings = data
          .filter((listing) => {
            // Filter by user's ID (sellerId matches user id) and ACTIVE status
            return listing.status === 'ACTIVE' && listing.sellerId === userId;
          })
          .map(mapListingToPostProps);
        
        setListings(userListings);
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
  }, []);

  if (loading) {
    return (
      <div className="marketplace-container">
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <p>Loading listings...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="marketplace-container">
        <div style={{ textAlign: 'center', padding: '2rem', color: '#dc2626' }}>
          <p>Error: {error}</p>
          <button 
            onClick={() => window.location.reload()} 
            style={{ marginTop: '1rem', padding: '0.5rem 1rem', cursor: 'pointer' }}
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  if (listings.length === 0) {
    return (
      <div className="marketplace-container">
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <p>No active listings found.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="marketplace-container">
      {listings.map((listing) => (
        <Post key={listing.id} {...listing} />
      ))}
    </div>
  );
}