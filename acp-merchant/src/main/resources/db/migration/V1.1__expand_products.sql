-- Expand Products table to match OpenAI Product Feed Spec
ALTER TABLE merchant.products 
    ADD COLUMN sale_price_amount NUMERIC(19, 4),
    ADD COLUMN sale_price_effective_date VARCHAR(100),
    ADD COLUMN gtin VARCHAR(100),
    ADD COLUMN mpn VARCHAR(100),
    ADD COLUMN merchant_name VARCHAR(255),
    ADD COLUMN merchant_url TEXT,
    ADD COLUMN shipping_weight VARCHAR(100),
    ADD COLUMN return_policy_days INTEGER,
    ADD COLUMN reviews_average_rating NUMERIC(3, 2),
    ADD COLUMN reviews_count INTEGER DEFAULT 0;

-- Create Product Images table for additional images
CREATE TABLE merchant.product_images (
    id              BIGSERIAL PRIMARY KEY,
    product_id      VARCHAR(255) NOT NULL REFERENCES merchant.products(id) ON DELETE CASCADE,
    image_url       TEXT NOT NULL,
    image_type      VARCHAR(50) NOT NULL, -- main, additional, thumbnail
    display_order   INTEGER NOT NULL DEFAULT 0,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_product_images_product_id ON merchant.product_images(product_id);
