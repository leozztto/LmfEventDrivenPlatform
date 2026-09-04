CREATE TABLE fraud_checks
(
    id           UUID PRIMARY KEY,
    order_id     UUID                     NOT NULL,
    customer_id  UUID,
    decision     VARCHAR(20)              NOT NULL,
    reason       VARCHAR(300),
    total_amount NUMERIC(19, 2),
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_fraud_checks_order_id ON fraud_checks (order_id);
