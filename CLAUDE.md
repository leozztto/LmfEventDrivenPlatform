# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Nota: este projeto usa português (pt-BR) nas mensagens de commit e na documentação. Responda e escreva documentação em pt-BR.

## Estrutura do repositório

Monorepo de microsserviços Spring Boot em `services/` mais bibliotecas compartilhadas em `shared/`. Existe um **`pom.xml` agregador na raiz** (`com.lmf:lmf-event-driven-platform`, packaging `pom`) que lista os dez módulos; ele serve para build local completo e para o CI construir um módulo por vez pelo reator (`./mvnw -pl <módulo> -am`). O agregador **não** é o `<parent>` dos módulos: cada serviço continua sendo um projeto Maven autocontido, com seu próprio Maven Wrapper (`mvnw`/`mvnw.cmd`) e herdando de `spring-boot-starter-parent`. Hoje todos os serviços fixam Spring Boot `3.5.14` e têm como alvo o Java 17, mas cada `pom.xml` declara isso por conta própria — não há herança de configuração via o agregador.

Bibliotecas compartilhadas (`groupId` `com.lmf`, versão `0.0.1-SNAPSHOT`):

- **`shared/contracts`** (`platform-contracts`) — contratos de evento (envelope + payloads da saga) compartilhados entre serviços.
- **`shared/libraries/platform-messaging`** (`platform-messaging`) — Outbox/Inbox/DLT comuns (relay agendado, consumidor idempotente). Depende de `platform-contracts`.

**OrderService**, **PaymentService**, **InventoryService** e **NotificationService** consomem essas libs hoje.

Serviços implementados: **OrderService**, **InventoryService**, **PaymentService**, **NotificationService**. `AuditService`, `AuthService`, `FraudService` e `GatewayService` são esqueletos gerados (apenas a classe `*Application` + um teste de carga de contexto) — trate-os como ainda não construídos, e o CI deles só compila (`skip-tests: true`).

O **NotificationService** é um consumidor puro (sem REST, sem produção de eventos): assina `order.created`, `payment.approved`, `payment.failed` e `inventory.reservation.failed` com `groupId=notification-service-group` (um segundo leitor desses tópicos, prova de fan-out da coreografia), guarda o contato do pedido em `notification_recipients` a partir do `order.created` e registra o histórico em `notifications`. O envio é um adapter fake (`ConsoleNotificationSender`, canal `LOG`); e-mail/SMS ficam para depois. Entrega é best-effort: falha vira registro `FAILED`, não retentativa de saga. Usa o Inbox do `platform-messaging` (não o Outbox).

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

Para construir um módulo isolado a partir da raiz (compila antes só as libs de que ele precisa): `./mvnw -pl services/OrderService -am verify`.

### CI (GitHub Actions)

CI **por módulo**: há um workflow fino por módulo em `.github/workflows/` (`order-service.yml`, `auth-service.yml`, `platform-contracts.yml`, …), disparado só pelos paths daquele módulo (os serviços de saga incluem também `shared/**`). Todos chamam o workflow reutilizável `_build-module.yml`, que roda `./mvnw -B -ntp -pl <módulo> -am verify` num runner `ubuntu-latest` (Docker disponível para os testes de integração). Ao adicionar um módulo novo, criar o `<nome>.yml` correspondente e adicioná-lo ao `<modules>` do `pom.xml` raiz.

### Infraestrutura local

`docker compose -f infrastructure/docker/docker-compose.yml up -d` sobe Postgres (`localhost:5432`, usuário `postgres` / senha `root`), Zookeeper e Kafka (`localhost:9092`). O `init-databases.sql` do compose cria um banco por serviço (`orderservice`, `paymentservice`, `inventoryservice`, `notificationservice`). Os serviços rodam com `spring.jpa.hibernate.ddl-auto: validate` e dependem das migrações **Flyway** em `src/main/resources/db/migration` (que também varre `db/migration/platform/*` vindo no jar do `platform-messaging`).

Portas HTTP dos serviços: OrderService `8080` (padrão do Spring, não configurada), PaymentService `8082`, InventoryService `8083`, NotificationService `8084`. Actuator + métricas Prometheus ficam expostos em `/actuator/prometheus`.

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
      ^                                   |                                        |
      |          (inventory.reservation.failed / payment.approved / payment.failed)
      +-----------------------------------+----------------------------------------+
                         (OrderService fecha a saga; NotificationService só notifica)
```

Tópicos: `order.created`, `inventory.reserved`, `inventory.reservation.failed`, `payment.approved`, `payment.failed` (mais a DLT `<topic>.dlt` de cada um). Os group ids dos consumidores seguem `<service>-service-group`; um mesmo tópico pode ter vários group ids (ex.: `payment.approved` é lido por `order-`, `inventory-` e `notification-service-group`).

**Transactional Outbox (produtores).** Um use case persiste sua alteração de domínio e uma `OutboxEventEntity` com status `PENDING` no *mesmo* método `@Transactional` (ver `CreateOrderUseCase`). Um poller `@Scheduled` (`OutboxProcessor` / `OutboxEventProcessor`) busca `findTop100ByOutboxStatusOrderByCreatedAtAsc(PENDING)`, publica no Kafka e marca como `PUBLISHED`. Em caso de falha marca `FAILED` e então `markAsPendingRetry()` (volta para `PENDING`) ou, esgotadas as tentativas, transiciona para `DLT` e emite um `DltEvent` no tópico `*.dlt`. Novos eventos de saída devem passar por essa tabela — nunca publique no Kafka diretamente de um use case.

**Inbox / consumo idempotente (consumidores).** Os listeners Kafka estendem `AbstractInboxConsumer` e chamam `process(event, aggregateId, processor)`, que deduplica por `event.eventId()` via `InboxEventService.isDuplicate`, registra o evento, executa o handler e grava `processed`/`failed`. Os payloads de evento de domínio implementam `EventMessage` (`eventId()`, `eventType()`).

**Idempotência HTTP (OrderService).** `POST /api/v1/orders` exige o header `Idempotency-Key`; o `CreateOrderUseCase` o armazena em `IdempotencyEntity` (constraint de unicidade) e retorna o pedido já criado em uma repetição, tratando também `DataIntegrityViolationException` como fallback de concorrência.

### Observabilidade

As requisições carregam um correlation id via `CorrelationIdFilter`; os padrões de log incluem `traceId`/`spanId`/`correlationId` (Micrometer tracing com a ponte Brave no OrderService).
