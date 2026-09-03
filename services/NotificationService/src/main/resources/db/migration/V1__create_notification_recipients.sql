CREATE TABLE notification_recipients
(
    order_id    UUID PRIMARY KEY,
    customer_id UUID,
    name        VARCHAR(255),
    email       VARCHAR(255),
    phone       VARCHAR(50),
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL
);
