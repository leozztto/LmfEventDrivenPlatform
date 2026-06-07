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

---

## Inventory Service

Inventory management microservice built with Domain-Driven Design (DDD), Event-Driven Architecture and Clean Architecture principles using Spring Boot.

This service is responsible for managing product inventory, stock reservations and inventory consistency across distributed services.

### Features

- Product registration and management
- Inventory reservation workflow
- Manual stock movement operations
- Available and reserved stock control
- Kafka-based asynchronous communication
- Transactional Outbox Pattern
- Inbox Pattern for idempotent event consumption
- Inventory reservation success/failure events
- Retry and Dead Letter Topic (DLT) support
- Global exception handling
- PostgreSQL persistence with Flyway migrations
- Observability with structured logging
- Integration testing with Testcontainers

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
│   ├── events/
│   ├── contracts/
│   ├── schemas/
│   └── libraries/
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

# Author
Developed by Leandro Menegazzo Franceschetto.
