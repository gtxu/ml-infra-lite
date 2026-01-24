# --- Stage 1: Build ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies by copying only POMs first
COPY pom.xml .
COPY ml-infra-api/pom.xml ml-infra-api/
COPY ml-infra-core/pom.xml ml-infra-core/
COPY ml-infra-server/pom.xml ml-infra-server/
RUN mvn dependency:go-offline -B

# Copy source and build the fat jar
COPY . .
RUN mvn clean install -DskipTests

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install networking tools for health checks and debugging
RUN apt-get update && apt-get install -y \
    iputils-ping \
    netcat-openbsd \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create writable temp directory for models
RUN mkdir -p /tmp/models && chmod 777 /tmp/models

# Copy the fat jar from build stage
COPY --from=build /app/ml-infra-server/target/ml-infra-server-1.0-SNAPSHOT.jar app.jar
COPY config.yml config.yml

# 8080: App, 8081: Admin/Metrics
EXPOSE 8080 8081

# Startup with Dropwizard 'server' command
ENTRYPOINT ["java", "-Djava.io.tmpdir=/tmp", "-jar", "app.jar", "server", "config.yml"]