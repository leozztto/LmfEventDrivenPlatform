CREATE TABLE fraud_blocklist
(
    id             UUID PRIMARY KEY,
    customer_id    UUID,
    customer_email VARCHAR(255),
    reason         VARCHAR(300)             NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_fraud_blocklist_has_identifier CHECK (customer_id IS NOT NULL OR customer_email IS NOT NULL)
);

CREATE INDEX idx_fraud_blocklist_customer_id ON fraud_blocklist (customer_id);
CREATE INDEX idx_fraud_blocklist_customer_email ON fraud_blocklist (customer_email);
