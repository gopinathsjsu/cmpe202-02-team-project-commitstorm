-- Test Data for Campus Marketplace APIs
-- AWS RDS database to populate with test data
-- Will then verify API w/ shell commands 
/*
    # Test Users API
    curl -s http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com//api/users

    # Test Categories API  
    curl -s http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com//api/categories

    # Test Listings API
    curl -s http://alb-cmpmarket-public-1403545222.us-west-2.elb.amazonaws.com//api/listings
*/

USE campusMarket;

-- Clear existing test data for this test, can do multiple times
DELETE FROM listings WHERE seller_id IN (SELECT id FROM users WHERE email LIKE '%@apitest1.com');
DELETE FROM users WHERE email LIKE '%@apitest1.com';

-- Inserting additional test users
INSERT INTO users (id, name, email, role, status, created_at, updated_at) VALUES
(UUID(), 'Alex Johnson', 'alex.johnson@apitest1.com', 'USER', 'ACTIVE', NOW(), NOW()),
(UUID(), 'Sarah Chen', 'sarah.chen@apitest1.com', 'USER', 'ACTIVE', NOW(), NOW()),
(UUID(), 'Mike Rodriguez', 'mike.rodriguez@apitest1.com', 'USER', 'ACTIVE', NOW(), NOW()),
(UUID(), 'Emma Wilson', 'emma.wilson@apitest1.com', 'ADMIN', 'ACTIVE', NOW(), NOW()),
(UUID(), 'David Kim', 'david.kim@apitest1.com', 'USER', 'SUSPENDED', NOW(), NOW()),
(UUID(), 'Lisa Thompson', 'lisa.thompson@apitest1.com', 'USER', 'ACTIVE', NOW(), NOW()),
(UUID(), 'Chris Brown', 'chris.brown@apitest1.com', 'USER', 'ACTIVE', NOW(), NOW());

-- Inserting test listings
SET @alex_id = (SELECT id FROM users WHERE email = 'alex.johnson@apitest1.com' LIMIT 1);
SET @sarah_id = (SELECT id FROM users WHERE email = 'sarah.chen@apitest1.com' LIMIT 1);
SET @mike_id = (SELECT id FROM users WHERE email = 'mike.rodriguez@apitest1.com' LIMIT 1);
SET @lisa_id = (SELECT id FROM users WHERE email = 'lisa.thompson@apitest1.com' LIMIT 1);
SET @chris_id = (SELECT id FROM users WHERE email = 'chris.brown@apitest1.com' LIMIT 1);

SET @electronics_id = (SELECT id FROM categories WHERE name = 'Electronics' LIMIT 1);
SET @books_id = (SELECT id FROM categories WHERE name = 'Books' LIMIT 1);
SET @clothing_id = (SELECT id FROM categories WHERE name = 'Clothing' LIMIT 1);
SET @furniture_id = (SELECT id FROM categories WHERE name = 'Furniture' LIMIT 1);
SET @sports_id = (SELECT id FROM categories WHERE name = 'Sports' LIMIT 1);

-- Inserting test listings
INSERT INTO listings (id, seller_id, title, description, price, category_id, `condition`, images, status, created_at, updated_at) VALUES
(UUID(), @lisa_id, 'iPhone 13 Pro', 'Excellent condition iPhone 13 Pro, 128GB, Space Gray. Includes original charger and case.', 800.00, @electronics_id, 'LIKE_NEW', '["https://example.com/iphone1.jpg", "https://example.com/iphone2.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @chris_id, 'Calculus Textbook', 'Calculus: Early Transcendentals, 8th Edition. Used but in good condition with some highlighting.', 75.00, @books_id, 'GOOD', '["https://example.com/calculus1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @alex_id, 'Nike Air Max', 'Nike Air Max 270, Size 10, Black/White. Worn a few times, excellent condition.', 120.00, @clothing_id, 'LIKE_NEW', '["https://example.com/nike1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @sarah_id, 'Office Chair', 'Ergonomic office chair, adjustable height, lumbar support. Great for studying!', 150.00, @furniture_id, 'GOOD', '["https://example.com/chair1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @lisa_id, 'Basketball', 'Spalding basketball, good condition, perfect for pickup games.', 25.00, @sports_id, 'GOOD', '["https://example.com/basketball1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @chris_id, 'MacBook Air M1', 'MacBook Air with M1 chip, 8GB RAM, 256GB SSD. Lightly used, excellent condition.', 900.00, @electronics_id, 'LIKE_NEW', '["https://example.com/macbook1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @alex_id, 'Winter Jacket', 'North Face winter jacket, Size M, Black. Perfect for cold weather.', 80.00, @clothing_id, 'GOOD', '["https://example.com/jacket1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @sarah_id, 'Desk Lamp', 'LED desk lamp with adjustable brightness and USB charging port.', 35.00, @furniture_id, 'NEW', '["https://example.com/lamp1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @lisa_id, 'Chemistry Lab Kit', 'Complete chemistry lab kit, barely used, includes all safety equipment.', 200.00, @books_id, 'LIKE_NEW', '["https://example.com/chem1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @chris_id, 'Yoga Mat', 'Premium yoga mat, non-slip, includes carrying strap.', 45.00, @sports_id, 'GOOD', '["https://example.com/yoga1.jpg"]', 'ACTIVE', NOW(), NOW());

-- Inserting listings with different statuses
INSERT INTO listings (id, seller_id, title, description, price, category_id, `condition`, images, status, created_at, updated_at) VALUES
(UUID(), @alex_id, 'Sold Textbook', 'This item has been sold', 50.00, @books_id, 'GOOD', '[]', 'SOLD', NOW(), NOW()),
(UUID(), @sarah_id, 'Draft Listing', 'This is a draft listing', 100.00, @electronics_id, 'NEW', '[]', 'DRAFT', NOW(), NOW()),
(UUID(), @lisa_id, 'Pending Sale', 'This item is pending sale', 75.00, @clothing_id, 'LIKE_NEW', '[]', 'PENDING', NOW(), NOW());

-- Inserting listings with different conditions for testing
INSERT INTO listings (id, seller_id, title, description, price, category_id, `condition`, images, status, created_at, updated_at) VALUES
(UUID(), @chris_id, 'Brand New Laptop', 'Brand new laptop, never opened', 1200.00, @electronics_id, 'NEW', '["https://example.com/laptop1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @alex_id, 'Fair Condition Bike', 'Bike in fair condition, needs some maintenance', 80.00, @sports_id, 'FAIR', '["https://example.com/bike1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @sarah_id, 'Poor Condition Phone', 'Old phone in poor condition, good for parts', 30.00, @electronics_id, 'POOR', '["https://example.com/phone1.jpg"]', 'ACTIVE', NOW(), NOW());

-- Inserting price range test data
INSERT INTO listings (id, seller_id, title, description, price, category_id, `condition`, images, status, created_at, updated_at) VALUES
(UUID(), @lisa_id, 'Expensive Watch', 'Luxury watch, very expensive', 2500.00, @clothing_id, 'NEW', '["https://example.com/watch1.jpg"]', 'ACTIVE', NOW(), NOW()),
(UUID(), @chris_id, 'Cheap Pen', 'Basic pen, very cheap', 2.00, @books_id, 'NEW', '[]', 'ACTIVE', NOW(), NOW()),
(UUID(), @alex_id, 'Mid-range Headphones', 'Good quality headphones at mid-range price', 150.00, @electronics_id, 'LIKE_NEW', '["https://example.com/headphones1.jpg"]', 'ACTIVE', NOW(), NOW());