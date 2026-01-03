.PHONY: help build test test-unit test-integration test-e2e test-all coverage lint fmt clean run docker-build docker-run install-tools

# Variables
BINARY_NAME=prometheus-alerts-handler
DOCKER_IMAGE=prometheus-alerts-handler
VERSION?=$(shell git describe --tags --always --dirty)
BUILD_TIME=$(shell date -u '+%Y-%m-%d_%H:%M:%S')
LDFLAGS=-ldflags "-w -s -X main.Version=${VERSION} -X main.BuildTime=${BUILD_TIME}"

# Default target
help:
	@echo "Available targets:"
	@echo "  build            - Build the application"
	@echo "  test             - Run all tests"
	@echo "  test-unit        - Run unit tests"
	@echo "  test-integration - Run integration tests"
	@echo "  test-e2e         - Run end-to-end tests"
	@echo "  coverage         - Generate test coverage report"
	@echo "  lint             - Run linters"
	@echo "  fmt              - Format code"
	@echo "  clean            - Clean build artifacts"
	@echo "  run              - Run the application"
	@echo "  docker-build     - Build Docker image"
	@echo "  docker-run       - Run Docker container"
	@echo "  install-tools    - Install development tools"

# Build
build:
	@echo "Building ${BINARY_NAME}..."
	go build ${LDFLAGS} -o ${BINARY_NAME} .

build-all:
	@echo "Building for multiple platforms..."
	GOOS=linux GOARCH=amd64 go build ${LDFLAGS} -o ${BINARY_NAME}-linux-amd64 .
	GOOS=linux GOARCH=arm64 go build ${LDFLAGS} -o ${BINARY_NAME}-linux-arm64 .
	GOOS=darwin GOARCH=amd64 go build ${LDFLAGS} -o ${BINARY_NAME}-darwin-amd64 .
	GOOS=darwin GOARCH=arm64 go build ${LDFLAGS} -o ${BINARY_NAME}-darwin-arm64 .
	GOOS=windows GOARCH=amd64 go build ${LDFLAGS} -o ${BINARY_NAME}-windows-amd64.exe .

# Testing
test: test-unit test-integration test-e2e

test-unit:
	@echo "Running unit tests..."
	go test -v -race -coverprofile=coverage.out ./handler/... ./processors/... ./config/... ./metrics/...

test-integration:
	@echo "Running integration tests..."
	go test -v ./tests/integration/...

test-e2e:
	@echo "Running E2E tests..."
	go test -v -timeout 5m ./e2e/...

test-all:
	@echo "Running all tests with coverage..."
	go test -v -race -coverprofile=coverage.out -covermode=atomic ./...

coverage: test-all
	@echo "Generating coverage report..."
	go tool cover -html=coverage.out -o coverage.html
	@echo "Coverage report generated: coverage.html"
	go tool cover -func=coverage.out | grep total | awk '{print "Total coverage: " $$3}'

# Code quality
lint:
	@echo "Running linters..."
	golangci-lint run --timeout 5m

fmt:
	@echo "Formatting code..."
	gofmt -s -w .
	goimports -w .

# Run
run:
	@echo "Running ${BINARY_NAME}..."
	go run .

run-dev:
	@echo "Running in development mode..."
	CONFIG_PATH=config.yaml go run .

# Docker
docker-build:
	@echo "Building Docker image..."
	docker build -t ${DOCKER_IMAGE}:${VERSION} -t ${DOCKER_IMAGE}:latest .

docker-run:
	@echo "Running Docker container..."
	docker run -p 8080:8080 -p 2112:2112 -v $(PWD)/config.yaml:/app/config.yaml ${DOCKER_IMAGE}:latest

docker-compose-up:
	@echo "Starting services with docker-compose..."
	docker-compose up -d

docker-compose-down:
	@echo "Stopping services..."
	docker-compose down

# Development tools
install-tools:
	@echo "Installing development tools..."
	go install github.com/golangci/golangci-lint/cmd/golangci-lint@latest
	go install golang.org/x/tools/cmd/goimports@latest

# Cleanup
clean:
	@echo "Cleaning up..."
	rm -f ${BINARY_NAME}
	rm -f ${BINARY_NAME}-*
	rm -f coverage.out coverage.html
	rm -f prometheus-alerts-handler-test
	rm -f test-config.yaml
	go clean -cache -testcache

# Dependencies
deps:
	@echo "Downloading dependencies..."
	go mod download

deps-update:
	@echo "Updating dependencies..."
	go get -u ./...
	go mod tidy

# CI commands
ci-lint: lint

ci-test: test-all

ci-build: build

ci: ci-lint ci-test ci-build
	@echo "CI pipeline completed successfully!"
