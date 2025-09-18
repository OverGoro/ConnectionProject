#!/bin/bash

# Start PostgreSQL separately
echo "Starting PostgreSQL..."
cd db
docker-compose up -d

# Check if DB is ready
echo "Checking PostgreSQL connection..."
docker exec connection-postgres psql -U test_user -d test_db -c "\dt" || {
    echo "Database not ready, initializing..."
    sleep 5
}
cd ..

# Create kind cluster (without DB)
echo "Creating Kind cluster..."
kind create cluster --name microservices --config kind-cluster-config.yaml

# Build and load device-service
echo "Building and loading device-service..."
cd device-service
docker build -t device-service:latest .
kind load docker-image device-service:latest --name microservices
cd ..

# Build and load auth-service
echo "Building and loading auth-service..."
cd auth-service
docker build -t auth-service:latest .
kind load docker-image auth-service:latest --name microservices
cd ..

# List loaded
docker exec microservices-control-plane crictl images

# Deploy to Kubernetes (only services)
echo "Deploying services to Kubernetes..."
kubectl apply -f k8s/device-service.yaml
kubectl apply -f k8s/auth-service.yaml

# Wait for services to start
echo "Waiting for services to start..."
sleep 5

# Set up port forwarding
echo "Setting up port forwarding..."
kubectl port-forward service/device-service 8081:8080 > /dev/null 2>&1 &
kubectl port-forward service/auth-service 8080:8080 > /dev/null 2>&1 &

echo "=== DEPLOYMENT COMPLETE ==="
echo "PostgreSQL:     localhost:5432 (user: test_user, pass: test_password, db: test_db)"
echo "Device Service: http://localhost:8081"
echo "Auth Service:   http://localhost:8080"
echo ""
echo "Check status: kubectl get pods"
