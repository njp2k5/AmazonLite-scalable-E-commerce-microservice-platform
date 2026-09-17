#!/bin/bash

# Setup signal handlers to gracefully terminate all child Java processes
trap 'echo "Stopping all services..."; kill 0; exit 0' SIGTERM SIGINT

# Internal Eureka URL — all services in this container communicate via localhost
EUREKA_URL="http://localhost:8761/eureka/"

echo "Starting Registry Service on port 8761..."
java \
  -Dserver.port=8761 \
  -jar registry-service.jar &

echo "Waiting for Registry Service to become available..."
until curl -sf http://localhost:8761/actuator/health | grep -q '"UP"'; do
  sleep 3
done
echo "Registry Service is UP!"

echo "Starting backend microservices..."
java \
  -Dserver.port=8082 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar auth-service.jar &

java \
  -Dserver.port=8081 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar product-service.jar &

java \
  -Dserver.port=8083 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar order-service.jar &

java \
  -Dserver.port=8085 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar inventory-service.jar &

java \
  -Dserver.port=8084 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar notification-service.jar &

java \
  -Dserver.port=8086 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar cart-service.jar &

# Gateway listens on $PORT (Render) or 8080 (local) — explicitly passed here
GATEWAY_PORT="${PORT:-8080}"
echo "Starting Gateway Service on port ${GATEWAY_PORT}..."
java \
  -Dserver.port="$GATEWAY_PORT" \
  -Dserver.address=0.0.0.0 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar gateway-service.jar &

echo "All services started. Container is running."

# Keep container alive; exit if any child process dies
wait -n
