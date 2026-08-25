# Stage 1: Build the application with Maven & Eclipse Temurin Java 21
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Java 21 Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy jar from builder stage
COPY --from=builder /build/target/ai-sales-crm-agent-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
