# LmfEventDrivenPlatform

Plataforma distribuída baseada em microsserviços orientados a eventos, projetada para demonstrar arquitetura escalável, resiliente e preparada para ambientes cloud-native.

Este projeto simula um ecossistema de transações financeiras/e-commerce, explorando padrões modernos de engenharia de software como:

- Event-Driven Architecture (EDA)
- Domain-Driven Design (DDD)
- Clean Architecture
- Saga Pattern
- Outbox Pattern
- Idempotency
- Observability
- Cloud-Native Deployment

---

# Arquitetura

A plataforma é composta por múltiplos microsserviços independentes que se comunicam de forma assíncrona através de eventos.

## Services

### Core Business Services

| Service | Responsabilidade |
|---------|------------------|
| AuditService | Auditoria e rastreabilidade de eventos |
| AuthService | Autenticação, autorização e emissão de JWT |
| FraudService | Análise e prevenção antifraude |
| GatewayService | API Gateway e roteamento |
| InventoryService | Controle e reserva de estoque |
| NotificationService | Envio de notificações |
| OrderService | Gestão do ciclo de pedidos |
| PaymentService | Processamento de pagamentos |

# Details of Services

## Order Service
Order management microservice built with Domain-Driven Design (DDD) and Clean Architecture principles using Spring Boot.

This project demonstrates production-grade backend patterns including:
- Transactional Outbox Pattern
- Apache Kafka integration
- Idempotent APIs
- Integration testing with Testcontainers
- Observability with Prometheus metrics
- Global exception handling
- PostgreSQL persistence with Flyway migrations

---

## Payment Service

Payment processing microservice built with Domain-Driven Design (DDD), Event-Driven Architecture and Clean Architecture principles using Spring Boot.

This service is responsible for handling payment transactions asynchronously, ensuring consistency and reliability across distributed services.

### Features

- Payment processing workflow
- Kafka-based asynchronous communication
- Transactional Outbox Pattern
- Idempotent payment processing
- Payment status lifecycle management
- Retry and Dead Letter Topic (DLT) support
- Global exception handling
- PostgreSQL persistence with Flyway migrations
- Observability with Prometheus metrics
- Distributed tracing ready with OpenTelemetry
- Integration testing with Testcontainers

### Payment Lifecycle

```text
PENDING -> PROCESSING -> APPROVED
                           └-> FAILED
```

---

## Notification Service

Microsserviço de notificações construído com Domain-Driven Design (DDD), Event-Driven Architecture e Clean Architecture com Spring Boot.

É um **consumidor puro da coreografia da saga**: não expõe REST e não produz eventos. Atua como um segundo leitor dos tópicos da saga (`groupId=notification-service-group`), provando o fan-out da arquitetura orientada a eventos — os mesmos eventos que fecham a saga no OrderService também disparam uma notificação aqui, de forma independente.

### Features

- Consome `order.created`, `payment.approved`, `payment.failed` e `inventory.reservation.failed`
- Guarda o contato do pedido em `notification_recipients` a partir do `order.created` (nome, e-mail, telefone, `customerId`)
- Registra o histórico de cada notificação em `notifications` (tipo, canal, destinatário, assunto, corpo, status e motivo da falha)
- Envio via adapter fake `ConsoleNotificationSender` (canal `LOG`); `EMAIL`/`SMS` ficam para adapters posteriores
- Entrega **best-effort**: falha de canal vira registro `FAILED` e ausência de destinatário conhecido vira `SKIPPED` — nunca dispara retentativa nem compensação de saga
- Consumo idempotente via **Inbox** da biblioteca comum `com.lmf:platform-messaging` (não usa Outbox, pois não publica eventos)
- Contratos de evento compartilhados em `com.lmf:platform-contracts`
- Persistência PostgreSQL (banco `notificationservice`) com migrações Flyway
- Observabilidade: logs estruturados com `correlationId` (via `CorrelationIdFilter`) + métricas Prometheus em `/actuator/prometheus`
- Porta HTTP `8084` (apenas Actuator)
- Testes unitários, de integração (Testcontainers: Postgres + Kafka) e E2E da coreografia

### Notification Lifecycle

```text
evento de saga consumido
        |
        v
resolve destinatário (notification_recipients)
        |
        +--> destinatário desconhecido --> SKIPPED
        |
        v
ConsoleNotificationSender (canal LOG)
        |
        +--> entregue --> SENT
        +--> falha    --> FAILED (failure_reason)
```

### Event Flow

```text
order.created ─────────────────► grava/atualiza notification_recipients + notifica "pedido criado"
payment.approved ──────────────► notifica "pagamento aprovado"
payment.failed ────────────────► notifica "pagamento recusado"
inventory.reservation.failed ──► notifica "estoque indisponível"
                                        │
                                        └─► cada resultado é persistido em notifications
```

---

## Inventory Service

Inventory management microservice built with Domain-Driven Design (DDD), Event-Driven Architecture and Clean Architecture principles using Spring Boot.

This service is responsible for managing product inventory, stock reservations and inventory consistency across distributed services.

### Features

- Cadastro e consulta de produtos (`POST /api/v1/products`, `GET /api/v1/products`,
  `GET /api/v1/products/{id}`, `PATCH /api/v1/products/stock`)
- Fluxo de reserva de estoque (um evento por pedido)
- Movimentações manuais de estoque com histórico (`stock_movements`)
- Ledger de reservas (`stock_reservations`) com compensação: `payment.failed` libera, `payment.approved` confirma
- Controle de quantidade disponível e reservada
- Comunicação assíncrona via Kafka (contratos compartilhados em `com.lmf:platform-contracts`)
- Outbox/Inbox e DLT via biblioteca comum `com.lmf:platform-messaging`
- Retry com backoff exponencial limitado + Dead Letter Topic
- Tratamento global de exceções (404/409/422/400)
- Persistência PostgreSQL com migrações Flyway
- Observabilidade: logs estruturados + tracing (Micrometer/Brave) propagado pelo Kafka
- OpenAPI/Swagger UI (springdoc) em `/swagger-ui.html`
- Testes de integração com Testcontainers

### Inventory Lifecycle

```text
AVAILABLE
    |
    v
RESERVED
    |
    +--> RELEASED
    |
    +--> CONFIRMED
```

### Event Flow

```text
Order Service
      |
      v
OrderCreatedEvent  (tópico order.created)
      |
      v
Inventory Service
      |
      +--> reserva o estoque de todos os itens (tudo ou nada) e grava as reservas
      |
      +--> InventoryReservedEvent          (tópico inventory.reserved, 1 por pedido)
      |       payload: orderId, customerId, totalAmount, payment, items[]
      |
      +--> InventoryReservationFailedEvent  (tópico inventory.reservation.failed)
      |
      v
Payment Service --> payment.approved | payment.failed
      |
      +--> payment.approved  --> Inventory confirma a reserva; Order -> PAYMENT_APPROVED
      +--> payment.failed    --> Inventory libera a reserva;  Order -> PAYMENT_REJECTED
```

---

## Gateway Service

Ponto único de entrada da plataforma, construído com **Spring Cloud Gateway (WebMVC)** — servlet, para
manter a coerência com o stack MVC/JPA do resto dos serviços. É REST-only e fica fora do tema
event-driven (sem Kafka, sem banco).

### Features

- Roteamento declarativo em `application.yaml` (`spring.cloud.gateway.server.webmvc.routes`) para os
  serviços com REST — `/api/v1/orders/**` → OrderService, `/api/v1/products/**` → InventoryService,
  `/api/v1/blocklist/**` → FraudService, `/api/v1/audit-events/**` → AuditService — mais
  `/api/v1/auth/**` e `/oauth2/jwks` → AuthService. As URIs vêm de variáveis de ambiente
  (`ORDER_SERVICE_URL`, …) com default `localhost`.
- **Validação de JWT na borda** como OAuth2 Resource Server: a chave pública vem do JWKS do AuthService
  (`spring.security.oauth2.resourceserver.jwt.jwk-set-uri`). O claim `roles` vira authorities;
  `/api/v1/blocklist/**` e `/api/v1/audit-events/**` exigem `ROLE_ADMIN`, o restante exige apenas um
  token válido; `/api/v1/auth/**`, `/oauth2/jwks`, `/actuator/**` e o Swagger são públicos.
- **Rate limiting** com Resilience4j `RateLimiter` num `OncePerRequestFilter` global (o gateway WebMVC
  não tem filtro de rate limit Resilience4j nativo). Chave = `sub` do JWT ou IP de origem; resposta
  `429` ao exceder `gateway.ratelimit.{limit-for-period,refresh-period,timeout}`.
- **Agregação de OpenAPI**: `http://localhost:8088/swagger-ui.html` com um seletor por serviço
  (`springdoc.swagger-ui.urls` + rotas `/aggregate/{service}/v3/api-docs`). Hoje cobre OrderService e
  InventoryService (os únicos com `springdoc`).
- 401/403 devolvidos com corpo `ErrorResponse` JSON; observabilidade com `correlationId`/`traceId` +
  métricas Prometheus.
- Porta HTTP `8088`.
- Testes unitários e de integração (roteamento + autorização + rate limit contra um WireMock in-JVM;
  não precisa de Docker).

Ver `docs/adr/0008-gateway-borda-jwt-webmvc-ratelimit-openapi.md`.

---

## Fraud Service

---

## Auth Service

Microsserviço de identidade construído com DDD + Clean Architecture. Faz cadastro/login de usuários
com papéis e emite um **access token JWT RS256**. É REST-only e fica fora do tema event-driven.

### Features

- Endpoints REST próprios (sem fluxos OAuth2): `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
  (devolve `{accessToken, tokenType, expiresIn}`) e `GET /api/v1/auth/me` (perfil do token).
- JWT assinado com **RS256** via `NimbusJwtEncoder`; claims `sub`, `roles`, `email`, `iss`, `iat`,
  `exp`, `jti`; TTL configurável em `auth.jwt.ttl` (default `PT1H`).
- Par RSA **gerado no startup** (ou fornecido via `auth.jwt.public-key`/`private-key`); a chave pública
  é publicada em `GET /oauth2/jwks` (RFC 7517) para o Gateway validar tokens localmente.
- Usuários e papéis em PostgreSQL (banco `authservice`, migrações Flyway); senha com **BCrypt**;
  `Role` é enum (`ROLE_USER`, `ROLE_ADMIN`) numa tabela `user_roles`.
- Migração `V2__seed_admin_user.sql` cria um usuário `admin` / `admin123` com `ROLE_ADMIN` para o
  ambiente local / e2e.
- Observabilidade com `correlationId`/`traceId` + métricas Prometheus em `/actuator/prometheus`.
- Porta HTTP `8087`.
- Testes unitários e de integração (fluxo register → login → `/me` contra Testcontainers Postgres).

Ver `docs/adr/0007-authservice-jwt-stateless-rsa-jwks.md`.

---

## Audit Service

Microsserviço de auditoria construído com Domain-Driven Design (DDD), Event-Driven Architecture e Clean Architecture com Spring Boot.

É um **event sink append-only**: um terceiro leitor de todos os sete tópicos da coreografia da saga (`groupId=audit-service-group`), sem participar do fluxo de negócio — só grava o envelope de cada evento consumido e expõe uma consulta de leitura. Não expõe nenhum endpoint de escrita e não publica eventos.

### Features

- Consome os sete tópicos da saga: `order.created`, `fraud.approved`, `fraud.rejected`, `inventory.reserved`, `inventory.reservation.failed`, `payment.approved`, `payment.failed` — um consumidor por tópico, reaproveitando os contratos tipados de `com.lmf:platform-contracts`
- Grava uma linha append-only por evento em `audit_events` (tópico, `eventId`, `eventType`, `aggregateId` = `orderId`, payload resserializado, `traceId`/`correlationId` quando disponíveis no MDC)
- Consulta de leitura: `GET /api/v1/audit-events?aggregateId=` ou `?correlationId=` (exatamente um dos dois filtros)
- Consumo idempotente via **Inbox** da biblioteca comum `com.lmf:platform-messaging` (não usa Outbox, pois não publica eventos — mesma decisão do NotificationService)
- Retry com backoff exponencial + Dead Letter Topic por tópico consumido
- Persistência PostgreSQL (banco `auditservice`) com migrações Flyway
- Observabilidade: logs estruturados com `correlationId`/`traceId` + métricas Prometheus em `/actuator/prometheus`
- Porta HTTP `8086`
- Testes unitários, de integração (Testcontainers: Postgres + Kafka) e E2E da coreografia (todos os sete tópicos, incluindo dedupe por redelivery)

> `correlationId` fica `null` na maior parte das vezes hoje: nenhum produtor da plataforma o propaga como header Kafka (só existe no MDC durante a requisição HTTP de origem). Ver `docs/adr/0006-audit-event-sink.md`.

### Event Flow

```text
order.created / fraud.approved / fraud.rejected / inventory.reserved /
inventory.reservation.failed / payment.approved / payment.failed
        |
        v
  consumidor do tópico (Inbox dedupe por eventId)
        |
        v
  grava topic + envelope + traceId/correlationId em audit_events
```

---

# Stack Tecnológica

## Backend

- Java 17
- Kotlin
- Spring Boot 3
- Spring Web
- Spring Security
- Spring Data JPA
- Spring Cloud
- Spring Batch
- Hibernate

---

## Mensageria e Event Streaming

- Apache Kafka

Padrões aplicados:

- Transactional Outbox
- Dead Letter Topic (DLT)
- Retry Pattern
- Idempotency

## Banco de Dados

- PostgreSQL
- Redis

## Infraestrutura

- Docker
- Docker Compose
- Kubernetes

## Observabilidade

- Prometheus
- Grafana
- OpenTelemetry

## Testes

- JUnit 5
- Mockito
- Testcontainers

---

# Estrutura do Monorepo

```text
LmfEventDrivenPlatform/
├── pom.xml                  # aggregator Maven (build multi-módulo)
├── mvnw / mvnw.cmd
├── services/
│   ├── AuditService/
│   ├── AuthService/
│   ├── FraudService/
│   ├── GatewayService/
│   ├── InventoryService/
│   ├── NotificationService/
│   ├── OrderService/
│   └── PaymentService/
│
├── shared/
│   └── contracts/           # com.lmf:platform-contracts — contratos de evento da saga
│
├── infrastructure/
│   ├── docker/
│   ├── kubernetes/
│   ├── terraform/
│   └── monitoring/
│
├── scripts/
└── docs/
```

### Build

```bash
# Builda os contratos compartilhados e os serviços na ordem correta:
./mvnw install

# Para buildar um serviço isoladamente, os contratos precisam estar no repositório local antes:
./mvnw -pl shared/contracts install
cd services/OrderService && ./mvnw test
```

### Rodar localmente com Docker

```bash
# Sobe tudo em containers (Postgres + Kafka + os 8 serviços implementados):
docker compose -f infrastructure/docker/docker-compose.yml up -d --build

# Portas: OrderService 8081, PaymentService 8082, InventoryService 8083, NotificationService 8084,
# FraudService 8085, AuditService 8086, AuthService 8087, GatewayService 8088
# Derruba tudo (com volumes):
docker compose -f infrastructure/docker/docker-compose.yml down -v

# E2E da saga rodando nos containers (aprovação + recusa/compensação + rejeição por fraude +
# fan-out + trilha de auditoria + DLT):
./scripts/e2e-docker.sh

# E2E da borda (register/login via Gateway + roteamento com/sem Bearer + gate de ROLE_ADMIN):
./scripts/e2e-auth-gateway.sh
```

As imagens são construídas pelo `infrastructure/docker/Dockerfile` genérico (build multi-stage
via reator Maven, parametrizado por `build.args.MODULE`).

# Author
Developed by Leandro Menegazzo Franceschetto.
