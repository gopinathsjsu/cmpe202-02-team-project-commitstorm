import { useState, useMemo } from 'react';
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
}

function Post( props: ListingDetailProps ) {
    const [post, setPost] = useState<ListingDetailProps>();

    useMemo( () => {
        setPost(props);
    }, [props]);

    function sendMessageToVendor(vendorId: number) {
        // Logic to send a message to the vendor
        console.log(`Message sent to vendor ${vendorId}`);
    }

    function reportPost(postId: number) {
        // Logic to report the post
        console.log(`Post ${postId} reported.`);
    }

    return (
        <div className='container'>
            <img src={post?.imageUrl} alt="Post Image" className="post-image" />
            <div className="post-content">
                <div className="description">
                    <p> Condition: {post?.condition} </p>
                    <h2>Description:</h2>
                    {post?.description}
                </div>
                <div className="post-details">
                    <div className="post-detail-item">
                        Vendor: {post?.username}
                    </div>
                    <div className="post-detail-item">
                        Price: ${post?.price}
                    </div>
                </div>
            </div>
            <div className="button-group">
                <button className="post-button" onClick={() => sendMessageToVendor(props.userId)}>Message Vendor</button>
                <button className="post-button" onClick={() => reportPost}>Report Post</button>
            </div>
            
        </div>
    );
}

export default Post;