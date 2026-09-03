CREATE TABLE notifications
(
    id                  UUID PRIMARY KEY,
    order_id            UUID NOT NULL,
    customer_id         UUID,
    notification_type   VARCHAR(50) NOT NULL,
    channel             VARCHAR(20) NOT NULL,
    recipient           VARCHAR(255),
    subject             VARCHAR(255) NOT NULL,
    body                TEXT NOT NULL,
    notification_status VARCHAR(20) NOT NULL,
    failure_reason      TEXT,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at             TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_notifications_order_id ON notifications(order_id);
CREATE INDEX idx_notifications_status ON notifications(notification_status);
