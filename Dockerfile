# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY services/ ./services/

# Build all services
RUN echo "Building services..." && \
    mkdir -p /app/jars && \
    for dir in services/*-service; do \
        if [ -d "$dir" ] && [ -f "$dir/pom.xml" ]; then \
            echo "Packaging service: $dir"; \
            cd "$dir" && mvn clean package -DskipTests -B && cd ../..; \
            SERVICE_NAME=$(basename "$dir"); \
            find "$dir/target" -maxdepth 1 -name "*.jar" ! -name "*original*" -exec cp {} "/app/jars/${SERVICE_NAME}.jar" \;; \
        fi; \
    done

# Runtime Stage
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Install necessary runtime utilities
RUN apk add --no-cache curl bash

# Copy all JARs from builder
COPY --from=builder /app/jars/*.jar ./

# Copy startup script
COPY scripts/start-services.sh start-services.sh
RUN chmod +x start-services.sh

# Expose Gateway port (Render will route public traffic here)
EXPOSE 8080

LABEL org.opencontainers.image.source="https://github.com/njp2k5/AmazonLite-scalable-E-commerce-microservice-platform"

CMD ["./start-services.sh"]
