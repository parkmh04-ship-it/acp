-- Add fulfillment option fields to checkout_sessions table
ALTER TABLE merchant.checkout_sessions 
ADD COLUMN IF NOT EXISTS selected_fulfillment_option VARCHAR(50);

ALTER TABLE merchant.checkout_sessions 
ADD COLUMN IF NOT EXISTS shipping_cost NUMERIC(19, 2);

-- Add comment
COMMENT ON COLUMN merchant.checkout_sessions.selected_fulfillment_option IS '선택된 배송 옵션 ID (standard, express, same_day)';
COMMENT ON COLUMN merchant.checkout_sessions.shipping_cost IS '계산된 배송비';
