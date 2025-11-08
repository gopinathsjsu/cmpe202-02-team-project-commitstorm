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
}

function Post( props: ListingDetailProps ) {
    const [imageError, setImageError] = useState(false);
    const [imageLoading, setImageLoading] = useState(true);

    // Reset image states when props change
    useEffect(() => {
        setImageLoading(true);
        setImageError(false);
    }, [props.imageUrl]);

    function sendMessageToVendor(vendorId: string | number) {
        // Logic to send a message to the vendor
        console.log(`Message sent to vendor ${vendorId}`);
    }

    function reportPost(postId: string | number) {
        // Logic to report the post
        console.log(`Post ${postId} reported.`);
    }

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
                    src={imageError ? 'https://via.placeholder.com/400x200?text=No+Image' : (props?.imageUrl || 'https://via.placeholder.com/400x200?text=No+Image')} 
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
                        console.error('Image failed to load:', props?.imageUrl);
                        setImageError(true);
                        setImageLoading(false);
                        // Force load placeholder
                        e.currentTarget.src = 'https://via.placeholder.com/400x200?text=No+Image';
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