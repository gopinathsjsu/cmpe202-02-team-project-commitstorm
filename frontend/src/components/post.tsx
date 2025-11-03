import React, { useState, useMemo } from 'react';
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
    const [post, setPost] = useState<ListingDetailProps>();

    useMemo( () => {
        setPost(props);
    }, [props]);

    function sendMessageToVendor(vendorId: string | number) {
        // Logic to send a message to the vendor
        console.log(`Message sent to vendor ${vendorId}`);
    }

    function reportPost(postId: string | number) {
        // Logic to report the post
        console.log(`Post ${postId} reported.`);
    }

    // Use sellerId if available (from API), otherwise fall back to userId
    const vendorId = post?.sellerId || post?.userId;

    return (
        <div className='container'>
            <img src={post?.imageUrl} alt={post?.title || "Post Image"} className="post-image" />
            <div className="post-content">
                <div className="description">
                    {post?.title && <h2>{post.title}</h2>}
                    <p>Condition: {post?.condition}</p>
                    <p>Category: {post?.category}</p>
                    <h3>Description:</h3>
                    {post?.description}
                </div>
                <div className="post-details">
                    <div className="post-detail-item">
                        Vendor: {post?.username}
                    </div>
                    <div className="post-detail-item">
                        Price: ${post?.price?.toFixed(2)}
                    </div>
                </div>
            </div>
            <div className="button-group">
                <button className="post-button" onClick={() => vendorId && sendMessageToVendor(vendorId)}>
                    Message Vendor
                </button>
                <button className="post-button" onClick={() => post?.listingId && reportPost(post.listingId)}>
                    Report Post
                </button>
            </div>
            
        </div>
    );
}

export default Post;