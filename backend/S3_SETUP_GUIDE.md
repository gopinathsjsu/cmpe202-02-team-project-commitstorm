# S3 Setup Guide for Image Uploads

## 📋 Prerequisites

- AWS Account
- AWS CLI installed and configured
- IAM user with S3 permissions

## 🚀 Step-by-Step Setup

### 1. Create S3 Bucket

```bash
aws s3 mb s3://campus-marketplace-images --region us-west-2
```

### 2. Configure Bucket for Public Read Access

Create a bucket policy (`bucket-policy.json`):

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

Apply policy:
```bash
aws s3api put-bucket-policy --bucket campus-marketplace-images --policy file://bucket-policy.json
```

### 3. Configure CORS for Browser Uploads

Create CORS configuration (`cors-config.json`):

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["PUT", "POST", "HEAD"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }
]
```

Apply CORS:
```bash
aws s3api put-bucket-cors --bucket campus-marketplace-images --cors-configuration file://cors-config.json
```

### 4. Create IAM User for Application

```bash
# Create user
aws iam create-user --user-name campus-marketplace-s3-user

# Create policy
cat > s3-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::campus-marketplace-images/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:ListBucket"
      ],
      "Resource": "arn:aws:s3:::campus-marketplace-images"
    }
  ]
}
EOF

aws iam put-user-policy \
  --user-name campus-marketplace-s3-user \
  --policy-name S3ImageUploadPolicy \
  --policy-document file://s3-policy.json

# Create access keys
aws iam create-access-key --user-name campus-marketplace-s3-user
```

Save the Access Key ID and Secret Access Key for your `.env` file.

### 5. Configure Application

Add to your `.env` file:

```bash
AWS_ACCESS_KEY_ID=AKIA...
AWS_SECRET_ACCESS_KEY=...
AWS_REGION=us-west-2
AWS_S3_BUCKET_NAME=campus-marketplace-images
AWS_S3_PRESIGNED_URL_EXPIRY_MINUTES=15
```

### 6. Test Upload

```bash
# Get presigned URL
curl -X POST http://localhost:8080/api/images/presigned-url \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fileName": "test.jpg",
    "contentType": "image/jpeg"
  }'

# Upload to presigned URL
curl -X PUT "PRESIGNED_URL_FROM_RESPONSE" \
  --upload-file test.jpg \
  -H "Content-Type: image/jpeg"
```

## 🔒 Security Best Practices

1. **Use IAM Roles on EC2** (Recommended):
   - Attach IAM role to EC2 instance
   - Remove access keys from `.env`
   - More secure than access keys

2. **Bucket Policy**:
   - Only allow public read access
   - Write access via presigned URLs only

3. **Presigned URL Expiry**:
   - Default: 15 minutes
   - Adjust based on your needs

4. **Content Type Validation**:
   - Only image types allowed
   - Prevents malicious file uploads

## 📊 Cost Optimization

- **S3 Standard**: For frequently accessed images
- **S3 Intelligent-Tiering**: Automatic cost optimization
- **CloudFront CDN**: Optional, for faster image delivery globally

## 🧪 Testing Without S3

If S3 is not configured, the application will:
- Return error when trying to generate presigned URLs
- Still work with external image URLs (current approach)
- Allow demo with Unsplash placeholder images

## 📝 Troubleshooting

### "S3 bucket name not configured"
- Check `AWS_S3_BUCKET_NAME` in `.env`
- Ensure bucket exists in specified region

### "Access Denied" when uploading
- Verify IAM user has `s3:PutObject` permission
- Check bucket policy allows uploads
- Verify CORS configuration

### "Invalid image type"
- Only `image/jpeg`, `image/png`, `image/webp`, `image/gif` allowed
- Check `contentType` in request

### Presigned URL expires
- Default expiry: 15 minutes
- Generate new URL if expired
- Increase `AWS_S3_PRESIGNED_URL_EXPIRY_MINUTES` if needed

