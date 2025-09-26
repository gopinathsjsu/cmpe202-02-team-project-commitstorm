-- Add password column to users table
ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';

-- Update existing users with a default password (in production, you'd want to handle this differently)
UPDATE users SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi' WHERE password = '';

-- Make password column NOT NULL after setting default values
ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NOT NULL;
