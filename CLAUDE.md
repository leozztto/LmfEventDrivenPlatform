# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Nota: este projeto usa português (pt-BR) nas mensagens de commit e na documentação. Responda e escreva documentação em pt-BR.

## Estrutura do repositório

Monorepo de microsserviços Spring Boot independentes em `services/`. **Não há `pom.xml` pai/agregador** — cada serviço é um projeto Maven autocontido, com seu próprio `pom.xml` e Maven Wrapper (`mvnw`/`mvnw.cmd`). As versões do Spring Boot e as configurações não são compartilhadas e divergem entre serviços (ex.: OrderService fixa `3.5.0`, InventoryService/PaymentService `3.5.14`). Todos os serviços têm como alvo o Java 17.

Serviços implementados: **OrderService**, **InventoryService**, **PaymentService**. `AuditService`, `AuthService`, `FraudService`, `GatewayService` e `NotificationService` são esqueletos gerados (apenas a classe `*Application` + um teste de carga de contexto) — trate-os como ainda não construídos.

Os scripts `scripts/*.sh` (`start-local.sh`, `seed-local.sh`, `stop-local.sh`) são placeholders vazios. As mensagens de commit são escritas em português.

## Comandos

Execute tudo de dentro do diretório do serviço, ex.: `cd services/OrderService`.

| Tarefa | Comando |
|--------|---------|
| Build (sem testes) | `./mvnw -q package -DskipTests` |
| Rodar todos os testes | `./mvnw test` |
| Uma classe de teste | `./mvnw test -Dtest=CreateOrderUseCaseTest` |
| Um método de teste | `./mvnw test -Dtest=CreateOrderUseCaseTest#createsOrder` |
| Subir o serviço | `./mvnw spring-boot:run` |

No Windows use `mvnw.cmd`. Os testes de integração (`*IT.java` / `*IntegrationTest.java`, que estendem `AbstractIntegrationTest`) sobem **Testcontainers** (Postgres + Kafka) e exigem um daemon Docker em execução. Os testes unitários são `*Test.java` e não dependem de nada externo.

### Infraestrutura local

`docker compose -f infrastructure/docker/docker-compose.yml up -d` sobe Postgres (`localhost:5432`, usuário `postgres` / senha `root`), Zookeeper e Kafka (`localhost:9092`). O arquivo compose cria apenas o banco `orderservice`; **InventoryService e PaymentService esperam seus próprios bancos** (`inventoryservice`, `paymentservice`), que precisam ser criados manualmente. Os serviços rodam com `spring.jpa.hibernate.ddl-auto: validate` e dependem das migrações **Flyway** em `src/main/resources/db/migration`.

Portas HTTP dos serviços: OrderService `8080` (padrão do Spring, não configurada), PaymentService `8082`, InventoryService `8083`. Actuator + métricas Prometheus ficam expostos em `/actuator/prometheus`.

## Arquitetura

Cada serviço implementado segue camadas de DDD + Clean/Hexagonal. Os nomes de pacote variam um pouco entre serviços, mas os papéis são consistentes:

- `domain` — agregados/entidades (`model`), records de `event` de domínio, tipos de `exception` e **interfaces de repositório** (`domain.repository`). Sem dependências de framework.
- `application` — classes de `usecase` / `service` que contêm a orquestração transacional, além dos records de entrada em `command`.
- `infrastructure` — adaptadores: `persistence` JPA, `producer`/`consumer` Kafka, `config` e a camada REST (`infrastructure.web.controller` no OrderService, `interfaces.rest` no InventoryService).

### Padrão de adaptador de persistência

Uma interface `XRepository` de domínio é implementada por `infrastructure.persistence.repository.XRepositoryImpl`, que delega a um Spring Data `SpringDataXRepository` e converte entre a `*Entity` JPA e o modelo de domínio por meio de um mapper (MapStruct no InventoryService — os caminhos do annotation processor estão configurados no `pom.xml`; manual/MapStruct nos demais). Mantenha essa estrutura de três partes ao adicionar repositórios.

### Fluxo de eventos e padrões de mensageria

A comunicação entre serviços é assíncrona via Kafka. Cadeia atual:

```
OrderService --(order.created)--> InventoryService --(inventory.reserved)--> PaymentService
```

Tópicos: `order.created`, `order.created.dlt`, `inventory.reserved`, `inventory.reservation.dlt`. Os group ids dos consumidores seguem `<service>-service-group`.

**Transactional Outbox (produtores).** Um use case persiste sua alteração de domínio e uma `OutboxEventEntity` com status `PENDING` no *mesmo* método `@Transactional` (ver `CreateOrderUseCase`). Um poller `@Scheduled` (`OutboxProcessor` / `OutboxEventProcessor`) busca `findTop100ByOutboxStatusOrderByCreatedAtAsc(PENDING)`, publica no Kafka e marca como `PUBLISHED`. Em caso de falha marca `FAILED` e então `markAsPendingRetry()` (volta para `PENDING`) ou, esgotadas as tentativas, transiciona para `DLT` e emite um `DltEvent` no tópico `*.dlt`. Novos eventos de saída devem passar por essa tabela — nunca publique no Kafka diretamente de um use case.

**Inbox / consumo idempotente (consumidores).** Os listeners Kafka estendem `AbstractInboxConsumer` e chamam `process(event, aggregateId, processor)`, que deduplica por `event.eventId()` via `InboxEventService.isDuplicate`, registra o evento, executa o handler e grava `processed`/`failed`. Os payloads de evento de domínio implementam `EventMessage` (`eventId()`, `eventType()`).

**Idempotência HTTP (OrderService).** `POST /api/v1/orders` exige o header `Idempotency-Key`; o `CreateOrderUseCase` o armazena em `IdempotencyEntity` (constraint de unicidade) e retorna o pedido já criado em uma repetição, tratando também `DataIntegrityViolationException` como fallback de concorrência.

### Observabilidade

As requisições carregam um correlation id via `CorrelationIdFilter`; os padrões de log incluem `traceId`/`spanId`/`correlationId` (Micrometer tracing com a ponte Brave no OrderService).
