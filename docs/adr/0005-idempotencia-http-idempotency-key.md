# 0005 — Idempotência HTTP via `Idempotency-Key`, separada da idempotência de consumidor Kafka

## Status

Aceito

## Contexto

`POST /api/v1/orders` cria um pedido e, na mesma transação, grava o `OutboxEvent` de
`order.created`. Um cliente HTTP pode repetir essa chamada por motivos que nada têm a ver com Kafka
— timeout de rede, retry automático do lado do cliente, duplo clique no checkout — e cada repetição,
sem proteção, criaria um pedido novo (e um `order.created` novo) para a mesma intenção de compra.

Esse problema é diferente do que o Inbox Pattern (ADR 0003) resolve: o Inbox deduplica **mensagens
Kafka já publicadas** que um consumidor pode receber mais de uma vez. Aqui a duplicação nasce **antes**
de qualquer evento existir — na própria requisição HTTP que dispara a criação do pedido. Um `eventId`
gerado depois da criação não ajuda, porque o problema é impedir que dois pedidos distintos (com dois
`eventId` diferentes) sejam criados para a mesma intenção do cliente.

## Decisão

`CreateOrderUseCase` exige um header `Idempotency-Key`, escolhido pelo cliente, e o trata como a
chave de deduplicação da própria intenção de compra — não do evento. Antes de criar o pedido, o use
case consulta `IdempotencyStore.findOrderIdByKey`; se a chave já existe, devolve o pedido já criado
por essa chave em vez de criar outro (idempotência de leitura, sem repetir efeito colateral). Se não
existe, `idempotencyStore.reserve(idempotencyKey, orderId)` grava a chave **antes** de persistir o
pedido, numa tabela com `UNIQUE(idempotency_key)`: numa corrida entre duas requisições concorrentes
com a mesma chave, a segunda viola essa constraint e a transação inteira é desfeita — sem pedido nem
outbox órfãos — virando um `409 CONFLICT` no `GlobalExceptionHandler`
(`DataIntegrityViolationException`); o cliente reprocessa a chamada e cai no caminho rápido
(`findOrderIdByKey` já encontra o pedido).

Os dois mecanismos coexistem porque resolvem problemas em pontos diferentes do fluxo: a
`Idempotency-Key` protege a fronteira HTTP (evita criar dois pedidos), e o Inbox Pattern protege a
fronteira de consumo Kafka (evita processar duas vezes um evento já publicado). Um não substitui o
outro — mesmo com a `Idempotency-Key` garantindo um único `order.created`, qualquer consumidor
daquele evento (`InventoryService`, `FraudService`, `NotificationService`) ainda pode recebê-lo mais
de uma vez pela semântica *at-least-once* do Kafka, e precisa da própria proteção.

## Consequências

**Positivas:**

- Repetições de rede no lado do cliente (o caso mais comum de duplicação em APIs HTTP) não geram
  pedidos duplicados nem eventos de saga duplicados a partir da origem.
- A responsabilidade de gerar a chave fica com o cliente (que sabe identificar uma única "tentativa
  de compra"), não com o servidor tentando inferir duplicidade por conteúdo — uma estratégia mais
  simples e mais confiável que deduplicar por igualdade de payload.
- O fallback em `DataIntegrityViolationException` cobre a corrida de duas requisições concorrentes
  com a mesma chave sem precisar de lock explícito.

**Negativas:**

- É responsabilidade do cliente da API gerar e reutilizar a `Idempotency-Key` corretamente entre
  tentativas — se o cliente gerar uma chave nova a cada retry (uso incorreto do mecanismo), a
  proteção não tem efeito.
- A tabela `idempotency_keys` cresce indefinidamente, sem expiração — mesma dívida técnica já aceita
  para `outbox_events` (ADR 0002); aceitável no volume atual, mas precisará de uma política de
  retenção se o serviço crescer.
