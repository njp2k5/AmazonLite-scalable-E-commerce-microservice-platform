#!/bin/bash

# Setup signal handlers to gracefully terminate all child Java processes
trap 'echo "Stopping all services..."; kill 0; exit 0' SIGTERM SIGINT

# All internal services communicate through localhost inside the same container
EUREKA_URL="http://localhost:8761/eureka/"

# ── STEP 1: Start Gateway FIRST on $PORT (0.0.0.0) ───────────────────────────
# Gateway binds to ALL interfaces (0.0.0.0) so Render's port scanner can detect it.
# All other services bind to 127.0.0.1 ONLY so Render never sees them.
# should-enforce-registration-at-init=false lets Gateway start before Eureka is ready.
GATEWAY_PORT="${PORT:-8080}"
echo "==> Starting Gateway Service on 0.0.0.0:${GATEWAY_PORT} [PUBLIC ENTRY POINT]"
java \
  -Dserver.port="$GATEWAY_PORT" \
  -Dserver.address=0.0.0.0 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.client.should-enforce-registration-at-init=false \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar gateway-service.jar &

# ── STEP 2: Start Eureka on 127.0.0.1:8761 (loopback only) ──────────────────
# 127.0.0.1 binding is invisible to Render's external port scanner.
# All in-container services can still reach it via localhost:8761.
echo "==> Starting Registry Service on 127.0.0.1:8761 [INTERNAL ONLY]"
java \
  -Dserver.port=8761 \
  -Dserver.address=127.0.0.1 \
  -jar registry-service.jar &

# ── STEP 3: Wait for Eureka to be ready ──────────────────────────────────────
echo "==> Waiting for Registry Service..."
until curl -sf http://localhost:8761/actuator/health | grep -q '"UP"'; do
  sleep 3
done
echo "==> Registry Service is UP at localhost:8761"

# ── STEP 4: Start remaining microservices on 127.0.0.1 (loopback only) ───────
# All internal — Render sees none of them.
echo "==> Starting backend microservices (127.0.0.1 only)..."
java \
  -Dserver.port=8082 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar auth-service.jar &

java \
  -Dserver.port=8081 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar product-service.jar &

java \
  -Dserver.port=8083 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar order-service.jar &

java \
  -Dserver.port=8085 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar inventory-service.jar &

java \
  -Dserver.port=8084 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar notification-service.jar &

java \
  -Dserver.port=8086 \
  -Dserver.address=127.0.0.1 \
  -Deureka.client.service-url.defaultZone="$EUREKA_URL" \
  -Deureka.instance.hostname=localhost \
  -Deureka.instance.prefer-ip-address=true \
  -jar cart-service.jar &

echo "==> All services started."
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
