#!/bin/bash

# Setup signal handlers to gracefully terminate all child Java processes
trap 'echo "Stopping all services..."; kill 0; exit 0' SIGTERM SIGINT

# Internal Eureka URL — all services communicate via localhost inside this container
EUREKA_URL="http://localhost:8761/eureka/"

# ── STEP 1: Start Gateway FIRST on $PORT ──────────────────────────────────────
# Render detects the public port by seeing which port becomes active first.
# Gateway must bind to $PORT before anything else so Render maps the public URL
# to the Gateway, NOT to Eureka.
# Spring Cloud Gateway connects to Eureka lazily — it will not crash if Eureka
# is not yet available and will retry in the background.
GATEWAY_PORT="${PORT:-8080}"
echo "Starting Gateway Service on port ${GATEWAY_PORT} (public entry point)..."
java \
  -Dserver.port="$GATEWAY_PORT" \
  -Dserver.address=0.0.0.0 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar gateway-service.jar &

# ── STEP 2: Start Eureka in the background in parallel ────────────────────────
# Starts concurrently with Gateway. Gateway's ~30s JVM boot gives Eureka enough
# time to become ready before Gateway's first Eureka heartbeat fires.
echo "Starting Registry Service on port 8761 (internal)..."
java \
  -Dserver.port=8761 \
  -jar registry-service.jar &

# ── STEP 3: Wait for Eureka before starting the downstream services ────────────
echo "Waiting for Registry Service to become available..."
until curl -sf http://localhost:8761/actuator/health | grep -q '"UP"'; do
  sleep 3
done
echo "Registry Service is UP!"

# ── STEP 4: Start all remaining microservices ──────────────────────────────────
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

echo "All services started. Container is running."

# Keep container alive; exit if any child process dies
wait -n
