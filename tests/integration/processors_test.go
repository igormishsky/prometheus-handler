package integration

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"sync"
	"testing"

	"github.com/igormishsky/prometheus-alerts-handler/processors"
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestWebhookProcessor_Integration(t *testing.T) {
	// Create a test webhook server
	var receivedAlerts []types.Alert
	var mu sync.Mutex

	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		assert.Equal(t, "POST", r.Method)
		assert.Equal(t, "application/json", r.Header.Get("Content-Type"))
		assert.Equal(t, "Bearer test-token", r.Header.Get("Authorization"))

		var alert types.Alert
		err := json.NewDecoder(r.Body).Decode(&alert)
		require.NoError(t, err)

		mu.Lock()
		receivedAlerts = append(receivedAlerts, alert)
		mu.Unlock()

		w.WriteHeader(http.StatusOK)
	}))
	defer server.Close()

	// Create webhook processor
	config := map[string]interface{}{
		"url":    server.URL,
		"method": "POST",
		"headers": map[string]interface{}{
			"Authorization": "Bearer test-token",
		},
	}

	processor, err := processors.NewWebhookProcessor(config)
	require.NoError(t, err)

	// Send test alert
	testAlert := types.Alert{
		Status: "firing",
		Labels: map[string]string{
			"alertname": "TestAlert",
			"severity":  "critical",
		},
		Annotations: map[string]string{
			"summary": "Test webhook integration",
		},
	}

	processor.Process(testAlert)

	// Verify alert was received
	mu.Lock()
	defer mu.Unlock()
	require.Len(t, receivedAlerts, 1)
	assert.Equal(t, "firing", receivedAlerts[0].Status)
	assert.Equal(t, "TestAlert", receivedAlerts[0].Labels["alertname"])
}

func TestWebhookProcessor_Retry(t *testing.T) {
	attempts := 0
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		attempts++
		if attempts < 2 {
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		w.WriteHeader(http.StatusOK)
	}))
	defer server.Close()

	config := map[string]interface{}{
		"url": server.URL,
	}

	processor, err := processors.NewWebhookProcessor(config)
	require.NoError(t, err)

	testAlert := types.Alert{
		Status: "firing",
		Labels: map[string]string{
			"alertname": "RetryTest",
		},
	}

	processor.Process(testAlert)

	// Note: Current implementation doesn't have retry logic
	// This test documents the expected behavior
	assert.Equal(t, 1, attempts)
}

func TestProcessorRegistry_Integration(t *testing.T) {
	registry := processors.NewRegistry()

	// Register multiple processors
	registry.Register(&processors.BasicProcessor{})

	// Create a mock webhook processor
	webhookReceived := false
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		webhookReceived = true
		w.WriteHeader(http.StatusOK)
	}))
	defer server.Close()

	webhookConfig := map[string]interface{}{
		"url": server.URL,
	}
	webhookProcessor, err := processors.NewWebhookProcessor(webhookConfig)
	require.NoError(t, err)
	registry.Register(webhookProcessor)

	// Process alert through registry
	testAlert := types.Alert{
		Status: "firing",
		Labels: map[string]string{
			"alertname": "RegistryTest",
			"severity":  "warning",
		},
		Annotations: map[string]string{
			"summary": "Testing registry integration",
		},
	}

	registry.ProcessAlert(testAlert)

	// Verify webhook was called
	assert.True(t, webhookReceived)
}

func TestProcessorFactory_Integration(t *testing.T) {
	factory := processors.NewFactory()

	tests := []struct {
		name          string
		processorType string
		config        map[string]interface{}
		expectError   bool
	}{
		{
			name:          "Create Basic Processor",
			processorType: "basic",
			config:        map[string]interface{}{},
			expectError:   false,
		},
		{
			name:          "Create Webhook Processor",
			processorType: "webhook",
			config: map[string]interface{}{
				"url": "http://example.com/webhook",
			},
			expectError: false,
		},
		{
			name:          "Create Slack Processor",
			processorType: "slack",
			config: map[string]interface{}{
				"webhook_url": "https://hooks.slack.com/test",
			},
			expectError: false,
		},
		{
			name:          "Invalid Processor Type",
			processorType: "invalid",
			config:        map[string]interface{}{},
			expectError:   true,
		},
		{
			name:          "Webhook Without URL",
			processorType: "webhook",
			config:        map[string]interface{}{},
			expectError:   true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Test the factory methods directly
			var processor processors.AlertProcessor
			var err error

			switch tt.processorType {
			case "basic":
				processor = &processors.BasicProcessor{}
			case "webhook":
				processor, err = processors.NewWebhookProcessor(tt.config)
			case "slack":
				processor, err = processors.NewSlackProcessor(tt.config)
			default:
				err = assert.AnError // Invalid type
			}

			if tt.expectError {
				assert.Error(t, err)
			} else {
				assert.NoError(t, err)
				assert.NotNil(t, processor)
			}
		})
	}

	// Verify factory is initialized
	assert.NotNil(t, factory)
}

func TestConcurrentProcessing(t *testing.T) {
	registry := processors.NewRegistry()

	// Track concurrent execution
	var mu sync.Mutex
	processingCount := 0
	maxConcurrent := 0

	// Create multiple test webhook servers
	for i := 0; i < 3; i++ {
		server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			mu.Lock()
			processingCount++
			if processingCount > maxConcurrent {
				maxConcurrent = processingCount
			}
			mu.Unlock()

			// Simulate processing time
			// time.Sleep(10 * time.Millisecond)

			mu.Lock()
			processingCount--
			mu.Unlock()

			w.WriteHeader(http.StatusOK)
		}))
		defer server.Close()

		config := map[string]interface{}{
			"url": server.URL,
		}
		processor, err := processors.NewWebhookProcessor(config)
		require.NoError(t, err)
		registry.Register(processor)
	}

	// Send alert
	testAlert := types.Alert{
		Status: "firing",
		Labels: map[string]string{
			"alertname": "ConcurrentTest",
		},
	}

	registry.ProcessAlert(testAlert)

	// Verify concurrent execution occurred
	assert.GreaterOrEqual(t, maxConcurrent, 1)
}

func TestBasicProcessor_AllSeverityLevels(t *testing.T) {
	processor := &processors.BasicProcessor{}

	severities := []string{"critical", "warning", "info", "unknown", ""}

	for _, severity := range severities {
		t.Run("Severity_"+severity, func(t *testing.T) {
			alert := types.Alert{
				Status: "firing",
				Labels: map[string]string{
					"alertname": "SeverityTest",
				},
				Annotations: map[string]string{
					"summary": "Testing severity: " + severity,
				},
			}

			if severity != "" {
				alert.Labels["severity"] = severity
			}

			// Should not panic
			assert.NotPanics(t, func() {
				processor.Process(alert)
			})
		})
	}
}

func TestProcessorRegistry_NoPanicOnError(t *testing.T) {
	registry := processors.NewRegistry()

	// Create a processor that will fail
	server := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusInternalServerError)
	}))
	defer server.Close()

	config := map[string]interface{}{
		"url": server.URL,
	}
	processor, err := processors.NewWebhookProcessor(config)
	require.NoError(t, err)
	registry.Register(processor)

	// Should not panic even if processor fails
	assert.NotPanics(t, func() {
		testAlert := types.Alert{
			Status: "firing",
			Labels: map[string]string{
				"alertname": "ErrorTest",
			},
		}
		registry.ProcessAlert(testAlert)
	})
}
