-- Trilha de auditoria append-only: uma linha por evento consumido de qualquer tópico da saga.
-- Nunca é alterada depois de gravada (o repositório do serviço só expõe save/leitura).
CREATE TABLE audit_events
(
    id              UUID PRIMARY KEY,
    topic           VARCHAR(100) NOT NULL,
    event_id        VARCHAR(100) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    aggregate_id    UUID,
    correlation_id  VARCHAR(100),
    trace_id        VARCHAR(100),
    payload         TEXT NOT NULL,
    received_at     TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_audit_events_aggregate_id ON audit_events(aggregate_id);
CREATE INDEX idx_audit_events_correlation_id ON audit_events(correlation_id);
CREATE INDEX idx_audit_events_topic ON audit_events(topic);
