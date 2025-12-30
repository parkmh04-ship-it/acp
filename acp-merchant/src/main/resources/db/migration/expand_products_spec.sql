-- Expand Products table to match OpenAI Product Feed Spec
ALTER TABLE merchant.products 
    ADD COLUMN IF NOT EXISTS sale_price_amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS sale_price_effective_date VARCHAR(100),
    ADD COLUMN IF NOT EXISTS gtin VARCHAR(100),
    ADD COLUMN IF NOT EXISTS mpn VARCHAR(100),
    ADD COLUMN IF NOT EXISTS merchant_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS merchant_url TEXT,
    ADD COLUMN IF NOT EXISTS shipping_weight VARCHAR(100),
    ADD COLUMN IF NOT EXISTS return_policy_days INTEGER,
    ADD COLUMN IF NOT EXISTS reviews_average_rating NUMERIC(3, 2),
    ADD COLUMN IF NOT EXISTS reviews_count INTEGER DEFAULT 0;

-- Create Product Images table for additional images
CREATE TABLE IF NOT EXISTS merchant.product_images (
    id              BIGSERIAL PRIMARY KEY,
    product_id      VARCHAR(255) NOT NULL, -- FK Removed: REFERENCES merchant.products(id) ON DELETE CASCADE
    image_url       TEXT NOT NULL,
    image_type      VARCHAR(50) NOT NULL, -- main, additional, thumbnail
    display_order   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON merchant.product_images(product_id);