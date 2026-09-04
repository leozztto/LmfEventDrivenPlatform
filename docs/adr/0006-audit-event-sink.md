# 0006 — AuditService: sink de auditoria append-only, consumo tipado, sem Outbox

## Status

Aceito

## Contexto

Com FraudService e NotificationService em produção, a saga já tem sete tópicos ativos
(`order.created`, `fraud.approved`, `fraud.rejected`, `inventory.reserved`,
`inventory.reservation.failed`, `payment.approved`, `payment.failed`), cada um consumido por um ou
mais serviços que reagem a ele para tomar uma decisão de negócio. Nenhum lugar único guarda a
sequência completa de eventos de um pedido — reconstruir o que aconteceu exige juntar logs de vários
serviços.

O `AuditService` precisava virar esse lugar único: um terceiro leitor (fan-out) de todos os sete
tópicos, sem influenciar o fluxo de negócio, guardando um registro append-only de cada evento —
embrião de um log de auditoria/event sourcing. Três decisões de design surgiram junto:

1. **Como consumir**: bytes crus do `ConsumerRecord` (parsing manual de JSON, sem depender dos
   contratos tipados) ou os records de `platform-contracts`, como todo outro consumidor da
   plataforma.
2. **Publica algo?**: como o FraudService (Outbox + decisão publicada) ou como o NotificationService
   (só consome, nunca publica).
3. **De onde vem o `aggregateId`**: os sete eventos carregam nomes de campo diferentes
   (`orderId`, e no caso de pagamento também `paymentId`) — preciso de um critério uniforme para a
   consulta por `aggregateId`.

## Decisão

- **Consumo tipado via `platform-contracts`**, um consumidor por tópico, cada um estendendo
  `AbstractInboxConsumer<T>` exatamente como os demais serviços. O "payload cru" salvo em
  `audit_events` é o evento resserializado via Jackson — equivalente ao JSON publicado, já que a
  plataforma nunca emite campos fora do contrato. Isso evita duplicar a lógica de extração de
  `eventId`/`eventType`/`orderId` em um parser de JSON paralelo, e reaproveita 100% da
  infraestrutura de idempotência (`InboxService`) já validada.
- **Sem Outbox**: o `AuditService` só grava, nunca publica. `PlatformMessagingAutoConfiguration`
  só cria o `OutboxRelay` quando há um bean `OutboxTopicRouter` no contexto — como o `AuditService`
  não declara nenhum, o relay simplesmente não sobe, igual ao `NotificationService`.
- **`aggregateId` = `orderId`**: os sete eventos da saga carregam esse campo (confirmado nos records
  de `shared/contracts`), então a extração é idêntica em todos os sete consumidores — sem precisar
  de lógica condicional por tipo de evento.

## Consequências

**Positivas:**

- Nenhum padrão arquitetural novo é introduzido — o `AuditService` reaproveita 100% da
  infraestrutura de Inbox/DLT/contratos já validada pelos outros cinco serviços.
- A consulta por `aggregateId` funciona uniformemente para qualquer um dos sete tópicos, sem mapear
  campo por tipo de evento.
- Adicionar um novo tópico à saga no futuro é só mais um consumidor no mesmo molde — nenhuma mudança
  estrutural no `AuditService`.

**Negativas:**

- **`correlationId` fica `null` para praticamente todo evento hoje.** O `CorrelationIdFilter` de
  `OrderService`/`PaymentService`/`NotificationService` só popula o MDC durante a requisição HTTP de
  origem; nenhum publicador propaga esse valor como header Kafka, então um consumidor puro como o
  `AuditService` não tem como recuperá-lo. A coluna existe e é populada via MDC mesmo assim (para
  funcionar automaticamente no dia em que a propagação por header for adicionada aos publicadores),
  mas por ora a consulta por `correlationId` só é útil dentro de uma mesma requisição
  HTTP → Kafka → consumo síncrono nos testes, não entre serviços. Propagar `correlationId` como
  header Kafka nos publicadores é trabalho futuro, fora do escopo desta mudança.
- O payload gravado é a resserialização do contrato tipado, não os bytes exatos publicados no
  tópico — aceitável enquanto a plataforma nunca emitir campos fora do contrato (o
  `fail-on-unknown-properties: false` do Jackson já assume esse comportamento em todos os
  consumidores), mas um evento malformado publicado por engano ficaria fora da trilha de auditoria
  em vez de ser registrado como está.
