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

---

## Fraud Service

---

## Auth Service

---

## Audit Service

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

# Author
Developed by Leandro Menegazzo Franceschetto.
