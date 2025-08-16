-- Database Schema Initialization Script for StockMeds Centurion Core
-- This script creates tables only if they don't already exist
-- Compatible with PostgreSQL

-- Create accounts table if it doesn't exist
CREATE TABLE IF NOT EXISTS accounts (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_id INTEGER,
    address TEXT,
    gst_number VARCHAR(255) UNIQUE,
    drug_license_number VARCHAR(255) UNIQUE,
    account_status VARCHAR(50) CHECK (account_status IN ('INCOMPLETE', 'ACTIVE', 'INACTIVE', 'BLOCKED')),
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table if it doesn't exist
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    role VARCHAR(50) NOT NULL CHECK (role IN ('OWNER', 'MANAGER', 'EMPLOYEE')) DEFAULT 'OWNER',
    is_verified BOOLEAN DEFAULT FALSE,
    user_status VARCHAR(50) CHECK (user_status IN ('ACTIVE', 'INACTIVE', 'BLOCKED')) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Create products table if it doesn't exist
CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    brand VARCHAR(255),
    manufacturer VARCHAR(255),
    price DECIMAL(10,2),
    stock_quantity INTEGER,
    unit_of_measure VARCHAR(100),
    variant_name VARCHAR(255),
    strength VARCHAR(100),
    packaging VARCHAR(255),
    salts VARCHAR(500),
    indications TEXT,
    key_ingredients TEXT,
    expiry_date DATE,
    batch_number VARCHAR(100),
    hsn_code VARCHAR(50),
    gst_percentage DECIMAL(5,2),
    prescription_required BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add foreign key constraint for accounts.owner_id if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_accounts_owner'
        AND table_name = 'accounts'
    ) THEN
        ALTER TABLE accounts ADD CONSTRAINT fk_accounts_owner
        FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Create indexes for better performance if they don't exist
CREATE INDEX IF NOT EXISTS idx_users_phone_number ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_account_id ON users(account_id);
CREATE INDEX IF NOT EXISTS idx_accounts_gst_number ON accounts(gst_number);
CREATE INDEX IF NOT EXISTS idx_accounts_drug_license ON accounts(drug_license_number);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_brand ON products(brand);
CREATE INDEX IF NOT EXISTS idx_products_manufacturer ON products(manufacturer);
CREATE INDEX IF NOT EXISTS idx_products_search_vector ON products USING gin(search_vector);
CREATE INDEX IF NOT EXISTS idx_product_categories_parent ON product_categories(parent_category_id);

-- Create triggers for automatic timestamp updates if they don't exist
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for each table
DO $$
BEGIN
    -- Trigger for users table
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.triggers
        WHERE trigger_name = 'update_users_updated_at'
    ) THEN
        CREATE TRIGGER update_users_updated_at
        BEFORE UPDATE ON users
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;

    -- Trigger for accounts table
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.triggers
        WHERE trigger_name = 'update_accounts_updated_at'
    ) THEN
        CREATE TRIGGER update_accounts_updated_at
        BEFORE UPDATE ON accounts
        FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;

    -- Trigger for products table
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.triggers
        WHERE trigger_name = 'update_products_updated_at'
    ) THEN
        CREATE TRIGGER update_products_updated_at
        BEFORE UPDATE ON products
        FOR EACH ROW EXECUTE FUNCTION update_products_updated_at_column();
    END IF;
END $$;


COMMIT;
