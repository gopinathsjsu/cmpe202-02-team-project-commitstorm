#!/bin/bash

# Test S3 Image Upload for iPhone 13 Listing
# This script:
# 1. Logs in to get JWT token
# 2. Generates a presigned URL for image upload
# 3. Uploads a test image to S3
# 4. Updates the iPhone 13 listing with the image URL

BASE_URL="http://localhost:8080"
LISTING_ID="3dd5f4e7-9a72-11f0-abe2-0eb151b291e1"  # iPhone 13 Pro

echo "📸 Testing S3 Image Upload for iPhone 13 Listing"
echo "================================================"
echo ""

# Step 1: Login to get JWT token
echo "1️⃣  Logging in..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sofia@gmail.com",
    "password": "123456"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "❌ Login failed. Response: $LOGIN_RESPONSE"
  echo ""
  echo "Trying with admin user..."
  LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
      "email": "admin@campusmarket.com",
      "password": "admin123"
    }')
  TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)
fi

if [ -z "$TOKEN" ]; then
  echo "❌ Login failed. Please check your credentials."
  exit 1
fi

echo "✅ Login successful!"
echo ""

# Step 2: Generate presigned URL
echo "2️⃣  Generating presigned URL..."
PRESIGNED_RESPONSE=$(curl -s -X POST "$BASE_URL/api/images/presigned-url" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "fileName": "iphone13.jpg",
    "contentType": "image/jpeg"
  }')

echo "Presigned URL Response: $PRESIGNED_RESPONSE"
echo ""

PRESIGNED_URL=$(echo $PRESIGNED_RESPONSE | grep -o '"presignedUrl":"[^"]*' | cut -d'"' -f4)
PUBLIC_URL=$(echo $PRESIGNED_RESPONSE | grep -o '"publicUrl":"[^"]*' | cut -d'"' -f4)

if [ -z "$PRESIGNED_URL" ]; then
  echo "❌ Failed to generate presigned URL. Response: $PRESIGNED_RESPONSE"
  exit 1
fi

echo "✅ Presigned URL generated!"
echo "Public URL: $PUBLIC_URL"
echo ""

# Step 3: Use a test image file
echo "3️⃣  Preparing image for upload..."
TEST_IMAGE="/tmp/test_iphone.jpg"

# Check if user provided an image file as argument
if [ -n "$1" ] && [ -f "$1" ]; then
  echo "Using provided image: $1"
  cp "$1" "$TEST_IMAGE"
else
  # Create a simple test image using ImageMagick if available, or use a minimal JPEG
  if command -v convert >/dev/null 2>&1; then
    convert -size 100x100 xc:blue -quality 90 "$TEST_IMAGE"
    echo "Created test image using ImageMagick"
  elif command -v ffmpeg >/dev/null 2>&1; then
    ffmpeg -f lavfi -i color=c=blue:s=100x100 -frames:v 1 -q:v 2 "$TEST_IMAGE" -y 2>/dev/null
    echo "Created test image using ffmpeg"
  else
    # Fallback: create minimal valid JPEG using printf
    printf '\xFF\xD8\xFF\xE0\x00\x10JFIF\x00\x01\x01\x01\x00H\x00H\x00\x00\xFF\xDB\x00C\x00\x08\x06\x06\x07\x06\x05\x08\x07\x07\x07\t\x08\n\x0B\x14\r\x0E\x0F\x11\x11\x14\x19\x16\x1A\x1D\x1A\x19\x16\x1C\x1C\x1E"#$%%&'"'"'()*456789:CDEFGHIJSTUVWXYZcdefghijstuvwxyz\x83\x84\x85\x86\x87\x88\x89\x8A\x92\x93\x94\x95\x96\x97\x98\x99\x9A\xA2\xA3\xA4\xA5\xA6\xA7\xA8\xA9\xAA\xB2\xB3\xB4\xB5\xB6\xB7\xB8\xB9\xBA\xC2\xC3\xC4\xC5\xC6\xC7\xC8\xC9\xCA\xD2\xD3\xD4\xD5\xD6\xD7\xD8\xD9\xDA\xE1\xE2\xE3\xE4\xE5\xE6\xE7\xE8\xE9\xEA\xF1\xF2\xF3\xF4\xF5\xF6\xF7\xF8\xF9\xFA\xFF\xC0\x00\x0B\x08\x00\x01\x00\x01\x01\x01\x11\x00\xFF\xC4\x00\x14\x00\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x08\xFF\xC4\x00\x14\x10\x01\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\xFF\xDA\x00\x08\x01\x01\x00\x00?\x00\xD2\xCF \xFF\xD9' > "$TEST_IMAGE"
    echo "Created minimal test image"
  fi
fi

if [ ! -f "$TEST_IMAGE" ]; then
  echo "❌ Failed to create test image"
  exit 1
fi

# Step 4: Upload image to S3 using presigned URL
echo "4️⃣  Uploading image to S3..."
UPLOAD_RESPONSE=$(curl -s -X PUT "$PRESIGNED_URL" \
  -H "Content-Type: image/jpeg" \
  --data-binary @"$TEST_IMAGE" \
  -w "\nHTTP_CODE:%{http_code}")

HTTP_CODE=$(echo $UPLOAD_RESPONSE | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)

if [ "$HTTP_CODE" != "200" ] && [ "$HTTP_CODE" != "204" ]; then
  echo "❌ Upload failed. HTTP Code: $HTTP_CODE"
  echo "Response: $UPLOAD_RESPONSE"
  exit 1
fi

echo "✅ Image uploaded to S3!"
echo "Public URL: $PUBLIC_URL"
echo ""

# Step 5: Get current listing data, then update with image URL
echo "5️⃣  Getting current listing data..."
CURRENT_LISTING=$(curl -s -X GET "$BASE_URL/api/listings/$LISTING_ID" \
  -H "Authorization: Bearer $TOKEN")

# Extract current fields from the listing
TITLE=$(echo $CURRENT_LISTING | grep -o '"title":"[^"]*' | cut -d'"' -f4)
DESCRIPTION=$(echo $CURRENT_LISTING | grep -o '"description":"[^"]*' | cut -d'"' -f4)
PRICE=$(echo $CURRENT_LISTING | grep -o '"price":[0-9.]*' | cut -d: -f2)
CATEGORY_ID=$(echo $CURRENT_LISTING | grep -o '"categoryId":"[^"]*' | cut -d'"' -f4)
CONDITION=$(echo $CURRENT_LISTING | grep -o '"condition":"[^"]*' | cut -d'"' -f4)
STATUS=$(echo $CURRENT_LISTING | grep -o '"status":"[^"]*' | cut -d'"' -f4)
SELLER_ID=$(echo $CURRENT_LISTING | grep -o '"sellerId":"[^"]*' | cut -d'"' -f4)

echo "Updating iPhone 13 listing with image URL..."
UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/api/listings/$LISTING_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"sellerId\": \"$SELLER_ID\",
    \"title\": \"$TITLE\",
    \"description\": \"$DESCRIPTION\",
    \"price\": $PRICE,
    \"categoryId\": \"$CATEGORY_ID\",
    \"condition\": \"$CONDITION\",
    \"images\": \"[\\\"$PUBLIC_URL\\\"]\",
    \"status\": \"$STATUS\"
  }")

echo "Update Response: $UPDATE_RESPONSE"
echo ""

# Step 6: Verify the listing was updated
echo "6️⃣  Verifying listing update..."
LISTING_RESPONSE=$(curl -s -X GET "$BASE_URL/api/listings/$LISTING_ID" \
  -H "Authorization: Bearer $TOKEN")

echo "Listing Response: $LISTING_RESPONSE"
echo ""

if echo "$LISTING_RESPONSE" | grep -q "$PUBLIC_URL"; then
  echo "✅ SUCCESS! iPhone 13 listing updated with S3 image URL!"
  echo "   Image URL: $PUBLIC_URL"
else
  echo "⚠️  Warning: Image URL not found in listing response"
fi

echo ""
echo "🎉 Test complete!"