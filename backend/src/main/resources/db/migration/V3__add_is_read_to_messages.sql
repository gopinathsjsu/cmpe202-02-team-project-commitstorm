-- Add is_read column to messages table
ALTER TABLE messages 
ADD COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE;

-- Set existing messages as read (optional, or leave as unread)
-- UPDATE messages SET is_read = FALSE;

