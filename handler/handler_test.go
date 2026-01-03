package handler_test

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/igormishsky/prometheus-alerts-handler/handler"
	"github.com/igormishsky/prometheus-alerts-handler/processors"
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/stretchr/testify/assert"
)

func TestAlertsHandler(t *testing.T) {
	sampleAlerts := []types.Alert{
		{
			Status: "firing",
			Labels: map[string]string{
				"severity":  "critical",
				"alertname": "TestAlert",
			},
			Annotations: map[string]string{
				"description": "Test description",
			},
		},
	}

	alertsBytes, _ := json.Marshal(sampleAlerts)

	// Create registry with basic processor
	registry := processors.NewRegistry()
	registry.Register(&processors.BasicProcessor{})

	// Create handler
	h := handler.NewHandler(registry)

	req, err := http.NewRequest("POST", "/alerts", bytes.NewBuffer(alertsBytes))
	assert.NoError(t, err)

	rr := httptest.NewRecorder()
	h.ServeHTTP(rr, req)

	assert.Equal(t, http.StatusOK, rr.Code)

	var response map[string]interface{}
	err = json.Unmarshal(rr.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, "success", response["status"])
	assert.Equal(t, float64(1), response["count"])
}

func TestAlertsHandler_InvalidJSON(t *testing.T) {
	registry := processors.NewRegistry()
	registry.Register(&processors.BasicProcessor{})
	h := handler.NewHandler(registry)

	req, err := http.NewRequest("POST", "/alerts", bytes.NewBufferString("invalid json"))
	assert.NoError(t, err)

	rr := httptest.NewRecorder()
	h.ServeHTTP(rr, req)

	assert.Equal(t, http.StatusBadRequest, rr.Code)

	var response map[string]interface{}
	err = json.Unmarshal(rr.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, "error", response["status"])
}

func TestAlertsHandler_MethodNotAllowed(t *testing.T) {
	registry := processors.NewRegistry()
	h := handler.NewHandler(registry)

	req, err := http.NewRequest("GET", "/alerts", nil)
	assert.NoError(t, err)

	rr := httptest.NewRecorder()
	h.ServeHTTP(rr, req)

	assert.Equal(t, http.StatusMethodNotAllowed, rr.Code)
}

func TestAlertsHandler_EmptyAlerts(t *testing.T) {
	registry := processors.NewRegistry()
	h := handler.NewHandler(registry)

	alertsBytes, _ := json.Marshal([]types.Alert{})
	req, err := http.NewRequest("POST", "/alerts", bytes.NewBuffer(alertsBytes))
	assert.NoError(t, err)

	rr := httptest.NewRecorder()
	h.ServeHTTP(rr, req)

	assert.Equal(t, http.StatusBadRequest, rr.Code)
}

func TestAlertsHandler_AlertManagerFormat(t *testing.T) {
	registry := processors.NewRegistry()
	registry.Register(&processors.BasicProcessor{})
	h := handler.NewHandler(registry)

	alertMsg := types.AlertMessage{
		Version:  "4",
		GroupKey: "test",
		Status:   "firing",
		Receiver: "test",
		Alerts: []types.Alert{
			{
				Status: "firing",
				Labels: map[string]string{
					"severity":  "critical",
					"alertname": "TestAlert",
				},
				Annotations: map[string]string{
					"summary": "Test summary",
				},
			},
		},
	}

	alertBytes, _ := json.Marshal(alertMsg)
	req, err := http.NewRequest("POST", "/alerts", bytes.NewBuffer(alertBytes))
	assert.NoError(t, err)

	rr := httptest.NewRecorder()
	h.ServeHTTP(rr, req)

	assert.Equal(t, http.StatusOK, rr.Code)

	var response map[string]interface{}
	err = json.Unmarshal(rr.Body.Bytes(), &response)
	assert.NoError(t, err)
	assert.Equal(t, "success", response["status"])
}
