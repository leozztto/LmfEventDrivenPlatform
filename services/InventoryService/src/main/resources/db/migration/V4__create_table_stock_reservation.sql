CREATE TABLE stock_reservations
(
    id UUID PRIMARY KEY,

    order_id UUID NOT NULL,

    product_id UUID NOT NULL,

    quantity INTEGER NOT NULL,

    reservation_status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uk_stock_reservation_order_product UNIQUE (order_id, product_id)
);

CREATE INDEX idx_stock_reservations_order_id
    ON stock_reservations(order_id);

CREATE INDEX idx_stock_reservations_status
    ON stock_reservations(reservation_status);
