-- Create Checkout Sessions table
CREATE TABLE IF NOT EXISTS merchant.checkout_sessions (
    id              VARCHAR(255) PRIMARY KEY,
    status          VARCHAR(50) NOT NULL, -- NOT_READY, READY, COMPLETED, CANCELED
    currency        VARCHAR(3) NOT NULL DEFAULT 'KRW',
    total_amount    NUMERIC(19, 4) NOT NULL DEFAULT 0,
    
    -- Buyer Info
    buyer_email     VARCHAR(255),
    buyer_name      VARCHAR(255),
    
    -- Fulfillment Address
    shipping_address_country VARCHAR(10),
    shipping_address_postal_code VARCHAR(20),
    
    -- Next Action
    next_action_url TEXT,
    
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP WITH TIME ZONE
);

-- Create Checkout Items table
CREATE TABLE IF NOT EXISTS merchant.checkout_items (
    id              BIGSERIAL PRIMARY KEY,
    checkout_session_id VARCHAR(255) NOT NULL, -- FK Removed: REFERENCES merchant.checkout_sessions(id) ON DELETE CASCADE
    product_id      VARCHAR(255) NOT NULL, -- FK Removed: REFERENCES merchant.products(id)
    quantity        INTEGER NOT NULL,
    
    -- Snapshot of price at the time of addition
    unit_price      NUMERIC(19, 4) NOT NULL,
    total_price     NUMERIC(19, 4) NOT NULL,
    
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_checkout_items_session_id ON merchant.checkout_items(checkout_session_id);