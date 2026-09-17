#!/bin/bash

# Setup signal handlers to gracefully terminate all child Java processes
trap 'echo "Stopping all services..."; kill 0; exit 0' SIGTERM SIGINT


echo "Starting Registry Service..."
java -jar registry-service.jar &

echo "Waiting for Registry Service to become available..."
while ! curl -s http://localhost:8761/actuator/health | grep -q 'UP'; do
  sleep 2
done
echo "Registry Service is UP!"

echo "Starting backend microservices..."
java -jar auth-service.jar &
java -jar product-service.jar &
java -jar order-service.jar &
java -jar inventory-service.jar &
java -jar notification-service.jar &
java -jar cart-service.jar &

echo "Starting Gateway Service..."
java -jar gateway-service.jar &

echo "All services have been started. Container is now running."

# Wait for any process to exit (if one crashes, the container will exit and can be restarted)
wait -n
