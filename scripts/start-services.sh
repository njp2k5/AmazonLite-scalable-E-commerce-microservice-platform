#!/bin/bash

# Setup signal handlers to gracefully terminate all child Java processes
trap 'echo "Stopping all services..."; kill 0; exit 0' SIGTERM SIGINT

# All internal services communicate through localhost inside the same container
EUREKA_URL="http://localhost:8761/eureka/"
GATEWAY_PORT="${PORT:-8080}"

# ── MEMORY LIMITS ─────────────────────────────────────────────────────────────
# Strict JVM memory constraints to prevent Render memory limit exhaustion (OOM).
# Base memory profile (Total: ~64MB Heap + ~128MB Metaspace overhead per JVM)
COMMON_JAVA_OPTS="-Xms64m -Xmx64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Xss256k"
# Gateway needs slightly more heap to handle Netty traffic buffers safely
GATEWAY_JAVA_OPTS="-Xms64m -Xmx128m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Xss256k"
# Eureka only holds routing information, so memory needs are small
EUREKA_JAVA_OPTS="-Xms64m -Xmx64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -Xss256k"

echo "==> Applying memory limits to prevent container OOM..."

# ── PHASE 1: Start Eureka ─────────────────────────────────────────────────────
echo "==> [PHASE 1] Starting Registry Service on 127.0.0.1:8761 (INTERNAL ONLY)"
java $EUREKA_JAVA_OPTS \
  -Dserver.port=8761 \
  -Dserver.address=127.0.0.1 \
  -jar registry-service.jar &

# ── PHASE 2: Wait for Eureka ──────────────────────────────────────────────────
echo "==> [PHASE 2] Waiting for Registry Service to become available..."
until curl -sf http://localhost:8761/actuator/health | grep -q '"UP"'; do
  sleep 3
done
echo "==> Registry Service is UP at localhost:8761"

# ── PHASE 3: Start Gateway ────────────────────────────────────────────────────
# Gateway binds to ALL interfaces (0.0.0.0) so Render's port scanner can detect it.
echo "==> [PHASE 3] Starting Gateway Service on 0.0.0.0:${GATEWAY_PORT} (PUBLIC ENTRY POINT)"
java $GATEWAY_JAVA_OPTS \
  -Dserver.port="$GATEWAY_PORT" \
  -Dserver.address=0.0.0.0 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.client.should-enforce-registration-at-init=false \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar gateway-service.jar &

# ── PHASE 4: Wait for Gateway Public Port ─────────────────────────────────────
echo "==> [PHASE 4] Waiting for Gateway Service to bind to port ${GATEWAY_PORT}..."
until curl -sf http://localhost:${GATEWAY_PORT}/actuator/health | grep -q '"UP"'; do
  sleep 3
done
echo "==> Gateway Service is listening and ready on 0.0.0.0:${GATEWAY_PORT}!"

# ── PHASE 5: Stagger Remaining Services ───────────────────────────────────────
# Start remaining microservices one by one with a small sleep between them.
# This prevents all 6 JVMs from slamming the CPU and RAM with class loading simultaneously.
echo "==> [PHASE 5] Staggering backend microservice startup on 127.0.0.1 (INTERNAL ONLY)..."

java $COMMON_JAVA_OPTS \
  -Dserver.port=8082 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar auth-service.jar &
sleep 2

java $COMMON_JAVA_OPTS \
  -Dserver.port=8081 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar product-service.jar &
sleep 2

java $COMMON_JAVA_OPTS \
  -Dserver.port=8083 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar order-service.jar &
sleep 2

java $COMMON_JAVA_OPTS \
  -Dserver.port=8085 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar inventory-service.jar &
sleep 2

java $COMMON_JAVA_OPTS \
  -Dserver.port=8084 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar notification-service.jar &
sleep 2

java $COMMON_JAVA_OPTS \
  -Dserver.port=8086 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar cart-service.jar &

echo "==> All 8 services successfully launched!"
echo "==>   PUBLIC:   Gateway  -> 0.0.0.0:${GATEWAY_PORT}"
echo "==>   INTERNAL: Eureka   -> 127.0.0.1:8761"
echo "==>   INTERNAL: Auth     -> 127.0.0.1:8082"
echo "==>   INTERNAL: Product  -> 127.0.0.1:8081"
echo "==>   INTERNAL: Order    -> 127.0.0.1:8083"
echo "==>   INTERNAL: Inventory-> 127.0.0.1:8085"
echo "==>   INTERNAL: Notif    -> 127.0.0.1:8084"
echo "==>   INTERNAL: Cart     -> 127.0.0.1:8086"

# Keep container alive; exit if any child process dies
wait -n
