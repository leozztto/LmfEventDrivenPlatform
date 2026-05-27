CREATE TABLE inbox_events (

    id UUID PRIMARY KEY,

    event_id VARCHAR(255) NOT NULL,

    aggregate_id UUID NOT NULL,

    event_type VARCHAR(255) NOT NULL,

    inbox_status VARCHAR(50) NOT NULL,

    received_at TIMESTAMP WITH TIME ZONE NOT NULL,

    processed_at TIMESTAMP WITH TIME ZONE,

    failure_reason TEXT,

    CONSTRAINT uk_inbox_event_id UNIQUE (event_id)
);