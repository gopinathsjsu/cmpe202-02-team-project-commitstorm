-- Long-term fix for Flyway migration V2 issue
-- This script repairs the Flyway schema history table
-- Run this ONCE to fix the database state

USE campusMarket;

-- Step 1: Check current state
SELECT 
    'Current Flyway History for V2:' AS info,
    version,
    description,
    success,
    installed_on,
    installed_rank
FROM flyway_schema_history 
WHERE version = '2';

-- Step 2: Check if password column exists
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'Password column EXISTS'
        ELSE 'Password column DOES NOT exist'
    END AS column_status,
    COUNT(*) AS column_count
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_SCHEMA = 'campusMarket' 
  AND TABLE_NAME = 'users' 
  AND COLUMN_NAME = 'password';

-- Step 3: Fix the schema history based on actual database state
-- If password column EXISTS, mark migration as successful
UPDATE flyway_schema_history 
SET success = 1 
WHERE version = '2' 
  AND EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'campusMarket' 
      AND TABLE_NAME = 'users' 
      AND COLUMN_NAME = 'password'
  );

-- If password column DOES NOT exist, delete the failed record so migration can re-run
DELETE FROM flyway_schema_history 
WHERE version = '2' 
  AND success = 0
  AND NOT EXISTS (
    SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = 'campusMarket' 
      AND TABLE_NAME = 'users' 
      AND COLUMN_NAME = 'password'
  );

-- Step 4: Verify the fix
SELECT 
    'After fix - Flyway History for V2:' AS info,
    version,
    description,
    success,
    installed_on
FROM flyway_schema_history 
WHERE version = '2';

-- Expected result:
-- If password column exists: success = 1
-- If password column doesn't exist: no record (will be re-created on next startup)

