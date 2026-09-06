# 0008 — GatewayService: validação de JWT na borda, Spring Cloud Gateway WebMVC, rate limit Resilience4j e agregação de OpenAPI

## Status

Aceito

## Contexto

O `GatewayService` é o ponto único de entrada da plataforma. Com o `AuthService` (ADR 0007) emitindo
JWT, três questões precisavam de decisão:

1. **WebFlux ou WebMVC?** O Spring Cloud Gateway tem duas variantes: a reativa (Netty/Reactor) e a
   `server-webmvc` (servlet). O resto da plataforma é todo MVC + JPA bloqueante.
2. **Onde validar o token?** Em cada serviço de negócio (repetindo a configuração de resource server
   seis vezes) ou uma vez, na borda.
3. **Rate limiting sem infraestrutura nova.** O rate limiter clássico do gateway (`RequestRateLimiter`)
   depende de Redis e só existe na variante reativa.
4. **Documentação dispersa** — cada serviço expõe seu próprio Swagger UI numa porta diferente.

## Decisão

- **`spring-cloud-starter-gateway-server-webmvc` (servlet).** Mantém a coerência com o stack MVC/JPA
  do resto da plataforma, o modelo de programação é o mesmo dos outros serviços e não introduz
  Reactor no projeto. O trade-off de throughput não é relevante no contexto didático.
- **Rotas declarativas** em `application.yaml` sob `spring.cloud.gateway.server.webmvc.routes`
  (prefixo novo do spring-cloud 2025.0.x; o antigo `spring.cloud.gateway.mvc.routes` está
  deprecado). Rotas para os cinco serviços com REST (`orders`, `products`, `blocklist`,
  `audit-events`) mais `auth` e `oauth2/jwks`, com as URIs vindas de variáveis de ambiente
  (`ORDER_SERVICE_URL`, ...) com default `localhost` para execução fora do Docker.
- **Validação de JWT na borda.** O Gateway é um **OAuth2 Resource Server** com
  `jwk-set-uri` apontando para o `/oauth2/jwks` do AuthService; valida assinatura e expiração antes
  de rotear. O claim `roles` vira authorities; `/api/v1/blocklist/**` e `/api/v1/audit-events/**`
  exigem `ROLE_ADMIN`, o restante exige apenas um token válido; `/api/v1/auth/**`, `/oauth2/jwks`,
  `/actuator/**` e o Swagger são públicos. 401/403 são devolvidos com corpo `ErrorResponse` JSON.
- **Rate limiting com Resilience4j `RateLimiter` num `OncePerRequestFilter` global.** Verificou-se
  que o gateway WebMVC **não** traz um filtro de rate limit baseado em Resilience4j (o único
  embutido é o do Bucket4j, que exigiria `bucket4j-core`/`bucket4j-caffeine` e um `AsyncProxyManager`,
  e cujo `keyResolver` só é configurável via DSL Java). Como o gateway WebMVC é servlet, um filtro
  `OncePerRequestFilter` de alta precedência roda **antes** do roteamento e reaproveita o
  `spring-cloud-starter-circuitbreaker-resilience4j` que já está no `pom.xml`. A chave é o `sub` do
  JWT quando autenticado, senão o IP de origem; ao exceder o limite (`gateway.ratelimit.*`) a
  resposta é 429.
- **Agregação de OpenAPI** via `springdoc.swagger-ui.urls` + rotas `/aggregate/{service}/v3/api-docs`
  (com `RewritePath` para o `/v3/api-docs` do downstream). Apenas `order-service` e
  `inventory-service`, que são os únicos serviços com `springdoc` hoje.
- **Sem banco de dados** — o Gateway é stateless. Removidos `data-jpa`, `postgresql` e Flyway do
  `pom.xml`.

## Consequências

**Positivas:**

- Um único ponto de autenticação: os serviços de negócio não precisam configurar resource server.
- Tokens validados **sem chamar o AuthService** a cada requisição (só o JWKS é buscado, e cacheado).
- Documentação unificada em `http://localhost:8088/swagger-ui.html`, com um seletor por serviço.
- Nenhuma dependência de infraestrutura nova (sem Redis).

**Negativas:**

- **Rate limit em memória e por instância** — não é distribuído. Duas réplicas do Gateway teriam,
  cada uma, seu próprio contador.
- A variante WebMVC tem teto de throughput menor que a reativa sob alta concorrência de I/O.
- O Gateway vira um **ponto único de falha** — mitigável com réplicas atrás de um load balancer,
  fora de escopo aqui.
- As rotas ficam acopladas ao DNS/portas dos serviços via variáveis de ambiente; um serviço novo
  exige editar o `application.yaml` do Gateway.
- **Defesa em profundidade incompleta**: como só o Gateway valida o token, uma requisição que chegue
  direto a um serviço de negócio (dentro da rede) não é autenticada. Validar o token também no
  downstream é trabalho futuro.

**Trabalho futuro:** rate limit distribuído, circuit breaker/retry por rota (Resilience4j já está no
classpath), e propagação de `correlationId` + identidade do usuário como headers para o downstream.
