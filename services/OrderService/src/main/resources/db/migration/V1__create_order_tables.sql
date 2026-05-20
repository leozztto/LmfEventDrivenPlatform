CREATE TABLE orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50) NOT NULL,
    shipping_street VARCHAR(255) NOT NULL,
    shipping_number VARCHAR(50) NOT NULL,
    shipping_city VARCHAR(100) NOT NULL,
    shipping_zip_code VARCHAR(20) NOT NULL,
    shipping_country VARCHAR(10) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    payment_installments INTEGER NOT NULL,
    paid_amount NUMERIC(19,2),
    order_status VARCHAR(50) NOT NULL,
    total_amount NUMERIC(19,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(19,2) NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    outbox_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message VARCHAR(300)
);

CREATE INDEX idx_orders_customer_id
ON orders(customer_id);

CREATE INDEX idx_orders_status
ON orders(order_status);

CREATE INDEX idx_outbox_status
ON outbox_events(outbox_status);

CREATE INDEX idx_outbox_created_at
ON outbox_events(created_at);