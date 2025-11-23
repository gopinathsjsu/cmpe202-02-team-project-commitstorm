-- V5: Add Performance Indexes
-- Optimize queries for listings, search, and common lookups
-- Note: MySQL 8.0.42 on RDS doesn't support IF NOT EXISTS for CREATE INDEX
-- So we create indexes directly - they will fail if they already exist, but most don't exist yet

-- Indexes for listings table
CREATE INDEX idx_listings_category_id ON listings(category_id);
CREATE INDEX idx_listings_seller_id ON listings(seller_id);
CREATE INDEX idx_listings_status ON listings(status);
CREATE INDEX idx_listings_condition ON listings(`condition`);
CREATE INDEX idx_listings_price ON listings(price);
CREATE INDEX idx_listings_created_at ON listings(created_at);

-- Fulltext index for search (title and description)
-- Note: Skipped - FULLTEXT index creation fails if index already exists
-- This can be added manually if fulltext search is needed
-- ALTER TABLE listings ADD FULLTEXT INDEX ft_listings_search (title, description);

-- Indexes for messages table
CREATE INDEX idx_messages_listing_id ON messages(listing_id);
CREATE INDEX idx_messages_from_user_id ON messages(from_user_id);
CREATE INDEX idx_messages_to_user_id ON messages(to_user_id);
CREATE INDEX idx_messages_is_read ON messages(is_read);
CREATE INDEX idx_messages_created_at ON messages(created_at);

-- Indexes for transactions table
CREATE INDEX idx_transactions_listing_id ON transactions(listing_id);
CREATE INDEX idx_transactions_buyer_id ON transactions(buyer_id);
CREATE INDEX idx_transactions_status ON transactions(status);

-- Indexes for reviews table
CREATE INDEX idx_reviews_transaction_id ON reviews(transaction_id);
CREATE INDEX idx_reviews_reviewer_id ON reviews(reviewer_id);
CREATE INDEX idx_reviews_seller_id ON reviews(seller_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- Indexes for reports table
CREATE INDEX idx_reports_reporter_id ON reports(reporter_id);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_reports_target_type ON reports(target_type);
CREATE INDEX idx_reports_target_id ON reports(target_id);
CREATE INDEX idx_reports_moderator_id ON reports(moderator_id);

-- Indexes for wishlist table
CREATE INDEX idx_wishlist_user_id ON wishlist(user_id);
CREATE INDEX idx_wishlist_listing_id ON wishlist(listing_id);

-- Indexes for follows table
-- Note: idx_follower and idx_seller already exist from V4, but these have different names
CREATE INDEX idx_follows_follower_id ON follows(follower_id);
CREATE INDEX idx_follows_seller_id ON follows(seller_id);

-- Indexes for users table
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);