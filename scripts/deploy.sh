#!/bin/bash

# Exit on any error
set -e

# Validate arguments
export IMAGE_TAG=$1
export REGISTRY=$2

if [ -z "$IMAGE_TAG" ] || [ -z "$REGISTRY" ]; then
  echo "Error: Missing arguments."
  echo "Usage: ./deploy.sh <image_tag> <registry>"
  exit 1
fi

echo "=========================================="
echo "Deploying AmazonLite Backend Services"
echo "Registry: $REGISTRY"
echo "Image Tag: $IMAGE_TAG"
echo "=========================================="

# Authenticate to GHCR (Assumes the server is already authenticated, or GITHUB_TOKEN is available)
# If private, you should run `docker login ghcr.io -u <username> -p <token>` before this script.

echo "1. Pulling the new Docker images..."
docker-compose pull

echo "2. Re-creating and starting containers..."
# docker-compose up -d will automatically stop and recreate containers whose images or configurations have changed, 
# while leaving unchanged containers running.
docker-compose up -d

echo "3. Cleaning up old images..."
docker image prune -f

echo "4. Verifying deployment..."
# Wait a few seconds
sleep 5
docker-compose ps

echo "✅ Backend deployment script completed."
