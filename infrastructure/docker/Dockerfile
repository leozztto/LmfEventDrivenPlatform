# Dockerfile genérico para os serviços Spring Boot do monorepo.
# O contexto de build é a raiz do repositório; o módulo alvo é escolhido via ARG MODULE
# (ex.: services/OrderService). O reator do pom.xml agregador compila só o módulo e as
# libs compartilhadas de que ele depende (-pl <módulo> -am).

# ---------- stage de build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copia só o necessário para o reator: o agregador, os wrappers e todos os módulos
# (o agregador lista os dez módulos, então todos os pom.xml precisam existir).
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
COPY shared/ shared/
COPY services/ services/

ARG MODULE
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests -pl "${MODULE}" -am package

# ---------- stage de runtime ----------
FROM eclipse-temurin:17-jre AS runtime
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app

ARG MODULE
COPY --from=build /workspace/${MODULE}/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
