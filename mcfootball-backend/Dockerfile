# ─────────────────────────────────────────────────────────────────────
# mcfootball-backend — multi-stage Docker build
# Spring Boot 2.7.18 / Java 11 / Gradle multi-project
#
# Build context must be the PROJECT ROOT (MCFootball/), not this dir,
# because the backend depends on :mcfootball-generator.
#
# Usage:
#   cd /path/to/MCFootball
#   docker build -f mcfootball-backend/Dockerfile -t mcfootball-backend .
#   docker run --rm -p 8080:8080 -e API_FOOTBALL_KEY=your_key mcfootball-backend
# ─────────────────────────────────────────────────────────────────────

# Stage 1: Build the fat JAR
FROM gradle:7.6.4-jdk11-alpine AS build

WORKDIR /project

# Copy Gradle wrapper + config first (layer caching)
COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle gradle.properties ./

# Copy both modules (backend depends on generator)
COPY mcfootball-generator/ mcfootball-generator/
COPY mcfootball-backend/ mcfootball-backend/

# Copy grammar + models (needed by generator at compile time)
COPY models/ models/

# Build the backend JAR (tests run in CI, not here)
RUN chmod +x gradlew && \
    ./gradlew :mcfootball-backend:bootJar --no-daemon -x test

# Stage 2: Slim runtime image
FROM eclipse-temurin:11-jre-alpine

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy built JAR from builder stage
COPY --from=build /project/mcfootball-backend/build/libs/*.jar app.jar

# HTTP port
EXPOSE 8080

# Runtime config via environment variables (no secrets baked in)
ENV SERVER_PORT=8080
# Override at runtime:
#   -e API_FOOTBALL_KEY=your_key
#   -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mcfootball

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
