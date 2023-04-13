package handler_test

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/igormishsky/prometheus-alerts-handler/handler"
	"github.com/stretchr/testify/assert"
)

func TestAlertsHandler(t *testing.T) {
	sampleAlerts := []handler.Alert{
		{
			Status: "firing",
			Labels: map[string]string{
				"severity": "critical",
			},
			Annotations: map[string]string{
				"description": "Test description",
			},
		},
	}

	alertsBytes, _ := json.Marshal(sampleAlerts)

	req, err := http.NewRequest("POST", "/alerts", bytes.NewBuffer(alertsBytes))
	assert.NoError(t, err)

	rr := httptest.NewRecorder()
	handlerFunc := http.HandlerFunc(handler.AlertsHandler)

	handlerFunc.ServeHTTP(rr, req)

	assert.Equal(t, http.StatusOK, rr.Code)
	assert.Equal(t, "Alerts received", rr.Body.String())
}
