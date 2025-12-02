import React, { useState, useEffect } from 'react';
import '../css/Post.css';

export type ListingDetailProps = {
    id: number,
    userId: number,
    username: string,
    description: string,
    imageUrl: string,
    createdAt: string,
    updatedAt: string,
    price: number,
    category: string,
    condition: string,
    title?: string,
    sellerId?: string,
    listingId?: string,
    onMessageVendor?: (data: { listingId: string, recipientId: string, recipientName: string, listingTitle: string }) => void;
    onReportPost?: (data: { listingId: string, listingTitle: string }) => void;
}

function Post( props: ListingDetailProps ) {
    const [imageError, setImageError] = useState(false);
    const [imageLoading, setImageLoading] = useState(true);
    const [imageSrc, setImageSrc] = useState<string>(props?.imageUrl || 'https://via.placeholder.com/400x200?text=No+Image');
    const errorHandledRef = React.useRef(false);

    // Reset image states when props change
    useEffect(() => {
        setImageLoading(true);
        setImageError(false);
        setImageSrc(props?.imageUrl || 'https://via.placeholder.com/400x200?text=No+Image');
        errorHandledRef.current = false;
    }, [props.imageUrl]);

    const sendMessageToVendor = (vendorId: string | number) => {
        // Check if user is logged in
        const userData = localStorage.getItem('auth.user');
        if (!userData) {
            alert('Please log in to message vendors');
            return;
        }

        // listingId should be the actual listing.id from the API (UUID string)
        // This is already set in marketplace.tsx as listingId: listing.id
        const listingId = props.listingId;
        
        if (!props.onMessageVendor || !listingId) {
            console.log('Cannot send message: missing data', { listingId, hasCallback: !!props.onMessageVendor });
            return;
        }
        
        // vendorId (sellerId) is userId1, current user (from login) will be userId2
        props.onMessageVendor({
            listingId: String(listingId), // Actual listing ID from API
            recipientId: String(vendorId), // sellerId (userId1)
            recipientName: props.username || 'Vendor',
            listingTitle: props.title || 'Listing',
        });
    };

    const reportPost = (listingId: string | number) => {
        // Check if user is logged in
        const userData = localStorage.getItem('auth.user');
        if (!userData) {
            alert('Please log in to report posts');
            return;
        }

        if (!props.onReportPost || !listingId) {
            console.log('Cannot report post: missing data');
            return;
        }
        
        props.onReportPost({
            listingId: String(listingId),
            listingTitle: props.title || 'Listing',
        });
    };

    // Use sellerId if available (from API), otherwise fall back to userId
    const vendorId = props?.sellerId || props?.userId;

    return (
        <div className='container'>
            <div style={{ position: 'relative', width: '100%', height: '200px', marginBottom: '12px', borderRadius: '8px', backgroundColor: '#f3f4f6', overflow: 'hidden' }}>
                {imageLoading && !imageError && (
                    <div style={{ 
                        position: 'absolute', 
                        top: 0, 
                        left: 0, 
                        width: '100%', 
                        height: '100%', 
                        display: 'flex', 
                        alignItems: 'center', 
                        justifyContent: 'center',
                        backgroundColor: '#f3f4f6',
                        color: '#9ca3af',
                        zIndex: 1
                    }}>
                        Loading image...
                    </div>
                )}
                <img 
                    src={imageSrc || 'https://via.placeholder.com/400x200?text=No+Image'} 
                    alt={props?.title || "Post Image"} 
                    className="post-image"
                    style={{ 
                        display: 'block',
                        opacity: imageLoading ? 0 : 1,
                        transition: 'opacity 0.3s ease'
                    }}
                    onLoad={() => {
                        setImageLoading(false);
                        setImageError(false);
                    }}
                    onError={(e) => {
                        // Prevent infinite loop by checking if we've already handled this error
                        if (errorHandledRef.current) {
                            return;
                        }
                        
                        // Only log error if it's not the placeholder
                        if (props?.imageUrl && props.imageUrl !== 'https://via.placeholder.com/400x200?text=No+Image') {
                            console.error('Image failed to load:', props?.imageUrl);
                        }
                        
                        errorHandledRef.current = true;
                        setImageError(true);
                        setImageLoading(false);
                        
                        // Set placeholder only if current src is not already the placeholder
                        const placeholderUrl = 'https://via.placeholder.com/400x200?text=No+Image';
                        if (e.currentTarget.src !== placeholderUrl) {
                            setImageSrc(placeholderUrl);
                        }
                    }}
                />
            </div>
            <div className="post-content">
                <div className="description">
                    {props?.title && <h2>{props.title}</h2>}
                    <p>Condition: {props?.condition}</p>
                    <p>Category: {props?.category}</p>
                    <h3>Description</h3>
                    <div>{props?.description}</div>
                </div>
                <div className="post-details">
                    <div className="post-detail-item">
                        Vendor: {props?.username}
                    </div>
                    <div className="post-detail-item">
                        ${props?.price?.toFixed(2)}
                    </div>
                </div>
            </div>
            <div className="button-group">
                <button className="post-button" onClick={() => vendorId && sendMessageToVendor(vendorId)}>
                    Message Vendor
                </button>
                <button className="post-button" onClick={() => props?.listingId && reportPost(props.listingId)}>
                    Report Post
                </button>
            </div>
        </div>
    );
}

export default Post;