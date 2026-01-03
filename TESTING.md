# Testing Guide

This document describes the testing strategy and how to run tests for the Prometheus Alerts Handler.

## Test Structure

The project has three levels of testing:

```
prometheus-handler/
├── handler/handler_test.go         # Unit tests for HTTP handlers
├── processors/*_test.go             # Unit tests for processors
├── tests/integration/               # Integration tests
│   └── processors_test.go
└── e2e/                            # End-to-end tests
    └── e2e_test.go
```

## Running Tests

### All Tests

```bash
# Run all tests
make test

# Run all tests with coverage
make test-all
make coverage
```

### Unit Tests

Unit tests test individual components in isolation:

```bash
# Run unit tests only
make test-unit

# Or directly with go test
go test ./handler/... ./processors/... ./config/... ./metrics/...
```

**Coverage:** Unit tests cover:
- HTTP handler logic
- Alert processing
- Error handling
- Edge cases
- Input validation

### Integration Tests

Integration tests verify that components work together correctly:

```bash
# Run integration tests
make test-integration

# Or directly
go test ./tests/integration/...
```

**Coverage:** Integration tests cover:
- Webhook processor with real HTTP server
- Processor registry coordination
- Concurrent alert processing
- Factory pattern processor creation
- Error propagation between components

### End-to-End Tests

E2E tests run the complete application and test it via HTTP API:

```bash
# Run E2E tests
make test-e2e

# Or directly (with timeout)
go test -v -timeout 5m ./e2e/...
```

**Coverage:** E2E tests cover:
- Complete application startup
- Health check endpoint
- Metrics endpoint
- Alert processing via REST API
- Both simple and AlertManager format
- Error responses
- Concurrent requests
- Metrics increment verification

## Test Environment

### E2E Test Setup

E2E tests automatically:
1. Build the application binary
2. Create a test configuration
3. Start the server on test ports (18080, 12112)
4. Wait for server to be healthy
5. Run tests
6. Clean up resources

No manual setup required!

### Docker Compose Testing

For full integration testing with Prometheus and Alertmanager:

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f alerts-handler

# Send test alert via Alertmanager
curl -X POST http://localhost:9093/api/v1/alerts -d '[
  {
    "labels": {
      "alertname": "TestAlert",
      "severity": "critical"
    },
    "annotations": {
      "summary": "Test alert from docker-compose"
    }
  }
]'

# Stop services
docker-compose down
```

## Coverage Reports

### Generate Coverage Report

```bash
# Generate HTML coverage report
make coverage

# View coverage in browser
open coverage.html  # macOS
xdg-open coverage.html  # Linux
```

### Coverage Goals

| Component | Current | Target |
|-----------|---------|--------|
| Handler   | ~85%    | 85%    |
| Processors | ~60%   | 75%    |
| Config    | 0%      | 70%    |
| Overall   | ~65%    | 75%    |

## Continuous Integration

### GitHub Actions

Tests run automatically on:
- Push to main, develop, or claude/** branches
- Pull requests to main or develop

CI Pipeline includes:
1. **Lint** - Code quality checks
2. **Unit Tests** - Fast component tests
3. **Integration Tests** - Component interaction tests
4. **E2E Tests** - Full application tests
5. **Build** - Multi-platform builds
6. **Security** - Vulnerability scanning
7. **Docker** - Container image build

### Running CI Locally

```bash
# Run complete CI pipeline
make ci

# Individual CI steps
make ci-lint
make ci-test
make ci-build
```

## Writing Tests

### Unit Test Example

```go
func TestAlertsHandler_InvalidJSON(t *testing.T) {
    registry := processors.NewRegistry()
    registry.Register(&processors.BasicProcessor{})
    h := handler.NewHandler(registry)

    req, err := http.NewRequest("POST", "/alerts",
        bytes.NewBufferString("invalid json"))
    assert.NoError(t, err)

    rr := httptest.NewRecorder()
    h.ServeHTTP(rr, req)

    assert.Equal(t, http.StatusBadRequest, rr.Code)
}
```

### Integration Test Example

```go
func TestWebhookProcessor_Integration(t *testing.T) {
    // Create test server
    server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        // Verify request
        assert.Equal(t, "POST", r.Method)
        w.WriteHeader(http.StatusOK)
    }))
    defer server.Close()

    // Create processor
    config := map[string]interface{}{
        "url": server.URL,
    }
    processor, err := processors.NewWebhookProcessor(config)
    require.NoError(t, err)

    // Test processing
    alert := types.Alert{Status: "firing"}
    processor.Process(alert)
}
```

### E2E Test Example

```go
func TestE2E_AlertsEndpoint(t *testing.T) {
    client := &http.Client{Timeout: 5 * time.Second}

    alerts := []types.Alert{{
        Status: "firing",
        Labels: map[string]string{"alertname": "Test"},
    }}

    payload, _ := json.Marshal(alerts)
    resp, err := client.Post(serverURL+"/alerts",
        "application/json", bytes.NewBuffer(payload))

    require.NoError(t, err)
    defer resp.Body.Close()

    assert.Equal(t, http.StatusOK, resp.StatusCode)
}
```

## Test Data

### Sample Alerts

Simple format:
```json
[
  {
    "status": "firing",
    "labels": {
      "alertname": "HighCPU",
      "severity": "critical",
      "instance": "server-1"
    },
    "annotations": {
      "summary": "High CPU usage",
      "description": "CPU usage above 90%"
    }
  }
]
```

AlertManager format:
```json
{
  "version": "4",
  "groupKey": "{}:{alertname=\"HighCPU\"}",
  "status": "firing",
  "receiver": "prometheus-alerts-handler",
  "alerts": [
    {
      "status": "firing",
      "labels": {
        "alertname": "HighCPU",
        "severity": "critical"
      },
      "annotations": {
        "summary": "High CPU usage"
      },
      "startsAt": "2024-01-01T00:00:00Z",
      "generatorURL": "http://prometheus:9090/graph"
    }
  ]
}
```

## Debugging Tests

### Verbose Output

```bash
# Run with verbose output
go test -v ./...

# Run specific test
go test -v -run TestAlertsHandler ./handler/...
```

### Race Detection

```bash
# Detect race conditions
go test -race ./...
```

### Test Coverage by Function

```bash
# Show coverage per function
go test -coverprofile=coverage.out ./...
go tool cover -func=coverage.out
```

## Performance Testing

### Benchmarks

```bash
# Run benchmarks
go test -bench=. ./...

# With memory profiling
go test -bench=. -benchmem ./...
```

### Load Testing

Use tools like `hey` or `ab` for load testing:

```bash
# Install hey
go install github.com/rakyll/hey@latest

# Load test alerts endpoint
hey -n 1000 -c 10 -m POST \
  -H "Content-Type: application/json" \
  -d '[{"status":"firing","labels":{"alertname":"LoadTest"}}]' \
  http://localhost:8080/alerts
```

## Troubleshooting

### E2E Tests Fail to Start Server

**Issue:** Server doesn't become healthy
**Solution:**
- Check if ports 18080 or 12112 are already in use
- Increase `serverStartDelay` in e2e_test.go
- Check server logs for errors

### Integration Tests Timeout

**Issue:** Tests hang or timeout
**Solution:**
- Check for deadlocks in concurrent code
- Verify HTTP client timeouts are set
- Use `-timeout` flag: `go test -timeout 2m ./tests/integration/...`

### Coverage Too Low

**Issue:** Coverage below target
**Solution:**
1. Identify uncovered code: `go tool cover -html=coverage.out`
2. Add tests for error cases
3. Add tests for edge cases
4. Test all code paths (if/else branches)

## Best Practices

1. **Test Naming**: Use descriptive names that explain what is being tested
2. **Table-Driven Tests**: Use for testing multiple scenarios
3. **Isolation**: Tests should not depend on each other
4. **Cleanup**: Always clean up resources (use `defer`)
5. **Timeouts**: Set appropriate timeouts for HTTP clients
6. **Assertions**: Use meaningful assertion messages
7. **Coverage**: Aim for 75%+ coverage, 100% for critical paths

## Resources

- [Go Testing Documentation](https://golang.org/pkg/testing/)
- [Testify Library](https://github.com/stretchr/testify)
- [Go Testing Best Practices](https://golang.org/doc/effective_go#testing)
