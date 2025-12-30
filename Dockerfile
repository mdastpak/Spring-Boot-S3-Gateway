# ============================================================================
# S3/MinIO Storage Gateway - Multi-Stage Docker Build
# ============================================================================
# This Dockerfile creates an optimized production-ready image for the
# storage gateway service that connects to external S3/MinIO services.
# ============================================================================

# ============================================================================
# Stage 1: Build Stage
# ============================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

LABEL stage=builder
LABEL app="storage-gateway"
LABEL description="S3/MinIO Storage Gateway Service - Build Stage"

WORKDIR /build

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (cached layer if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests -B && \
    mkdir -p target/dependency && \
    cd target/dependency && \
    jar -xf ../*.jar

# ============================================================================
# Stage 2: Runtime Stage
# ============================================================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="your-email@example.com"
LABEL app="storage-gateway"
LABEL description="S3/MinIO Storage Gateway Service"
LABEL version="0.0.1-SNAPSHOT"

# Install wget for health checks and curl for debugging
RUN apk add --no-cache wget curl tzdata && \
    rm -rf /var/cache/apk/*

# Set timezone (optional, configure via environment variable)
ENV TZ=UTC

WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && \
    adduser -S spring -G spring && \
    chown -R spring:spring /app

# Copy JAR from build stage
COPY --from=build --chown=spring:spring /build/target/*.jar app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s \
            --timeout=3s \
            --start-period=40s \
            --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=50.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
