CREATE TABLE IF NOT EXISTS outbox_events
(
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    outbox_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    error_message VARCHAR(300)
);

CREATE INDEX IF NOT EXISTS idx_outbox_status ON outbox_events(outbox_status);
CREATE INDEX IF NOT EXISTS idx_outbox_created_at ON outbox_events(created_at);
