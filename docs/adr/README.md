# Architecture Decision Records

Registro das decisões arquiteturais da plataforma — o contexto e as alternativas consideradas, não
só o resultado final. Formato: Status / Contexto / Decisão / Consequências.

| ADR | Decisão |
|-----|---------|
| [0001](0001-fraud-coreografia-vs-orquestracao.md) | FraudService: coreografia via Kafka em vez de orquestração central |
| [0002](0002-transactional-outbox.md) | Transactional Outbox como único caminho de publicação de eventos |
| [0003](0003-inbox-vs-idempotencia-por-estado.md) | Inbox Pattern nos consumidores da coreografia, exceto no fechamento da saga em OrderService |
| [0004](0004-compensacao-reserva-estoque.md) | Compensação assíncrona da reserva de estoque em vez de commit distribuído |
| [0005](0005-idempotencia-http-idempotency-key.md) | Idempotência HTTP via `Idempotency-Key`, separada da idempotência de consumidor Kafka |
| [0006](0006-audit-event-sink.md) | AuditService: sink de auditoria append-only, consumo tipado, sem Outbox |
| [0007](0007-authservice-jwt-stateless-rsa-jwks.md) | AuthService: emissão de JWT stateless com assinatura RSA + JWKS, sem Authorization Server |
| [0008](0008-gateway-borda-jwt-webmvc-ratelimit-openapi.md) | GatewayService: validação de JWT na borda, Spring Cloud Gateway WebMVC, rate limit Resilience4j e agregação de OpenAPI |
