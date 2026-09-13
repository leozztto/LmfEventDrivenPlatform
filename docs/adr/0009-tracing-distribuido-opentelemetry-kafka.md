# 0009 — Tracing distribuído com OpenTelemetry, propagado via headers Kafka

## Status

Aceito

## Contexto

Os 8 serviços já traziam `micrometer-tracing-bridge-brave` no `pom.xml` e populavam `traceId`/`spanId`
no MDC/logs, mas **nenhum exporter estava configurado** — os spans nunca saíam da JVM. O
`spring.kafka.template.observation-enabled` e o `spring.kafka.listener.observation-enabled` já
estavam `true` em todos os `application.yaml`, ou seja, o Spring Kafka já sabia injetar/extrair
automaticamente o contexto de trace nos headers de `ProducerRecord`/`ConsumerRecord` — mas só quando
existe um span **corrente** no momento do `send()`.

O gap real estava no **Transactional Outbox** (ADR 0002): o span da requisição HTTP de origem termina
junto com a transação de negócio; o `OutboxRelay` publica no Kafka minutos/segundos depois, numa
thread de `@Scheduled` sem nenhum span ativo. Isso é exatamente a lacuna que o
`docs/adr/0006-audit-event-sink.md` já registrava como "trabalho futuro" — o `AuditService` lê
`MDC.get("traceId")` em `RecordAuditEventService` desde sempre, mas o valor capturado era um `traceId`
desconectado por hop, não um trace único ponta-a-ponta.

Três decisões precisavam ser tomadas:

1. **Backend de visualização de traces** a subir na infra local.
2. **Como emitir os spans**: manter Micrometer Tracing (padrão já usado no projeto) trocando só a
   ponte, ou adotar o OpenTelemetry Java Agent (instrumentação automática via javaagent).
3. **Como fechar o gap do Outbox** sem inflar o contrato de eventos (`platform-contracts`) com um
   campo de trace.

## Decisão

- **Jaeger all-in-one** (`jaegertracing/all-in-one:1.60`) como backend, novo serviço no
  `infrastructure/docker/docker-compose.yml`: recebe spans via OTLP HTTP (porta `4318`) e expõe a UI
  de busca de traces na `16686`. Escolhido por receber OTLP nativamente e não exigir peças adicionais
  (Grafana Tempo exigiria subir também o Grafana e configurar datasource).
- **`micrometer-tracing-bridge-otel` no lugar de `micrometer-tracing-bridge-brave`**, mais
  `opentelemetry-exporter-otlp`, em todos os 8 `pom.xml`, configurado via a propriedade nativa do
  Spring Boot 3.5 `management.otlp.tracing.endpoint` (sem bean manual). Mantém a API do Micrometer
  Tracing já usada no projeto (mesmo MDC, mesmos padrões de log com `traceId`/`spanId`) — só troca a
  ponte, zero mudança de código de aplicação nos oito serviços. Descartado o javaagent: fugiria do
  padrão Micrometer já estabelecido e complicaria o `Dockerfile` genérico (precisaria baixar e anexar
  um `.jar` de agente por módulo).
- **Escopo nos 8 serviços**, incluindo `AuthService`/`GatewayService` (REST-only, sem Kafka), para um
  trace ponta-a-ponta desde a borda até o último consumidor da saga.
- **Fechamento do gap do Outbox sem tocar `platform-contracts`**: o contexto de trace viaja só como
  header Kafka (W3C `traceparent`), nunca no payload de negócio. Nova coluna `trace_parent` em
  `outbox_events` (migration `V0_003`, em `platform-messaging`): o `OutboxWriter` grava, no mesmo
  método transacional de sempre, o `traceparent` do span corrente (via
  `io.micrometer.tracing.propagation.Propagator`) quando existe um. O `OutboxRelay`, ao publicar,
  extrai esse valor e abre um span filho (`propagator.extract(...).name("outbox-relay").start()`),
  tornando-o corrente só durante a chamada de `publish` — a instrumentação Observation do
  `KafkaTemplate`, já ligada, injeta esse span no header do `ProducerRecord` sem nenhum código manual
  de header em `MessagePublisher`. Do lado do consumo, nada muda: `listener.observation-enabled: true`
  já extrai o header automaticamente antes de invocar o `@KafkaListener`.

## Consequências

**Positivas:**

- Reaproveita 100% da instrumentação Observation do Spring Kafka que já estava ligada nos oito
  serviços — nenhum código manual de leitura/escrita de header de trace.
- `AuditService`/`RecordAuditEventService` passam a gravar um `traceId` coerente ponta-a-ponta sem
  nenhuma mudança de código — a lacuna do ADR 0006 fica fechada de graça.
- `platform-contracts`/`EventMessage` continua sem nenhum campo de infraestrutura — trace e payload de
  negócio permanecem desacoplados.
- Troca de ponte (Brave → OTel) é transparente para o código de aplicação: MDC, padrões de log e
  `CorrelationIdFilter` continuam funcionando exatamente como antes.

**Negativas:**

- Novo container (`jaeger`) na infraestrutura local — mais uma dependência para `docker compose up`
  e para quem roda os serviços fora do Docker (precisa subir o Jaeger à parte para ver traces, embora
  os serviços funcionem normalmente sem ele, só sem exportar).
- `trace_parent` fica gravado em texto claro em `outbox_events` — aceitável, não é dado sensível, só
  identifica o trace.
- Sampling em `1.0` (100%) é adequado para o ambiente local/didático da plataforma, mas precisaria ser
  revisto (probabilístico, ex. 0.1) antes de qualquer cenário de maior volume.
- `CorrelationIdFilter` continua sendo um id de negócio independente do `traceId` do OTel — não foi
  unificado com o trace, por ser uma mudança de escopo maior que a pedida aqui.

**Trabalho futuro:** propagar também o `traceparent` nas chamadas HTTP síncronas entre
GatewayService → serviços downstream (hoje cobertas pela instrumentação padrão do Spring, mas não
verificado explicitamente); considerar exportar métricas via OTLP também (hoje só Prometheus).
