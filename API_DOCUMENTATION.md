# Campus Marketplace API Documentation

## Overview
This is a Spring Boot REST API for a campus marketplace application that allows students to buy and sell items within their campus community.

## Base URL
```
http://ec2-16-146-79-242.us-west-2.compute.amazonaws.com/api
```

Swagger UI (deployed): `http://ec2-16-146-79-242.us-west-2.compute.amazonaws.com/swagger-ui/index.html`
## API Endpoints

### Authentication
- `POST /api/auth/register` – Register a new user account
- `POST /api/auth/login` – Authenticate and receive a JWT token
- `GET /api/auth/me` – Return the user associated with the bearer token
- `POST /api/auth/logout` – Client-side logout hint for JWT sessions

### Users
- `POST /api/users` - Create a new user
- `GET /api/users` - Get all users *(ADMIN only)*
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `GET /api/users/role/{role}` - Get users by role (USER, ADMIN)
- `GET /api/users/status/{status}` - Get users by status (ACTIVE, SUSPENDED, BANNED)
- `GET /api/users/search/name?name={name}` - Search users by name
- `GET /api/users/search/email?email={email}` - Search users by email
- `GET /api/users/exists/email?email={email}` - Check if email already exists
- `PUT /api/users/{id}` - Update user
- `PATCH /api/users/{id}/status?status={status}` - Update user status *(ADMIN only)*
- `DELETE /api/users/{id}` - Delete user *(ADMIN only)*

### Categories
- `POST /api/categories` - Create a new category
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `GET /api/categories/name/{name}` - Get category by exact name
- `GET /api/categories/search?name={name}` - Search categories by partial name
- `GET /api/categories/exists/name?name={name}` - Check if a category name already exists
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

### Listings
- `POST /api/listings` - Create a new listing
- `GET /api/listings` - Get all listings
- `GET /api/listings/{id}` - Get listing by ID
- `GET /api/listings/seller/{sellerId}` - Get listings by seller
- `GET /api/listings/category/{categoryId}` - Get listings by category
- `GET /api/listings/status/{status}` - Get listings by status (ACTIVE, SOLD, PENDING, DRAFT)
- `GET /api/listings/condition/{condition}` - Get listings by condition (NEW, LIKE_NEW, GOOD, FAIR, POOR)
- `GET /api/listings/price-range?minPrice={min}&maxPrice={max}` - Filter by price range
- `GET /api/listings/search?searchTerm={term}` - Search listings by title or description
- `GET /api/listings/seller/{sellerId}/status/{status}` - Get listings by seller and status
- `GET /api/listings/category/{categoryId}/status/{status}?page={page}&size={size}` - Paginated listings by category and status
- `GET /api/listings/status/{status}/page?page={page}&size={size}` - Paginated listings by status
- `GET /api/listings/seller/{sellerId}/page?page={page}&size={size}` - Paginated listings by seller
- `GET /api/listings/search/advanced?searchTerm={term}&categoryId={id}&minPrice={min}&maxPrice={max}&condition={condition}&status={status}&sortBy={sort}&page={page}&size={size}` - Combined keyword search, filters, sort, and pagination
- `POST /api/listings/chatbot-search` - Natural-language search (body `{ "query": "..." }`)
- `GET /api/listings/chatbot-search?query={query}` - Natural-language search via query parameter
- `PUT /api/listings/{id}` - Update listing
- `PATCH /api/listings/{id}/status?status={status}` - Update listing status
- `DELETE /api/listings/{id}` - Delete listing

### Follows
- `POST /api/follows?followerId={id}&sellerId={id}` - Follow a seller
- `DELETE /api/follows?followerId={id}&sellerId={id}` - Unfollow a seller
- `GET /api/follows/check?followerId={id}&sellerId={id}` - Check following status
- `GET /api/follows/following/{followerId}` - Get sellers followed by a user
- `GET /api/follows/followers/{sellerId}` - Get followers for a seller
- `GET /api/follows/followers/{sellerId}/count` - Get follower count for a seller
- `GET /api/follows/following/{followerId}/count` - Get following count for a user

### Wishlist
- `POST /api/wishlist` - Add a listing to a user's wishlist (JSON body with `userId` and `listingId`)
- `GET /api/wishlist/user/{userId}` - Get the full wishlist for a user
- `GET /api/wishlist/{userId}/{listingId}` - Check if a listing is wishlisted by a user
- `GET /api/wishlist/user/{userId}/count` - Get wishlist count for a user
- `GET /api/wishlist/listing/{listingId}/count` - Get wishlist count for a listing
- `GET /api/wishlist/listing/{listingId}` - Get all users who wishlisted a listing
- `DELETE /api/wishlist/{userId}/{listingId}` - Remove a listing from a wishlist
- `DELETE /api/wishlist/user/{userId}` - Clear a user's entire wishlist

### Messages
- `POST /api/messages` - Send a message (requires JWT)
- `GET /api/messages/{id}` - Get message by ID (requires JWT)
- `GET /api/messages/listing/{listingId}` - Get messages tied to a listing (requires JWT)
- `GET /api/messages/conversation/{userId1}/{userId2}` - Get conversation between two users (requires JWT)
- `GET /api/messages/conversation/listing/{listingId}/{userId1}/{userId2}` - Get conversation for a specific listing (requires JWT)
- `GET /api/messages/conversation/listing/{listingId}/{userId1}/{userId2}/page?page={page}&size={size}` - Paginated listing conversation (requires JWT)
- `GET /api/messages/sent/{userId}?page={page}&size={size}` - Get messages sent by a user (user can only access their own data)
- `GET /api/messages/received/{userId}?page={page}&size={size}` - Get messages received by a user
- `GET /api/messages/user/{userId}?page={page}&size={size}` - Get all messages (sent + received) for a user
- `GET /api/messages/partners/{userId}` - Get conversation partners for a user
- `GET /api/messages/unread/count/{userId}` - Get unread message count
- `GET /api/messages/unread/{userId}` - Get unread messages
- `PATCH /api/messages/{messageId}/mark-read` - Mark a message as read
- `PATCH /api/messages/mark-all-read/{userId}` - Mark all of a user's messages as read
- `DELETE /api/messages/{id}` - Delete a message (sender only)

### Transactions
- `POST /api/transactions` - Create a new transaction
- `POST /api/transactions/request-to-buy?listingId={id}&buyerId={id}` - Buyer requests to buy (creates PENDING transaction)
- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `GET /api/transactions/listing/{listingId}` - Get transaction by listing ID
- `GET /api/transactions/buyer/{buyerId}` - Get transactions by buyer
- `GET /api/transactions/seller/{sellerId}` - Get transactions by seller
- `GET /api/transactions/seller/{sellerId}/status/{status}` - Get transactions by seller and status
- `GET /api/transactions/status/{status}` - Get transactions by status (PENDING, COMPLETED, CANCELLED, REFUNDED)
- `PUT /api/transactions/{id}` - Update transaction
- `PATCH /api/transactions/{id}/status?status={status}` - Update transaction status
- `PATCH /api/transactions/{transactionId}/mark-sold?sellerId={id}` - Seller accepts request (marks as COMPLETED)
- `PATCH /api/transactions/{transactionId}/reject?sellerId={id}` - Seller rejects request (marks as CANCELLED)
- `DELETE /api/transactions/{id}` - Delete transaction

### Reviews
- `POST /api/reviews` - Create a new review
- `GET /api/reviews` - Get all reviews
- `GET /api/reviews/{id}` - Get review by ID
- `GET /api/reviews/transaction/{transactionId}` - Get review by transaction ID
- `GET /api/reviews/reviewer/{reviewerId}` - Get reviews by reviewer
- `GET /api/reviews/seller/{sellerId}` - Get reviews by seller
- `GET /api/reviews/rating/{rating}` - Get reviews by rating (1-5)
- `GET /api/reviews/seller/{sellerId}/rating/{rating}` - Get reviews by seller and rating
- `GET /api/reviews/seller/{sellerId}/average-rating` - Get average rating for seller
- `GET /api/reviews/seller/{sellerId}/count` - Get review count for seller
- `PUT /api/reviews/{id}` - Update review
- `DELETE /api/reviews/{id}` - Delete review

### Reports
- `POST /api/reports` - Create a new report
- `GET /api/reports` - Get all reports *(ADMIN only)*
- `GET /api/reports/{id}` - Get report by ID
- `GET /api/reports/reporter/{reporterId}` - Get reports by reporter
- `GET /api/reports/moderator/{moderatorId}` - Get reports by moderator
- `GET /api/reports/status/{status}` - Get reports by status (OPEN, IN_REVIEW, RESOLVED, ACTIONED)
- `GET /api/reports/target-type/{targetType}` - Get reports by target type (LISTING, USER)
- `GET /api/reports/target/{targetId}` - Get reports by target ID
- `GET /api/reports/target-type/{targetType}/status/{status}` - Get reports by target type and status
- `PUT /api/reports/{id}` - Update report
- `PATCH /api/reports/{id}/assign-moderator?moderatorId={moderatorId}` - Assign moderator to report *(ADMIN only)*
- `PATCH /api/reports/{id}/status?status={status}` - Update report status *(ADMIN only)*
- `DELETE /api/reports/{id}` - Delete report *(ADMIN only)*

### Images
- `POST /api/images/presigned-url` - Generate a presigned URL for uploading to S3 (requires JWT)
- `POST /api/images/presigned-url/batch` - Generate up to 10 presigned URLs at once (requires JWT)
- `DELETE /api/images?imageUrl={url}` - Delete an image from S3 by its public URL (requires JWT)

### Health
- `GET /api/health` - API + database health check endpoint

## Data Models

### User
```json
{
  "id": "string (UUID)",
  "name": "string",
  "email": "string (email format)",
  "role": "USER | ADMIN",
  "status": "ACTIVE | SUSPENDED | BANNED",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Category
```json
{
  "id": "string (UUID)",
  "name": "string"
}
```

### Listing
```json
{
  "id": "string (UUID)",
  "sellerId": "string (UUID)",
  "sellerName": "string",
  "title": "string",
  "description": "string",
  "price": "decimal",
  "categoryId": "string (UUID)",
  "categoryName": "string",
  "condition": "NEW | LIKE_NEW | GOOD | FAIR | POOR",
  "images": "string (JSON)",
  "status": "ACTIVE | SOLD | PENDING | DRAFT",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Message
```json
{
  "id": "string (UUID)",
  "listingId": "string (UUID)",
  "fromUserId": "string (UUID)",
  "fromUserName": "string",
  "toUserId": "string (UUID)",
  "toUserName": "string",
  "content": "string",
  "createdAt": "datetime"
}
```

### Transaction
```json
{
  "id": "string (UUID)",
  "listingId": "string (UUID)",
  "listingTitle": "string",
  "buyerId": "string (UUID)",
  "buyerName": "string",
  "sellerId": "string (UUID)",
  "sellerName": "string",
  "finalPrice": "decimal",
  "status": "PENDING | COMPLETED | CANCELLED | REFUNDED",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### Review
```json
{
  "id": "string (UUID)",
  "transactionId": "string (UUID)",
  "reviewerId": "string (UUID)",
  "reviewerName": "string",
  "sellerId": "string (UUID)",
  "sellerName": "string",
  "rating": "integer (1-5)",
  "comment": "string",
  "createdAt": "datetime"
}
```

### Report
```json
{
  "id": "string (UUID)",
  "reporterId": "string (UUID)",
  "reporterName": "string",
  "targetType": "LISTING | USER",
  "targetId": "string (UUID)",
  "reason": "string",
  "status": "OPEN | IN_REVIEW | RESOLVED | ACTIONED",
  "moderatorId": "string (UUID)",
  "moderatorName": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

## Error Handling
The API returns appropriate HTTP status codes and error messages:
- `400 Bad Request` - Invalid input data or validation errors
- `403 Forbidden` - Access denied (for ADMIN-only endpoints or insufficient permissions)
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

Error response format:
```json
{
  "timestamp": "datetime",
  "status": "integer",
  "error": "string",
  "message": "string",
  "path": "string",
  "validationErrors": "object (for validation errors)"
}
```

## Swagger Documentation
API documentation is available at: `http://localhost:8080/swagger-ui.html`

## Database
The application uses MySQL database with Flyway migrations for schema management. The database schema is defined in `V1__init_mysql.sql`.

## Sample Data
The application automatically initializes with sample data including:
- Admin user: admin@campusmarket.com
- Sample users: john.doe@university.edu, jane.smith@university.edu
- Predefined categories: Electronics, Books, Clothing, etc.
