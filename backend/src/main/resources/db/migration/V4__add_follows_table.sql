-- Follows table for following sellers
CREATE TABLE IF NOT EXISTS follows (
  id            CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
  follower_id   CHAR(36) NOT NULL,
  seller_id     CHAR(36) NOT NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY unique_follow (follower_id, seller_id),
  CONSTRAINT fk_follow_follower FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_follow_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_follower (follower_id),
  INDEX idx_seller (seller_id)
);

