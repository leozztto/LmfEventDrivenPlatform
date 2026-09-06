# 0007 — AuthService: emissão de JWT stateless com assinatura RSA + JWKS, sem Authorization Server

## Status

Aceito

## Contexto

`AuthService` e `GatewayService` são os dois últimos serviços da plataforma e, por decisão anterior,
ficam **fora do tema event-driven**: são síncronos/REST. O `AuthService` precisa oferecer o básico de
identidade — cadastro de usuários, papéis (roles) e emissão/validação de token — para que o
`GatewayService` possa autenticar as requisições na borda.

O esqueleto gerado trouxe `spring-boot-starter-oauth2-authorization-server` no `pom.xml`. Rodar um
Authorization Server OAuth2/OIDC completo resolve o problema, mas traz um modelo (registro de
clients, fluxos `authorization_code`/`client_credentials`, consentimento) muito maior do que o
necessário — e o Spring Authorization Server **não** suporta o grant de senha (`password`), que é o
fluxo natural de um login usuário/senha. Implementar um grant customizado só para isso seria
complexidade sem retorno num projeto didático.

Três decisões de design apareceram juntas:

1. **Como emitir o token**: fluxo OAuth2 formal (Authorization Server) ou endpoints REST próprios.
2. **Como o Gateway valida**: chamando o `AuthService` a cada requisição, ou validando localmente a
   assinatura.
3. **De onde vem a chave de assinatura**: gerada pela aplicação ou provisionada externamente.

## Decisão

- **Endpoints REST próprios**, sem fluxos OAuth2: `POST /api/v1/auth/register`,
  `POST /api/v1/auth/login` e `GET /api/v1/auth/me`. O `login` devolve um **access token JWT**.
- **Assinatura RS256** via `NimbusJwtEncoder` (do `spring-security-oauth2-jose`, já transitivo do
  `spring-boot-starter-oauth2-resource-server`). Claims: `sub` (username), `roles` (lista já com o
  prefixo `ROLE_`), `email`, `iss`, `iat`, `exp` e `jti`. TTL configurável em `auth.jwt.ttl`
  (default `PT1H`).
- **Par RSA efêmero gerado no startup.** A chave pública é publicada em `GET /oauth2/jwks` (JWK Set,
  RFC 7517) e o Gateway a consome via `jwk-set-uri`, validando os tokens **localmente** — sem
  round-trip ao `AuthService` a cada requisição. Chaves PEM podem ser fornecidas via
  `auth.jwt.public-key`/`auth.jwt.private-key` quando se quer um par estável.
- **Apenas access token na v1** — sem refresh token, sem revogação, sem rotação de chave.
- **Usuários e papéis em Postgres** (`authservice`), senha com **BCrypt**. `Role` é um `enum`
  (`ROLE_USER`, `ROLE_ADMIN`) persistido numa tabela `user_roles` via `@ElementCollection` — sem
  entidade `Role` dedicada.
- Troca de `spring-boot-starter-oauth2-authorization-server` por
  `spring-boot-starter-oauth2-resource-server`; remoção das dependências que não se aplicam ao
  serviço (Kafka, mail, spring-cloud gateway).
- O `AuthService` valida seus próprios tokens (`iss` + expiração) para proteger `/api/v1/auth/me`.

## Consequências

**Positivas:**

- Nenhum segredo para gerenciar/provisionar no ambiente didático — o serviço sobe e funciona.
- O Gateway fica **desacoplado** do AuthService em runtime: valida o token com a chave pública do
  JWKS, um padrão de facto para validação distribuída de JWT.
- Stateless: o AuthService não guarda sessão; escalar horizontalmente é trivial do ponto de vista de
  request handling.
- Curva de aprendizado baixa — o fluxo é o mínimo que um portfólio precisa demonstrar, sem o peso
  conceitual de um IdP OIDC.

**Negativas:**

- **Reiniciar o AuthService invalida todos os tokens já emitidos** (a chave muda). Aceitável com uma
  única instância no docker-compose; múltiplas instâncias exigiriam uma chave compartilhada
  (arquivo/keystore) — fora de escopo.
- **Sem refresh token, rotação ou revogação.** Um token vazado vale até expirar.
- **Não é um IdP OIDC completo**: sem discovery (`/.well-known/openid-configuration`), sem
  `/userinfo`, sem consentimento. Integrações externas que esperam OIDC não funcionam.

**Trabalho futuro:** carregar a chave de um PEM/keystore versionado fora do repositório, refresh
tokens, rotação de chave com múltiplos `kid` no JWKS, e lockout após tentativas de login malsucedidas.
