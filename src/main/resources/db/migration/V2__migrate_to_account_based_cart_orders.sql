-- Migration to change cart and order functionality from user-based to account-based
-- This migration adds account_id columns and updates existing data

-- Step 1: Add account_id column to users table if not exists
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_id INTEGER;

-- Step 2: Add account_id column to carts table
ALTER TABLE carts ADD COLUMN account_id INTEGER;

-- Step 3: Update carts to link to accounts instead of users
-- This assumes that users have account_id populated
UPDATE carts
SET account_id = (
    SELECT u.account_id
    FROM users u
    WHERE u.id = carts.user_id
)
WHERE carts.user_id IS NOT NULL;

-- Step 4: Drop the old user_id column from carts and add constraints
ALTER TABLE carts DROP COLUMN IF EXISTS user_id;
ALTER TABLE carts ALTER COLUMN account_id SET NOT NULL;
ALTER TABLE carts ADD CONSTRAINT uk_carts_account_id UNIQUE (account_id);
ALTER TABLE carts ADD CONSTRAINT fk_carts_account_id FOREIGN KEY (account_id) REFERENCES accounts(id);

-- Step 5: Add account_id column to orders table
ALTER TABLE orders ADD COLUMN account_id INTEGER;

-- Step 6: Update orders to link to accounts instead of users
-- This assumes that users have account_id populated
UPDATE orders
SET account_id = (
    SELECT u.account_id
    FROM users u
    WHERE u.id = orders.user_id
)
WHERE orders.user_id IS NOT NULL;

-- Step 7: Drop the old user_id column from orders and add constraints
ALTER TABLE orders DROP COLUMN IF EXISTS user_id;
ALTER TABLE orders ALTER COLUMN account_id SET NOT NULL;
ALTER TABLE orders ADD CONSTRAINT fk_orders_account_id FOREIGN KEY (account_id) REFERENCES accounts(id);

-- Step 8: Add foreign key constraint for users.account_id if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_users_account_id'
        AND table_name = 'users'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT fk_users_account_id FOREIGN KEY (account_id) REFERENCES accounts(id);
    END IF;
END $$;

-- Step 9: Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_carts_account_id ON carts(account_id);
CREATE INDEX IF NOT EXISTS idx_orders_account_id ON orders(account_id);
CREATE INDEX IF NOT EXISTS idx_users_account_id ON users(account_id);
