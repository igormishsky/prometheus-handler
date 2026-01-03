package e2e

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"os/exec"
	"testing"
	"time"

	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

const (
	serverURL        = "http://localhost:18080"
	metricsURL       = "http://localhost:12112"
	serverStartDelay = 2 * time.Second
	requestTimeout   = 5 * time.Second
)

var (
	serverCmd *exec.Cmd
	serverCtx context.Context
	cancelFn  context.CancelFunc
)

// TestMain sets up and tears down the test environment
func TestMain(m *testing.M) {
	// Build the application
	fmt.Println("Building application...")
	buildCmd := exec.Command("go", "build", "-o", "../prometheus-alerts-handler-test", "..")
	buildCmd.Dir = "../"
	if output, err := buildCmd.CombinedOutput(); err != nil {
		fmt.Printf("Failed to build application: %v\n%s\n", err, output)
		os.Exit(1)
	}

	// Create test config
	testConfig := `
server:
  port: 18080
  metrics_port: 12112
  log_level: debug

processors:
  - type: basic
    enabled: true
    name: test-basic
    config: {}
`
	if err := os.WriteFile("../test-config.yaml", []byte(testConfig), 0644); err != nil {
		fmt.Printf("Failed to write test config: %v\n", err)
		os.Exit(1)
	}

	// Start the server
	fmt.Println("Starting server...")
	serverCtx, cancelFn = context.WithCancel(context.Background())
	serverCmd = exec.CommandContext(serverCtx, "../prometheus-alerts-handler-test")
	serverCmd.Env = append(os.Environ(), "CONFIG_PATH=../test-config.yaml")
	serverCmd.Stdout = os.Stdout
	serverCmd.Stderr = os.Stderr

	if err := serverCmd.Start(); err != nil {
		fmt.Printf("Failed to start server: %v\n", err)
		cleanup()
		os.Exit(1)
	}

	// Wait for server to start
	fmt.Println("Waiting for server to start...")
	time.Sleep(serverStartDelay)

	// Check if server is healthy
	if !waitForHealthy(30 * time.Second) {
		fmt.Println("Server failed to become healthy")
		cleanup()
		os.Exit(1)
	}

	fmt.Println("Server is ready, running tests...")

	// Run tests
	code := m.Run()

	// Cleanup
	cleanup()

	os.Exit(code)
}

func cleanup() {
	fmt.Println("Cleaning up...")
	if cancelFn != nil {
		cancelFn()
	}
	if serverCmd != nil && serverCmd.Process != nil {
		serverCmd.Process.Kill()
		serverCmd.Wait()
	}
	os.Remove("../prometheus-alerts-handler-test")
	os.Remove("../test-config.yaml")
}

func waitForHealthy(timeout time.Duration) bool {
	deadline := time.Now().Add(timeout)
	client := &http.Client{Timeout: 1 * time.Second}

	for time.Now().Before(deadline) {
		resp, err := client.Get(serverURL + "/health")
		if err == nil && resp.StatusCode == http.StatusOK {
			resp.Body.Close()
			return true
		}
		if resp != nil {
			resp.Body.Close()
		}
		time.Sleep(500 * time.Millisecond)
	}
	return false
}

func TestE2E_HealthEndpoint(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}

	resp, err := client.Get(serverURL + "/health")
	require.NoError(t, err)
	defer resp.Body.Close()

	assert.Equal(t, http.StatusOK, resp.StatusCode)

	var result map[string]string
	err = json.NewDecoder(resp.Body).Decode(&result)
	require.NoError(t, err)
	assert.Equal(t, "healthy", result["status"])
}

func TestE2E_RootEndpoint(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}

	resp, err := client.Get(serverURL + "/")
	require.NoError(t, err)
	defer resp.Body.Close()

	assert.Equal(t, http.StatusOK, resp.StatusCode)
	assert.Contains(t, resp.Header.Get("Content-Type"), "text/html")

	body, err := io.ReadAll(resp.Body)
	require.NoError(t, err)
	assert.Contains(t, string(body), "Prometheus Alerts Handler")
}

func TestE2E_MetricsEndpoint(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}

	resp, err := client.Get(metricsURL + "/metrics")
	require.NoError(t, err)
	defer resp.Body.Close()

	assert.Equal(t, http.StatusOK, resp.StatusCode)

	body, err := io.ReadAll(resp.Body)
	require.NoError(t, err)

	metrics := string(body)
	assert.Contains(t, metrics, "prometheus_alerts_handler_alerts_received_total")
	assert.Contains(t, metrics, "prometheus_alerts_handler_alerts_processed_total")
	assert.Contains(t, metrics, "prometheus_alerts_handler_processing_errors_total")
	assert.Contains(t, metrics, "prometheus_alerts_handler_processing_duration_seconds")
}

func TestE2E_AlertsEndpoint_SimpleFormat(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}

	alerts := []types.Alert{
		{
			Status: "firing",
			Labels: map[string]string{
				"alertname": "TestAlert",
				"severity":  "critical",
				"instance":  "server1",
			},
			Annotations: map[string]string{
				"summary":     "Test alert summary",
				"description": "Test alert description",
			},
		},
	}

	payload, err := json.Marshal(alerts)
	require.NoError(t, err)

	resp, err := client.Post(serverURL+"/alerts", "application/json", bytes.NewBuffer(payload))
	require.NoError(t, err)
	defer resp.Body.Close()

	assert.Equal(t, http.StatusOK, resp.StatusCode)

	var result map[string]interface{}
	err = json.NewDecoder(resp.Body).Decode(&result)
	require.NoError(t, err)
	assert.Equal(t, "success", result["status"])
	assert.Equal(t, "Alerts received and processed", result["message"])
	assert.Equal(t, float64(1), result["count"])
}

func TestE2E_AlertsEndpoint_AlertManagerFormat(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}

	alertMsg := types.AlertMessage{
		Version:  "4",
		GroupKey: "{}:{alertname=\"TestAlert\"}",
		Status:   "firing",
		Receiver: "test-receiver",
		GroupLabels: map[string]string{
			"alertname": "TestAlert",
		},
		CommonLabels: map[string]string{
			"severity": "warning",
		},
		Alerts: []types.Alert{
			{
				Status: "firing",
				Labels: map[string]string{
					"alertname": "HighMemoryUsage",
					"severity":  "warning",
					"instance":  "prod-server-1",
				},
				Annotations: map[string]string{
					"summary":     "High memory usage detected",
					"description": "Memory usage is above 90%",
				},
				StartsAt:     time.Now().Format(time.RFC3339),
				GeneratorURL: "http://prometheus:9090/graph",
			},
			{
				Status: "firing",
				Labels: map[string]string{
					"alertname": "HighCPUUsage",
					"severity":  "critical",
					"instance":  "prod-server-1",
				},
				Annotations: map[string]string{
					"summary":     "High CPU usage detected",
					"description": "CPU usage is above 95%",
				},
				StartsAt:     time.Now().Format(time.RFC3339),
				GeneratorURL: "http://prometheus:9090/graph",
			},
		},
	}

	payload, err := json.Marshal(alertMsg)
	require.NoError(t, err)

	resp, err := client.Post(serverURL+"/alerts", "application/json", bytes.NewBuffer(payload))
	require.NoError(t, err)
	defer resp.Body.Close()

	assert.Equal(t, http.StatusOK, resp.StatusCode)

	var result map[string]interface{}
	err = json.NewDecoder(resp.Body).Decode(&result)
	require.NoError(t, err)
	assert.Equal(t, "success", result["status"])
	assert.Equal(t, float64(2), result["count"])
}

func TestE2E_AlertsEndpoint_ErrorCases(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}

	tests := []struct {
		name           string
		method         string
		body           string
		expectedStatus int
	}{
		{
			name:           "Invalid JSON",
			method:         "POST",
			body:           "invalid json",
			expectedStatus: http.StatusBadRequest,
		},
		{
			name:           "Empty array",
			method:         "POST",
			body:           "[]",
			expectedStatus: http.StatusBadRequest,
		},
		{
			name:           "Wrong HTTP method",
			method:         "GET",
			body:           "",
			expectedStatus: http.StatusMethodNotAllowed,
		},
		{
			name:           "PUT not allowed",
			method:         "PUT",
			body:           "{}",
			expectedStatus: http.StatusMethodNotAllowed,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			var req *http.Request
			var err error

			if tt.body != "" {
				req, err = http.NewRequest(tt.method, serverURL+"/alerts", bytes.NewBufferString(tt.body))
			} else {
				req, err = http.NewRequest(tt.method, serverURL+"/alerts", nil)
			}
			require.NoError(t, err)

			resp, err := client.Do(req)
			require.NoError(t, err)
			defer resp.Body.Close()

			assert.Equal(t, tt.expectedStatus, resp.StatusCode)
		})
	}
}

func TestE2E_MetricsIncrement(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}

	// Get initial metrics
	resp, err := client.Get(metricsURL + "/metrics")
	require.NoError(t, err)
	initialBody, _ := io.ReadAll(resp.Body)
	resp.Body.Close()

	// Send an alert
	alerts := []types.Alert{
		{
			Status: "firing",
			Labels: map[string]string{
				"alertname": "MetricsTest",
				"severity":  "info",
			},
			Annotations: map[string]string{
				"summary": "Testing metrics increment",
			},
		},
	}

	payload, err := json.Marshal(alerts)
	require.NoError(t, err)

	resp, err = client.Post(serverURL+"/alerts", "application/json", bytes.NewBuffer(payload))
	require.NoError(t, err)
	resp.Body.Close()

	// Wait a bit for metrics to update
	time.Sleep(100 * time.Millisecond)

	// Get updated metrics
	resp, err = client.Get(metricsURL + "/metrics")
	require.NoError(t, err)
	updatedBody, _ := io.ReadAll(resp.Body)
	resp.Body.Close()

	// Verify metrics changed
	assert.NotEqual(t, string(initialBody), string(updatedBody))
	assert.Contains(t, string(updatedBody), "prometheus_alerts_handler_alerts_received_total")
}

func TestE2E_ConcurrentRequests(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}
	concurrency := 10

	alerts := []types.Alert{
		{
			Status: "firing",
			Labels: map[string]string{
				"alertname": "ConcurrentTest",
				"severity":  "info",
			},
			Annotations: map[string]string{
				"summary": "Testing concurrent requests",
			},
		},
	}

	payload, err := json.Marshal(alerts)
	require.NoError(t, err)

	// Send concurrent requests
	errors := make(chan error, concurrency)
	for i := 0; i < concurrency; i++ {
		go func(id int) {
			resp, err := client.Post(serverURL+"/alerts", "application/json", bytes.NewBuffer(payload))
			if err != nil {
				errors <- fmt.Errorf("request %d failed: %w", id, err)
				return
			}
			defer resp.Body.Close()

			if resp.StatusCode != http.StatusOK {
				errors <- fmt.Errorf("request %d returned status %d", id, resp.StatusCode)
				return
			}
			errors <- nil
		}(i)
	}

	// Collect results
	for i := 0; i < concurrency; i++ {
		err := <-errors
		assert.NoError(t, err)
	}
}

func TestE2E_ResolvedAlerts(t *testing.T) {
	client := &http.Client{Timeout: requestTimeout}

	alerts := []types.Alert{
		{
			Status: "resolved",
			Labels: map[string]string{
				"alertname": "ResolvedTest",
				"severity":  "critical",
			},
			Annotations: map[string]string{
				"summary": "This alert has been resolved",
			},
			StartsAt: time.Now().Add(-1 * time.Hour).Format(time.RFC3339),
			EndsAt:   time.Now().Format(time.RFC3339),
		},
	}

	payload, err := json.Marshal(alerts)
	require.NoError(t, err)

	resp, err := client.Post(serverURL+"/alerts", "application/json", bytes.NewBuffer(payload))
	require.NoError(t, err)
	defer resp.Body.Close()

	assert.Equal(t, http.StatusOK, resp.StatusCode)

	var result map[string]interface{}
	err = json.NewDecoder(resp.Body).Decode(&result)
	require.NoError(t, err)
	assert.Equal(t, "success", result["status"])
}
