#!/bin/bash

set -e

echo "=== Starting Microservices Deployment ==="

# Start PostgreSQL
echo "Starting PostgreSQL..."
cd db
docker-compose up -d

# Wait for PostgreSQL
echo "Waiting for PostgreSQL to be ready..."
sleep 10

# Check database connection
echo "Checking PostgreSQL connection..."
docker exec connection-postgres psql -U test_user -d test_db -c "\dt" > /dev/null 2>&1 || {
    echo "Initializing database..."
    sleep 5
}
cd ..

# Create Kind cluster
echo "Creating Kind cluster..."
kind create cluster --name microservices --config kind-cluster-config.yaml

# Build and load services
echo "Building and loading services..."

# Auth Service
echo "Building auth-service..."
cd auth-service
docker build -t auth-service:latest .
kind load docker-image auth-service:latest --name microservices
cd ..

# Buffer Service  
echo "Building buffer-service..."
cd buffer-service
docker build -t buffer-service:latest .
kind load docker-image buffer-service:latest --name microservices
cd ..

# Deploy to Kubernetes
echo "Deploying to Kubernetes..."

# Deploy Kafka first
kubectl apply -f k8s/kafka.yaml

# Wait for Kafka
echo "Waiting for Kafka to start..."
sleep 30

# Deploy services
kubectl apply -f k8s/auth-service.yaml
kubectl apply -f k8s/buffer-service.yaml

# Wait for services to start
echo "Waiting for services to start..."
sleep 30

# Setup port forwarding in background
echo "Setting up port forwarding..."
kubectl port-forward service/auth-service 8080:8080 > /dev/null 2>&1 &
kubectl port-forward service/buffer-service 8081:8080 > /dev/null 2>&1 &

echo "=== DEPLOYMENT COMPLETE ==="
echo "PostgreSQL:     localhost:5432"
echo "Auth Service:   http://localhost:8080"
echo "Buffer Service: http://localhost:8081"
echo "Kafka:          localhost:9092 (via kubectl port-forward service/kafka 9092:9092)"
echo ""
echo "Check status:"
echo "  kubectl get pods"
echo "  kubectl get services"
echo ""
echo "View logs:"
echo "  kubectl logs -f deployment/auth-service"
echo "  kubectl logs -f deployment/buffer-service"