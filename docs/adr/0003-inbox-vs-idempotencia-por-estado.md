# 0003 — Inbox Pattern nos consumidores da coreografia, exceto no fechamento da saga em OrderService

## Status

Aceito

## Contexto

Kafka garante entrega *at-least-once*: um consumidor pode receber a mesma mensagem mais de uma vez
(reprocessamento após rebalance, retentativa do produtor, etc.), e o handler de cada evento precisa
ser seguro contra isso. A plataforma tem hoje dois mecanismos para essa idempotência, aplicados de
forma diferente conforme o consumidor:

1. **Inbox Pattern** (`AbstractInboxConsumer` + `InboxService`, em `platform-messaging`): antes de
   processar, o consumidor tenta registrar `event.eventId()` numa tabela `inbox_events` (constraint
   única em `event_id`); se já existe (`isAlreadyProcessed`) ou se o `INSERT` colide
   (`DuplicateEventException`, típico de uma corrida entre duas instâncias do mesmo consumer group),
   o evento é ignorado sem reprocessar. É o mecanismo usado por `OrderCreatedConsumer` do
   `InventoryService`, `FraudApprovedConsumer` do `InventoryService`, todos os consumidores do
   `NotificationService` e `OrderCreatedConsumer` do `FraudService`.

2. **Idempotência por estado do agregado**: o `OrderSagaConsumer` do `OrderService` (que fecha a
   saga consumindo `payment.approved`, `payment.failed`, `inventory.reservation.failed` e
   `fraud.rejected`) não usa Inbox. Cada handler chama `UpdateOrderStatusUseCase.applyTransition`,
   que só aplica a transição se o pedido ainda está `PENDING_PAYMENT` — uma reentrega do mesmo
   evento encontra o pedido já em outro status e é ignorada silenciosamente (log + `return`).

Essa diferença não é acidental, mas também não estava documentada — o comentário original em
`UpdateOrderStatusUseCase` já registrava a intenção ("dispensa uma tabela de inbox nesta fase") sem
justificar a decisão num lugar central.

## Decisão

Manter os dois mecanismos, cada um onde já se aplica:

- Consumidores que **disparam uma ação com efeito colateral não idempotente por natureza** (reservar
  estoque, avaliar regras de fraude e gravar um `FraudCheck` por decisão, registrar uma notificação)
  usam Inbox — sem ele, reprocessar `order.created` duas vezes reservaria estoque duas vezes ou
  geraria dois registros de auditoria de fraude para o mesmo pedido.
- O `OrderSagaConsumer` do `OrderService` **não** precisa de Inbox porque toda transição que ele
  aplica já é, por construção, idempotente pelo próprio estado do agregado: `Order.approvePayment()`,
  `Order.rejectPayment()`, `Order.cancel()` e `Order.rejectForFraud()` só têm efeito a partir de
  `PENDING_PAYMENT`, e a saga garante que o pedido só recebe **um** desfecho terminal. Reprocessar o
  mesmo `payment.approved` ou receber `payment.approved` depois de `fraud.rejected` (fora de ordem)
  encontra o pedido fora de `PENDING_PAYMENT` e é ignorado — o mesmo resultado que o Inbox daria, sem
  precisar de uma tabela extra nem de uma transação adicional de registro.

Regra prática para novos consumidores: se o efeito do handler não é idempotente por si (grava um
registro novo, incrementa algo, dispara uma chamada externa), use Inbox. Se o efeito é só uma
transição de estado que uma guarda de domínio já torna idempotente, a idempotência por estado é
suficiente e mais simples.

## Consequências

**Positivas:**

- Cada consumidor usa o mecanismo mínimo necessário — não se paga o custo de uma tabela de Inbox
  (write extra por evento, mais uma linha por mensagem recebida) onde uma guarda de domínio já
  resolve o problema.
- A guarda de estado em `Order` já existia por outro motivo (impedir transições inválidas) — reaproveitá-la
  para idempotência não introduz nenhum código novo no `OrderService`.

**Negativas:**

- A inconsistência de padrão entre serviços exige essa documentação para não ser lida como uma
  omissão: alguém adicionando um novo consumidor à saga precisa saber qual dos dois mecanismos
  escolher, em vez de copiar cegamente o `OrderSagaConsumer` (que é a exceção, não a regra) ou
  assumir que Inbox é sempre necessário.
- Se o `OrderSagaConsumer` um dia precisar de um efeito colateral que não seja só a transição de
  status (por exemplo, uma chamada externa antes de mudar o estado), a idempotência por estado deixa
  de ser suficiente e o consumidor precisará migrar para Inbox — o comentário original já registrava
  isso como possível "Fase 2".
