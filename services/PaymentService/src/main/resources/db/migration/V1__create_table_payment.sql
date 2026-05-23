CREATE TABLE payment
(
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    installments INTEGER,
    status VARCHAR(50) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    transaction_id VARCHAR(255),
    gateway_status VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    paid_at TIMESTAMP WITH TIME ZONE,
    failed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_payment_order_id UNIQUE(order_id)
);

CREATE INDEX idx_payment_status
ON payment(status);

CREATE INDEX idx_payment_created_at
ON payment(created_at);

CREATE INDEX idx_payment_status_created_at
ON payment(status, created_at);

CREATE INDEX idx_payment_customer_id
ON payment(customer_id);