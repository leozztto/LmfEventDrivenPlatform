# 0001 — FraudService: coreografia via Kafka em vez de orquestração central

## Status

Aceito

## Contexto

A saga de pedidos da plataforma sempre foi 100% coreografada via Kafka: cada serviço reage a um
evento publicado por outro e publica o seu próprio, sem nenhum componente central que conheça o
fluxo inteiro. Até aqui a cadeia era `order.created -> inventory.reserved -> payment.approved` (ou
`payment.failed`/`inventory.reservation.failed`), fechada pelo `OrderService` ao consumir os
desfechos.

O `FraudService` precisava entrar como um novo passo de decisão **entre** a criação do pedido e a
reserva de estoque — antes de comprometer estoque ou iniciar cobrança em um pedido potencialmente
fraudulento. Havia duas formas de encaixar essa decisão na saga:

1. **Manter a coreografia**: o `FraudService` consome `order.created` como mais um leitor do tópico
   (do mesmo jeito que `InventoryService` e `NotificationService` já fazem hoje) e publica
   `fraud.approved`/`fraud.rejected`. O `InventoryService` troca seu gatilho de reserva de
   `order.created` para `fraud.approved`, e o `OrderService` ganha um novo listener para
   `fraud.rejected` que cancela o pedido — o mesmo padrão de compensação já usado para
   `inventory.reservation.failed`.
2. **Introduzir uma orquestração central**: um componente novo (no próprio `OrderService` ou um
   orquestrador dedicado) chamaria o `FraudService`, o `InventoryService` e o `PaymentService` de
   forma sequencial e explícita — por comando síncrono ou por uma máquina de estados que dirige cada
   passo.

## Decisão

Manter a coreografia. O `FraudService` se pluga como mais um elo da cadeia de eventos, exatamente
como os demais serviços da saga: consome `order.created` via `AbstractInboxConsumer` (idempotência
por Inbox) e publica sua decisão via `OutboxWriter` (Transactional Outbox) — a mesma infraestrutura
de `platform-messaging` que todo produtor/consumidor da plataforma já usa. Nenhum serviço passa a
conhecer o fluxo da saga inteira; o `InventoryService` só sabe que reage a `fraud.approved`, o
`OrderService` só sabe que `fraud.rejected` cancela o pedido.

## Consequências

**Positivas:**

- Nenhum padrão arquitetural novo é introduzido — o `FraudService` reaproveita 100% da infraestrutura
  de Outbox/Inbox/DLT já validada pelos outros quatro serviços.
- Acoplamento continua baixo: o `InventoryService` não conhece o `FraudService` diretamente, apenas o
  contrato de evento `fraud.approved` (`shared/contracts`).
- Adicionar o próximo passo de decisão na saga (se algum dia existir) segue o mesmo molde, sem
  precisar tocar num orquestrador central.

**Negativas:**

- A saga fica um passo mais longa: um pedido aprovado agora atravessa `order.created ->
  fraud.approved -> inventory.reserved -> payment.approved` em vez de pular direto para a reserva,
  adicionando a latência de mais um ciclo de outbox (poll a cada alguns segundos) mais um round-trip
  de Kafka.
- A depuração distribuída continua sendo o trade-off já aceito pela coreografia: entender o caminho
  completo de um pedido exige olhar logs/tópicos de múltiplos serviços — mitigado pelo
  `correlationId` propagado nos logs, mas sem um único lugar que mostre o fluxo inteiro.
