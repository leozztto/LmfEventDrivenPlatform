# 0004 — Compensação assíncrona da reserva de estoque em vez de commit distribuído

## Status

Aceito

## Contexto

Depois que o `InventoryService` reserva estoque para um pedido (`inventory.reserved`), o
`PaymentService` processa o pagamento de forma assíncrona e pode aprová-lo ou recusá-lo. O estoque
reservado precisa refletir esse desfecho: confirmado (debitado de vez) se o pagamento passou,
devolvido ao disponível se falhou. Como `InventoryService` e `PaymentService` são bancos de dados
independentes, não há como envolver os dois numa única transação ACID.

Duas abordagens resolvem esse tipo de inconsistência entre serviços:

1. **Commit distribuído (2PC / XA)**: um coordenador de transação mantém `InventoryService` e
   `PaymentService` bloqueados até que ambos confirmem, garantindo atomicidade "clássica" entre os
   dois bancos.
2. **Compensação assíncrona (padrão Saga)**: cada serviço confirma sua própria transação
   imediatamente, guardando estado suficiente para desfazer (compensar) depois; o desfecho de um
   serviço chega ao outro via evento, que aciona a compensação ou confirmação.

## Decisão

Compensação assíncrona. A reserva de estoque nasce no estado `RESERVED`
(`StockReservation.create`), decrementando `availableQuantity` e incrementando
`reservedQuantity` do produto imediatamente, na mesma transação da reserva. O
`PaymentOutcomeConsumer` do `InventoryService` assina `payment.approved` e `payment.failed`
(mesmo `groupId=inventory-service-group`, já usado pela reserva) e delega a
`ReservationOutcomeService.confirm`/`release`:

- `confirm` (pagamento aprovado): `Product.confirmReservedStock` debita definitivamente o
  `reservedQuantity`; a reserva marca `CONFIRMED`.
- `release` (pagamento recusado): `Product.releaseStock` devolve a quantidade a
  `availableQuantity`; a reserva marca `RELEASED`.

Ambos os métodos são idempotentes por estado: `ReservationOutcomeService.apply` filtra só reservas
ainda `RESERVED` (`StockReservation.isPending()`) antes de agir — uma reentrega do mesmo evento, ou
os dois desfechos chegando fora de ordem, não reaplica nem desfaz o que já foi assentado. Essa é a
"compensação" mais completa da plataforma hoje (mais rica que o simples `cancel()` do
`OrderService` para falha de reserva), porque reverte um efeito de negócio já commitado (estoque
debitado), não só uma mudança de status.

## Consequências

**Positivas:**

- Nenhum serviço fica bloqueado esperando o outro — a reserva é confirmada e commitada
  imediatamente, e o `InventoryService` segue disponível para processar outros pedidos enquanto o
  pagamento do primeiro ainda está em voo.
- Sem dependência de um coordenador de transação distribuída (XA), tecnologia que a stack atual
  (Spring Data JPA simples + Kafka) não usa em nenhum outro lugar.
- O modelo de estados explícito (`RESERVED -> CONFIRMED | RELEASED`) torna o histórico de cada
  reserva auditável — dá para responder "o que aconteceu com o estoque deste pedido" só olhando a
  tabela `stock_reservations`, sem reconstruir a partir dos tópicos Kafka.

**Negativas:**

- Existe uma janela de inconsistência temporária: entre a reserva e a confirmação/liberação, o
  `reservedQuantity` fica "preso" mesmo que o pagamento venha a falhar — outros pedidos veem o
  produto com menos `availableQuantity` do que efetivamente será vendido. Aceitável para o domínio
  (é o comportamento esperado de qualquer e-commerce com reserva de carrinho), mas é uma
  consistência eventual, não imediata.
- Se o evento de desfecho do pagamento nunca chegar (perda de mensagem, DLT sem reprocessamento
  manual), a reserva fica presa em `RESERVED` indefinidamente — não há hoje um job de expiração de
  reservas órfãs; é dívida técnica aceita nesta fase.
