-- Guarda o traceparent (W3C) vigente no momento em que a linha do outbox foi gravada. O OutboxRelay
-- usa esse valor para retomar o trace da requisição de origem como span pai, minutos depois, quando
-- publica no Kafka -- sem isso o span do relay ficaria orfao (thread do @Scheduled, sem contexto).
ALTER TABLE outbox_events ADD COLUMN IF NOT EXISTS trace_parent VARCHAR(200);
