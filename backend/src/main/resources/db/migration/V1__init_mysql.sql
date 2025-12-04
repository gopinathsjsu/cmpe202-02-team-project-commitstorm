-- campusMarket Database --
CREATE DATABASE IF NOT EXISTS campusMarket
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE campusMarket;

-- Users
CREATE TABLE IF NOT EXISTS users (
    id      CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
    name    VARCHAR(255) NOT NULL,
    email   VARCHAR(255) NOT NULL UNIQUE,
    role    ENUM('USER','ADMIN') NOT NULL DEFAULT 'USER',
    status  ENUM('ACTIVE','SUSPENDED','BANNED') NOT NULL DEFAULT 'ACTIVE',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
  id    CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
  name  VARCHAR(255) NOT NULL UNIQUE
);

-- Listings
CREATE TABLE IF NOT EXISTS listings (
  id           CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
  seller_id    CHAR(36) NOT NULL,
  title        VARCHAR(255) NOT NULL,
  description  TEXT,
  price        DECIMAL(10,2) NOT NULL,
  category_id  CHAR(36) NOT NULL,
  `condition`  ENUM('NEW','LIKE_NEW','GOOD','FAIR','POOR') NOT NULL,
  images       JSON NULL,
  status       ENUM('ACTIVE','SOLD','PENDING','DRAFT') NOT NULL DEFAULT 'ACTIVE',
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_listings_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_listings_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
  CONSTRAINT chk_listing_price CHECK (price >= 0)
);

-- Wishlist
CREATE TABLE IF NOT EXISTS wishlist (
  user_id    CHAR(36) NOT NULL,
  listing_id CHAR(36) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, listing_id),
  CONSTRAINT fk_wish_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_wish_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE
);

-- Messages
CREATE TABLE IF NOT EXISTS messages (
  id            CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
  listing_id    CHAR(36) NOT NULL,
  from_user_id  CHAR(36) NOT NULL,
  to_user_id    CHAR(36) NOT NULL,
  content       TEXT NOT NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_msg_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE CASCADE,
  CONSTRAINT fk_msg_from FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_msg_to FOREIGN KEY (to_user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Reports
CREATE TABLE IF NOT EXISTS reports (
  id            CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
  reporter_id   CHAR(36) NOT NULL,
  target_type   ENUM('LISTING','USER') NOT NULL,
  target_id     CHAR(36) NOT NULL,
  reason        TEXT NOT NULL,
  status        ENUM('OPEN','IN_REVIEW','RESOLVED','ACTIONED') NOT NULL DEFAULT 'OPEN',
  moderator_id  CHAR(36) NULL,
  created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_report_reporter  FOREIGN KEY (reporter_id)  REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_report_moderator FOREIGN KEY (moderator_id) REFERENCES users(id)
);

-- Transactions
CREATE TABLE IF NOT EXISTS transactions (
  id           CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
  listing_id   CHAR(36) NOT NULL UNIQUE,
  buyer_id     CHAR(36) NOT NULL,
  final_price  DECIMAL(10,2) NOT NULL,
  status       ENUM('PENDING','COMPLETED','CANCELLED','REFUNDED') NOT NULL DEFAULT 'PENDING',
  created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_tx_listing FOREIGN KEY (listing_id) REFERENCES listings(id) ON DELETE RESTRICT,
  CONSTRAINT fk_tx_buyer FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_tx_price CHECK (final_price >= 0)
);

-- Reviews
CREATE TABLE IF NOT EXISTS reviews (
  id             CHAR(36) NOT NULL PRIMARY KEY DEFAULT (UUID()),
  transaction_id CHAR(36) NOT NULL UNIQUE,
  reviewer_id    CHAR(36) NOT NULL,
  seller_id      CHAR(36) NOT NULL,
  rating         TINYINT NOT NULL,
  comment        TEXT,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rev_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
  CONSTRAINT fk_rev_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_rev_seller FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5)
);