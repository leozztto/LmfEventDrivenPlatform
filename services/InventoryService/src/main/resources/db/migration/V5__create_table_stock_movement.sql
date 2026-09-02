CREATE TABLE stock_movements
(
    id UUID PRIMARY KEY,

    product_id UUID NOT NULL,

    movement_type VARCHAR(10) NOT NULL,

    reason VARCHAR(30) NOT NULL,

    quantity INTEGER NOT NULL,

    available_after INTEGER NOT NULL,

    reserved_after INTEGER NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_stock_movements_product_id
    ON stock_movements(product_id);

CREATE INDEX idx_stock_movements_created_at
    ON stock_movements(created_at);
