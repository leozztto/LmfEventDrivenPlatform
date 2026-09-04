# 0002 — Transactional Outbox como único caminho de publicação de eventos

## Status

Aceito

## Contexto

Todo serviço que produz eventos de saga (`OrderService`, `InventoryService`, `PaymentService` e,
mais recentemente, `FraudService`) precisa, no mesmo caso de uso, persistir uma mudança de domínio
**e** publicar um evento sobre ela — por exemplo, `CreateOrderUseCase` grava o pedido e precisa
publicar `order.created`.

Publicar diretamente no Kafka a partir do use case (`kafkaTemplate.send(...)` logo após o
`repository.save(...)`) cria uma janela de inconsistência: se o processo cair, a transação de banco
falhar depois do envio, ou o commit no Kafka falhar depois do commit no banco, o estado do domínio e
o evento publicado divergem — um pedido pode existir sem que `order.created` jamais chegue ao
`InventoryService`, ou o evento pode ser publicado e a transação de banco ser desfeita em seguida.
Esse é o problema clássico de "dual write" em sistemas orientados a eventos.

## Decisão

Nenhum use case publica no Kafka diretamente. Em vez disso, a mudança de domínio e um registro
`OutboxEvent` (status `PENDING`) são gravados no **mesmo** método `@Transactional` — se um falha, o
outro também é desfeito, porque é a mesma transação de banco. Um poller `@Scheduled`
(`OutboxRelay`, em `platform-messaging`) busca até 100 eventos `PENDING` por vez
(`findTop100ByStatusOrderByCreatedAtAsc`, com `SELECT ... FOR UPDATE SKIP LOCKED` via
`lockPending`, o que permite múltiplas instâncias do serviço rodarem o relay em paralelo sem
publicar o mesmo evento duas vezes), publica cada um no tópico resolvido por um
`OutboxTopicRouter` (bean que cada serviço produtor implementa, mapeando `eventType -> tópico`) e
marca como `PUBLISHED`. Em falha de publicação, o evento marca `FAILED` e volta para `PENDING`
(retentativa no próximo ciclo do `@Scheduled`) até esgotar `MAX_RETRIES = 3`, quando transiciona
para `DLT` e um `DltEvent` é publicado no tópico de DLT do outbox daquele serviço
(`platform.outbox.dlt-topic`, ex.: `payment.outbox.dlt`).

O bean `OutboxRelay` só é criado se o serviço fornecer um `OutboxTopicRouter` — um serviço
puramente consumidor (como o `NotificationService`) não precisa desse bean.

## Consequências

**Positivas:**

- Consistência entre o estado do domínio e os eventos publicados é garantida por uma transação de
  banco comum, sem precisar de um coordenador de transação distribuída (2PC) entre Postgres e Kafka.
- A publicação real fica desacoplada da requisição/transação de negócio — uma falha temporária do
  broker não derruba o caso de uso, só atrasa a entrega até o próximo ciclo do relay.
- O padrão é uniforme entre os quatro serviços produtores, então adicionar um evento de saída novo
  em qualquer serviço segue sempre o mesmo molde: `outboxWriter.write(aggregateId, aggregateType,
  eventType, payload)` dentro da transação, mais uma entrada no `OutboxTopicRouter`.

**Negativas:**

- Publicação não é instantânea — há uma latência de até `platform.outbox.poll-interval-ms` (5s por
  padrão) entre o commit da transação e a mensagem chegar ao tópico, o que se soma à latência total
  da saga a cada passo.
- A tabela `outbox_events` cresce indefinidamente com eventos `PUBLISHED`; não há hoje uma rotina de
  limpeza/arquivamento — aceito como dívida técnica por enquanto, dado o volume baixo de um ambiente
  de desenvolvimento/demonstração.
