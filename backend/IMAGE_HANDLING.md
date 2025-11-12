# Image Handling - Implementation Guide

## 📸 Current Implementation

### Storage Method
- **Database Column**: `images` (JSON type in MySQL)
- **Data Format**: JSON array of image URLs stored as a **String**
- **Example**: `'["https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=800"]'`

## 🚀 S3 Image Upload (Production-Ready)

### Overview
The application now supports **S3 presigned URLs** for secure, direct image uploads to AWS S3.

### Architecture
```
Frontend → Backend (Get Presigned URL) → Frontend → S3 (Direct Upload) → Backend (Store URL in DB)
```

### Setup

1. **Create S3 Bucket**:
   ```bash
   aws s3 mb s3://campus-marketplace-images --region us-west-2
   ```

2. **Configure Bucket Policy** (for public read access):
   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "PublicReadGetObject",
         "Effect": "Allow",
         "Principal": "*",
         "Action": "s3:GetObject",
         "Resource": "arn:aws:s3:::campus-marketplace-images/*"
       }
     ]
   }
   ```

3. **Set CORS Policy** (for browser uploads):
   ```json
   [
     {
       "AllowedHeaders": ["*"],
       "AllowedMethods": ["PUT", "POST"],
       "AllowedOrigins": ["*"],
       "ExposeHeaders": ["ETag"],
       "MaxAgeSeconds": 3000
     }
   ]
   ```

4. **Configure Environment Variables**:
   ```bash
   AWS_ACCESS_KEY_ID=your-access-key
   AWS_SECRET_ACCESS_KEY=your-secret-key
   AWS_REGION=us-west-2
   AWS_S3_BUCKET_NAME=campus-marketplace-images
   AWS_S3_PRESIGNED_URL_EXPIRY_MINUTES=15
   ```

### API Endpoints

#### 1. Generate Presigned URL (Single Image)
```http
POST /api/images/presigned-url
Authorization: Bearer <jwt_token>
Content-Type: application/json

{
  "fileName": "my-image.jpg",
  "contentType": "image/jpeg"
}
```

**Response**:
```json
{
  "presignedUrl": "https://bucket.s3.region.amazonaws.com/listings/uuid.jpg?X-Amz-Algorithm=...",
  "objectKey": "listings/uuid.jpg",
  "publicUrl": "https://bucket.s3.region.amazonaws.com/listings/uuid.jpg",
  "expiresInMinutes": 15
}
```

#### 2. Generate Presigned URLs (Batch - up to 10)
```http
POST /api/images/presigned-url/batch
Authorization: Bearer <jwt_token>
Content-Type: application/json

[
  {"fileName": "image1.jpg", "contentType": "image/jpeg"},
  {"fileName": "image2.png", "contentType": "image/png"}
]
```

#### 3. Delete Image
```http
DELETE /api/images?imageUrl=https://bucket.s3.region.amazonaws.com/listings/uuid.jpg
Authorization: Bearer <jwt_token>
```

### Frontend Integration Flow

```javascript
// Step 1: Get presigned URL from backend
const response = await fetch('/api/images/presigned-url', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    fileName: file.name,
    contentType: file.type
  })
});

const { presignedUrl, publicUrl } = await response.json();

// Step 2: Upload directly to S3 using presigned URL
await fetch(presignedUrl, {
  method: 'PUT',
  body: file,
  headers: {
    'Content-Type': file.type
  }
});

// Step 3: Use publicUrl when creating listing
const listing = {
  title: "My Item",
  images: JSON.stringify([publicUrl]), // Store as JSON string
  // ... other fields
};
```

### Supported Image Types
- `image/jpeg` or `image/jpg`
- `image/png`
- `image/webp`
- `image/gif`

### Security Features
- ✅ **Authentication Required**: All image endpoints require JWT token
- ✅ **Content Type Validation**: Only valid image types allowed
- ✅ **Presigned URL Expiry**: URLs expire after 15 minutes (configurable)
- ✅ **Unique Object Keys**: UUID-based keys prevent collisions
- ✅ **Direct Upload**: Images upload directly to S3 (no backend bandwidth)

### Utility Functions

Use `ImageUtil` class for parsing image JSON strings:

```java
// Parse JSON string to List
List<String> urls = ImageUtil.parseImageUrls(listing.getImages());

// Convert List to JSON string
String json = ImageUtil.toJsonString(urls);

// Add image URL
String updated = ImageUtil.addImageUrl(existingJson, newImageUrl);

// Get first image
String firstImage = ImageUtil.getFirstImageUrl(listing.getImages());
```

### Implementation Details

#### Entity Level (`Listing.java`)
```java
@Column(name = "images", columnDefinition = "JSON")
private String images; // JSON string for image URLs
```

#### DTO Level (`ListingDTO.java`)
```java
private String images; // JSON string array of URLs
```

#### Database Schema
```sql
images JSON NULL
```

### Current Workflow

1. **Client/Frontend** provides image URLs when creating/updating a listing
2. **Backend** stores the JSON string directly in the database
3. **No validation** of image URLs (could be any URL)
4. **No file upload** - images must be hosted elsewhere
5. **No image processing** - no resizing, compression, or optimization

### Example Request

```json
{
  "sellerId": "seller-123",
  "title": "MacBook Pro",
  "description": "Great laptop",
  "price": 1299.99,
  "categoryId": "cat-123",
  "condition": "LIKE_NEW",
  "images": "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]",
  "status": "ACTIVE"
}
```

### Current Limitations

1. ❌ **No File Upload**: Users must host images externally
2. ❌ **No Validation**: Any URL can be stored (no verification)
3. ❌ **No Image Processing**: No resizing, compression, or optimization
4. ❌ **No Storage Management**: No cleanup of unused images
5. ❌ **No CDN**: Images served from external sources (could be slow)
6. ❌ **Security Risk**: No validation that URLs point to actual images

## 🔄 How It Works Now

### Creating a Listing with Images

```bash
POST /api/listings
{
  "images": "[\"https://images.unsplash.com/photo-123.jpg\"]"
}
```

The backend:
1. Accepts the JSON string as-is
2. Stores it directly in the database
3. Returns the listing with images

### Retrieving Images

```bash
GET /api/listings/{id}
```

Response includes:
```json
{
  "id": "listing-123",
  "title": "MacBook Pro",
  "images": "[\"https://images.unsplash.com/photo-123.jpg\"]",
  ...
}
```

Frontend must:
1. Parse the JSON string
2. Extract the array of URLs
3. Display images from those URLs

## 🚀 Production-Ready Solution (S3 Upload)

For production, you would want:

### Option 1: S3 with Presigned URLs (Recommended)

**Flow**:
1. Frontend requests presigned URL from backend
2. Frontend uploads directly to S3 using presigned URL
3. Backend stores S3 URL in database

**Benefits**:
- Direct upload to S3 (no backend bandwidth)
- Scalable
- Secure (presigned URLs expire)
- CDN-ready

### Option 2: Backend Upload to S3

**Flow**:
1. Frontend uploads to backend endpoint
2. Backend processes and uploads to S3
3. Backend stores S3 URL in database

**Benefits**:
- Image processing on backend
- Validation before storage
- More control

### Option 3: Local Storage (Not Recommended for Production)

**Flow**:
1. Frontend uploads to backend
2. Backend saves to filesystem
3. Backend serves via static endpoint

**Limitations**:
- Not scalable
- EC2 storage limits
- No CDN

## 📋 Current Status

✅ **Works for Demo**: Current implementation is sufficient for demos with placeholder images (Unsplash URLs)

⚠️ **Not Production-Ready**: For real users uploading photos, you need:
- File upload endpoint
- S3 integration
- Image validation
- Image processing (optional)
- Presigned URL generation

## 💡 Recommendation

For your current demo/production deployment:

1. **Short-term**: Keep current approach with Unsplash placeholder URLs
2. **Production**: Implement S3 presigned URLs when you need real image uploads

The current implementation is **functional** for demos but would need enhancement for production image uploads.

