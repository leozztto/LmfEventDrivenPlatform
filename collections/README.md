# Collections de API — LmfEventDrivenPlatform

Collections Postman (schema v2.1) com **todos os endpoints HTTP disponíveis** na plataforma,
uma por módulo. Onde um módulo não expõe endpoint HTTP de negócio, isso está descrito abaixo.

## Arquivos

| Arquivo | Conteúdo |
|---------|----------|
| `LmfEventDrivenPlatform.postman_environment.json` | Environment com as base URLs locais e variáveis compartilhadas (`productId`, `orderId`). |
| `OrderService.postman_collection.json` | API de pedidos (2 endpoints REST) + Actuator + Swagger. |
| `InventoryService.postman_collection.json` | Catálogo de produtos e estoque (4 endpoints REST) + Actuator + Swagger. |
| `PaymentService.postman_collection.json` | Apenas Actuator (sem API REST — serviço dirigido a eventos). |
| `NotificationService.postman_collection.json` | Apenas Actuator (sem API REST — consumidor puro). |

## Como usar

### Postman / Insomnia / Bruno
1. Importe o `*.postman_environment.json` e as `*.postman_collection.json` desejadas.
2. Selecione o environment **LmfEventDrivenPlatform - Local**.
3. Suba a plataforma: `docker compose -f infrastructure/docker/docker-compose.yml up -d --build`.
4. Rode **InventoryService → Criar produto** (grava `productId` no environment) e depois
   **OrderService → Criar pedido** (grava `orderId`). **Buscar pedido por id** acompanha a saga.

### Newman (CLI)
```bash
newman run collections/InventoryService.postman_collection.json \
  -e collections/LmfEventDrivenPlatform.postman_environment.json
```

## Mapa de módulos

| Módulo | Porta (compose) | Endpoints HTTP | Observação |
|--------|-----------------|----------------|------------|
| **OrderService** | 8081 | `POST /api/v1/orders`, `GET /api/v1/orders/{orderId}` + Actuator (`health,info,metrics,prometheus`) + Swagger (`/v3/api-docs`, `/swagger-ui/index.html`) | Fora do Docker (`mvnw spring-boot:run`) a porta é **8080** (default do Spring); no compose é 8081 via `SERVER_PORT`. |
| **InventoryService** | 8083 | `GET /api/v1/products`, `GET /api/v1/products/{id}`, `POST /api/v1/products`, `PATCH /api/v1/products/stock` + Actuator (`health,prometheus,metrics,info`) + Swagger | — |
| **PaymentService** | 8082 | **Nenhum endpoint REST.** Só Actuator (`health,prometheus,metrics,info`). | Serviço dirigido a eventos: consome `inventory.reserved`, publica `payment.approved` / `payment.failed`. Sem `springdoc`, portanto sem Swagger. |
| **NotificationService** | 8084 | **Nenhum endpoint REST.** Só Actuator (`health,info,metrics,prometheus`). | Consumidor puro (fan-out da coreografia). Não produz eventos. Sem Swagger. |
| **AuditService** | — (não está no compose) | **Nenhum endpoint.** Esqueleto: só `AuditServiceApplication` + teste de context load. Tem `spring-boot-starter-web` e `-actuator`, mas sem `management.endpoints.web.exposure` configurado, portanto só `GET /actuator/health` responde (default do Boot), na porta 8080. | Trate como não construído. CI só compila (`skip-tests`). |
| **AuthService** | — | **Nenhum endpoint.** Esqueleto (inclui `spring-cloud-starter-gateway-server-webmvc`, sem rotas). Só `/actuator/health` por default, porta 8080. | Idem. |
| **FraudService** | — | **Nenhum endpoint.** Esqueleto. Só `/actuator/health` por default, porta 8080. | Idem. |
| **GatewayService** | — | **Nenhum endpoint.** Esqueleto (Spring Cloud Gateway sem rotas configuradas). Só `/actuator/health` por default, porta 8080. | Idem. |

> `shared/contracts` e `shared/libraries/platform-messaging` são bibliotecas (sem processo HTTP) — não têm collection.

## curls de referência

Assumindo o `docker-compose` (Order em 8081, Payment 8082, Inventory 8083, Notification 8084).

### InventoryService

```bash
# Listar produtos
curl -s http://localhost:8083/api/v1/products

# Criar produto (guarde o "id" retornado)
curl -s -X POST http://localhost:8083/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"SKU-NOTE","name":"Notebook","description":"Notebook 14 polegadas","price":5000,"initialStock":100}'

# Buscar produto por id
curl -s http://localhost:8083/api/v1/products/<PRODUCT_ID>

# Movimentar estoque
#   stockMovementType: IN | OUT
#   stockMovementReason: PURCHASE | RETURN | DAMAGE | LOSS | INVENTORY_ADJUSTMENT | MANUAL
curl -s -X PATCH http://localhost:8083/api/v1/products/stock \
  -H 'Content-Type: application/json' \
  -d '{"productId":"<PRODUCT_ID>","stockMovementType":"IN","quantity":50,"stockMovementReason":"PURCHASE"}'
```

### OrderService

```bash
# Criar pedido (Idempotency-Key é obrigatório; repetir a chave retorna o mesmo pedido)
#   payment.paymentMethod: CREDIT_CARD | DEBIT_CARD | PIX | BOLETO | PAYPAL | APPLE_PAY | GOOGLE_PAY
curl -s -X POST http://localhost:8081/api/v1/orders \
  -H 'Content-Type: application/json' \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{
    "customer": {"customerId":"11111111-1111-1111-1111-111111111111","name":"Ana Souza","email":"ana@example.com","phone":"11999999999"},
    "shippingAddress": {"street":"Rua A","number":"10","city":"Sao Paulo","zipCode":"01000-000","country":"BR"},
    "payment": {"paymentMethod":"CREDIT_CARD","installments":1,"amount":3000},
    "items": [{"productId":"<PRODUCT_ID>","quantity":2,"unitPrice":1500}]
  }'

# Buscar pedido / acompanhar a saga
curl -s http://localhost:8081/api/v1/orders/<ORDER_ID>
```

### Actuator (todos os serviços implementados)

```bash
curl -s http://localhost:8081/actuator/health      # OrderService
curl -s http://localhost:8082/actuator/health      # PaymentService
curl -s http://localhost:8083/actuator/health      # InventoryService
curl -s http://localhost:8084/actuator/health      # NotificationService
# tambem: /actuator/info, /actuator/metrics, /actuator/prometheus
```

### PaymentService e NotificationService

Não há como acionar esses serviços por HTTP. Eles participam da saga por eventos Kafka.
Para exercitá-los, rode o fluxo do OrderService acima (ou os scripts `scripts/e2e-saga.sh`,
`scripts/e2e-saga-declined.sh`, `scripts/e2e-docker.sh`) e observe:

```bash
docker compose -f infrastructure/docker/docker-compose.yml logs -f payment-service notification-service
```
