# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Nota: este projeto usa português (pt-BR) nas mensagens de commit e na documentação. Responda e escreva documentação em pt-BR.

## Estrutura do repositório

Monorepo de microsserviços Spring Boot em `services/` mais bibliotecas compartilhadas em `shared/`. Existe um **`pom.xml` agregador na raiz** (`com.lmf:lmf-event-driven-platform`, packaging `pom`) que lista os dez módulos; ele serve para build local completo e para o CI construir um módulo por vez pelo reator (`./mvnw -pl <módulo> -am`). O agregador **não** é o `<parent>` dos módulos: cada serviço continua sendo um projeto Maven autocontido, com seu próprio Maven Wrapper (`mvnw`/`mvnw.cmd`) e herdando de `spring-boot-starter-parent`. Hoje todos os serviços fixam Spring Boot `3.5.14` e têm como alvo o Java 17, mas cada `pom.xml` declara isso por conta própria — não há herança de configuração via o agregador.

Bibliotecas compartilhadas (`groupId` `com.lmf`, versão `0.0.1-SNAPSHOT`):

- **`shared/contracts`** (`platform-contracts`) — contratos de evento (envelope + payloads da saga) compartilhados entre serviços.
- **`shared/libraries/platform-messaging`** (`platform-messaging`) — Outbox/Inbox/DLT comuns (relay agendado, consumidor idempotente). Depende de `platform-contracts`.

**OrderService**, **PaymentService**, **InventoryService**, **NotificationService**, **FraudService** e **AuditService** consomem essas libs hoje.

Serviços implementados: **OrderService**, **InventoryService**, **PaymentService**, **NotificationService**, **FraudService**, **AuditService**, **AuthService**, **GatewayService**. **AuthService** e **GatewayService** são REST-only e ficam **fora do tema event-driven**: não usam Kafka, `platform-messaging` nem `platform-contracts` (ver `docs/adr/0007-*` e `0008-*`).

O **NotificationService** é um consumidor puro (sem REST, sem produção de eventos): assina `order.created`, `payment.approved`, `payment.failed` e `inventory.reservation.failed` com `groupId=notification-service-group` (um segundo leitor desses tópicos, prova de fan-out da coreografia), guarda o contato do pedido em `notification_recipients` a partir do `order.created` e registra o histórico em `notifications`. O envio é um adapter fake (`ConsoleNotificationSender`, canal `LOG`); e-mail/SMS ficam para depois. Entrega é best-effort: falha vira registro `FAILED`, não retentativa de saga. Usa o Inbox do `platform-messaging` (não o Outbox).

O **FraudService** é o novo elo entre `OrderService` e `InventoryService` (ver `docs/adr/0001-fraud-coreografia-vs-orquestracao.md`): consome `order.created`, aplica as regras de fraude da v1 (limite de valor configurável via `fraud.rules.max-order-amount` + lista de bloqueio por `customerId`/e-mail), grava o histórico em `fraud_checks` e publica `fraud.approved` ou `fraud.rejected` via Outbox — o `InventoryService` passou a reagir a `fraud.approved` em vez de `order.created`, e o `OrderService` cancela o pedido (`FRAUD_REJECTED`) ao consumir `fraud.rejected`. Expõe `POST /api/v1/blocklist` e `DELETE /api/v1/blocklist/{id}` para administração simples da lista de bloqueio.

O **AuthService** (porta `8087`, banco `authservice`) faz cadastro/login de usuários com papéis e emite um **access token JWT RS256** via `NimbusJwtEncoder`. Endpoints REST próprios (`POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET /api/v1/auth/me`) — sem fluxos OAuth2. O par RSA é gerado no startup (ou vem de `auth.jwt.public-key`/`private-key`) e a chave pública é publicada em `GET /oauth2/jwks` para o Gateway validar tokens localmente. `Role` é enum (`ROLE_USER`, `ROLE_ADMIN`) em `user_roles` via `@ElementCollection`; senha com BCrypt. Migração `V2__seed_admin_user.sql` cria `admin` / `admin123` (`ROLE_ADMIN`). Ver `docs/adr/0007-authservice-jwt-stateless-rsa-jwks.md`.

O **GatewayService** (porta `8088`, sem banco) é o ponto único de entrada: **Spring Cloud Gateway WebMVC** (servlet), rotas declarativas em `application.yaml` sob `spring.cloud.gateway.server.webmvc.routes` para os serviços com REST + `auth` + `oauth2/jwks`. Valida o JWT na borda como **OAuth2 Resource Server** (`spring.security.oauth2.resourceserver.jwt.jwk-set-uri` → `/oauth2/jwks` do Auth), mapeia o claim `roles` para authorities e exige `ROLE_ADMIN` em `/api/v1/blocklist/**` e `/api/v1/audit-events/**`. **Rate limiting** com Resilience4j `RateLimiter` num `OncePerRequestFilter` global (chave = `sub` do JWT ou IP; 429 ao exceder `gateway.ratelimit.*`) — o gateway WebMVC não tem filtro de rate limit Resilience4j nativo. **Agregação de OpenAPI** via `springdoc.swagger-ui.urls` + rotas `/aggregate/{service}/v3/api-docs` (só OrderService e InventoryService). Ver `docs/adr/0008-gateway-borda-jwt-webmvc-ratelimit-openapi.md`.

O **AuditService** é um event sink append-only: um terceiro leitor (fan-out) de todos os sete tópicos da saga (`order.created`, `fraud.approved`, `fraud.rejected`, `inventory.reserved`, `inventory.reservation.failed`, `payment.approved`, `payment.failed`), um consumidor por tópico via `AbstractInboxConsumer`. Grava o envelope de cada evento (tipado por `platform-contracts`, resserializado como payload) em `audit_events`, junto com `traceId`/`correlationId` quando presentes no MDC — hoje `correlationId` fica quase sempre nulo para eventos vindos do Kafka, pois nenhum produtor da plataforma o propaga como header (ver `docs/adr/0006-audit-event-sink.md`). Expõe `GET /api/v1/audit-events?aggregateId=` ou `?correlationId=` para consulta. Como só grava e nunca publica, não usa Outbox (usa só o Inbox do `platform-messaging`, igual ao NotificationService).

Os scripts em `scripts/` estão implementados: `start-local.sh` / `stop-local.sh` sobem e derrubam a infra, `seed-local.sh` cadastra produtos no InventoryService, `e2e-saga.sh` / `e2e-saga-declined.sh` rodam a saga ponta a ponta com os serviços via `java -jar`, `e2e-docker.sh` roda a saga inteira **nos containers** (`docker compose up -d --build` + os três caminhos — aprovado, recusado por pagamento e rejeitado por fraude — + fan-out do NotificationService + trilha de auditoria do AuditService + checagem de DLT; `E2E_KEEP=1` mantém o stack de pé no fim, `E2E_NO_BUILD=1` pula o build), e `e2e-auth-gateway.sh` valida a borda nos containers (`register`/`login` via Gateway + roteamento downstream com/sem Bearer + gate de `ROLE_ADMIN` + JWKS). As mensagens de commit são escritas em português.

## Comandos

Execute tudo de dentro do diretório do serviço, ex.: `cd services/OrderService`.

| Tarefa | Comando |
|--------|---------|
| Build (sem testes) | `./mvnw -q package -DskipTests` |
| Rodar todos os testes | `./mvnw test` |
| Uma classe de teste | `./mvnw test -Dtest=CreateOrderUseCaseTest` |
| Um método de teste | `./mvnw test -Dtest=CreateOrderUseCaseTest#createsOrder` |
| Subir o serviço | `./mvnw spring-boot:run` |

Para subir a plataforma inteira em containers (a partir da raiz): `docker compose -f infrastructure/docker/docker-compose.yml up -d --build`. Rebuild de um serviço só: `docker compose -f infrastructure/docker/docker-compose.yml up -d --build order-service`.

No Windows use `mvnw.cmd`. Os testes de integração (`*IT.java` / `*IntegrationTest.java`, que estendem `AbstractIntegrationTest`) sobem **Testcontainers** (Postgres + Kafka) e exigem um daemon Docker em execução. Os testes unitários são `*Test.java` e não dependem de nada externo.

Para construir um módulo isolado a partir da raiz (compila antes só as libs de que ele precisa): `./mvnw -pl services/OrderService -am verify`.

### CI (GitHub Actions)

CI **por módulo**: há um workflow fino por módulo em `.github/workflows/` (`order-service.yml`, `auth-service.yml`, `platform-contracts.yml`, …), disparado só pelos paths daquele módulo (os serviços de saga incluem também `shared/**`). Todos chamam o workflow reutilizável `_build-module.yml`, que roda `./mvnw -B -ntp -pl <módulo> -am verify` num runner `ubuntu-latest` (Docker disponível para os testes de integração). Ao adicionar um módulo novo, criar o `<nome>.yml` correspondente e adicioná-lo ao `<modules>` do `pom.xml` raiz.

### Infraestrutura local

`docker compose -f infrastructure/docker/docker-compose.yml up -d --build` sobe a plataforma inteira: Postgres (`localhost:5432`, usuário `postgres` / senha `root`), Zookeeper, Kafka (`localhost:9092`) **e os oito serviços implementados** (OrderService, InventoryService, PaymentService, NotificationService, FraudService, AuditService, AuthService, GatewayService). Os serviços rodam com `spring.jpa.hibernate.ddl-auto: validate` e dependem das migrações **Flyway** em `src/main/resources/db/migration` (que também varre `db/migration/platform/*` vindo no jar do `platform-messaging`).

**Bancos por serviço.** O Postgres usa o volume nomeado `pgdata`. O `init-databases.sql` cria `orderservice`, `paymentservice`, `inventoryservice`, `notificationservice`, `fraudservice`, `auditservice` e `authservice` (o GatewayService não tem banco), mas o hook `/docker-entrypoint-initdb.d` só roda quando o volume é criado do zero. Por isso existe o serviço `db-init` (roda o mesmo script, idempotente via `\gexec`, a cada `up`), do qual os serviços com banco dependem (`condition: service_completed_successfully`). Se ainda assim faltar algum banco (volume corrompido/antigo), rode `docker compose -f infrastructure/docker/docker-compose.yml down -v` e suba de novo.

As imagens dos serviços são construídas pelo `infrastructure/docker/Dockerfile` genérico (build multi-stage: `maven:3.9-eclipse-temurin-17` roda `mvn -pl <MODULE> -am -DskipTests package` pelo reator do agregador; runtime em `eclipse-temurin:17-jre`). O contexto de build é a raiz do repositório e o módulo alvo vem do `build.args.MODULE` de cada serviço no compose. Dentro da rede do compose os serviços falam com o broker pelo listener interno `kafka:29092` e com o banco em `postgres:5432` (via `KAFKA_BOOTSTRAP_SERVERS` / `DB_URL`, que também têm default para execução fora do Docker apontando para `localhost`). O broker sobe com `KAFKA_AUTO_CREATE_TOPICS_ENABLE=false`: os beans `NewTopic` de cada `KafkaTopicsConfig` são a única fonte de criação de tópicos (sempre 3 partições), evitando a corrida em que o broker criaria o tópico com 1 partição antes do bean.

Para rodar um serviço fora do container (pelo IDE ou `./mvnw spring-boot:run`), suba só a infra com `docker compose -f infrastructure/docker/docker-compose.yml up -d postgres kafka` e deixe os defaults `localhost` valerem.

Portas HTTP dos serviços: OrderService `8081` (via `SERVER_PORT` no compose; fora do Docker o default do Spring é `8080`), PaymentService `8082`, InventoryService `8083`, NotificationService `8084`, FraudService `8085`, AuditService `8086`, AuthService `8087`, GatewayService `8088`. Actuator + métricas Prometheus ficam expostos em `/actuator/prometheus`.

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
OrderService --(order.created)--> FraudService --(fraud.approved)--> InventoryService --(inventory.reserved)--> PaymentService
      ^                                 |                                    |                                        |
      |                     (fraud.rejected)   (inventory.reservation.failed / payment.approved / payment.failed)
      +---------------------------------+------------------------------------+----------------------------------------+
                (OrderService fecha a saga; NotificationService só notifica; AuditService só grava a trilha)
```

Tópicos: `order.created`, `fraud.approved`, `fraud.rejected`, `inventory.reserved`, `inventory.reservation.failed`, `payment.approved`, `payment.failed` (mais a DLT `<topic>.dlt` de cada um). Os group ids dos consumidores seguem `<service>-service-group`; um mesmo tópico pode ter vários group ids (ex.: `payment.approved` é lido por `order-`, `inventory-`, `notification-` e `audit-service-group`; o AuditService lê os sete tópicos com `audit-service-group`).

**Transactional Outbox (produtores).** Um use case persiste sua alteração de domínio e uma `OutboxEventEntity` com status `PENDING` no *mesmo* método `@Transactional` (ver `CreateOrderUseCase`). Um poller `@Scheduled` (`OutboxProcessor` / `OutboxEventProcessor`) busca `findTop100ByOutboxStatusOrderByCreatedAtAsc(PENDING)`, publica no Kafka e marca como `PUBLISHED`. Em caso de falha marca `FAILED` e então `markAsPendingRetry()` (volta para `PENDING`) ou, esgotadas as tentativas, transiciona para `DLT` e emite um `DltEvent` no tópico `*.dlt`. Novos eventos de saída devem passar por essa tabela — nunca publique no Kafka diretamente de um use case.

**Inbox / consumo idempotente (consumidores).** Os listeners Kafka estendem `AbstractInboxConsumer` e chamam `process(event, aggregateId, processor)`, que deduplica por `event.eventId()` via `InboxEventService.isDuplicate`, registra o evento, executa o handler e grava `processed`/`failed`. Os payloads de evento de domínio implementam `EventMessage` (`eventId()`, `eventType()`).

**Idempotência HTTP (OrderService).** `POST /api/v1/orders` exige o header `Idempotency-Key`; o `CreateOrderUseCase` o armazena em `IdempotencyEntity` (constraint de unicidade) e retorna o pedido já criado em uma repetição, tratando também `DataIntegrityViolationException` como fallback de concorrência.

### Observabilidade

As requisições carregam um correlation id via `CorrelationIdFilter`; os padrões de log incluem `traceId`/`spanId`/`correlationId` (Micrometer tracing com a ponte Brave no OrderService).
