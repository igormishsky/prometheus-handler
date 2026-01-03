package handler

import (
	"encoding/json"
	"time"

	"github.com/igormishsky/prometheus-alerts-handler/metrics"
	"github.com/igormishsky/prometheus-alerts-handler/processors"
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/sirupsen/logrus"
	"io/ioutil"
	"net/http"
)

// Handler manages alert processing
type Handler struct {
	registry *processors.Registry
}

// NewHandler creates a new alert handler
func NewHandler(registry *processors.Registry) *Handler {
	return &Handler{
		registry: registry,
	}
}

// ServeHTTP handles incoming alert webhooks
func (h *Handler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	startTime := time.Now()

	// Only accept POST requests
	if r.Method != http.MethodPost {
		respondWithError(w, http.StatusMethodNotAllowed, "Only POST method is allowed")
		return
	}

	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		logrus.WithError(err).Error("Error reading request body")
		respondWithError(w, http.StatusBadRequest, "Error reading request body")
		return
	}

	// Try to unmarshal as AlertMessage first (Alertmanager format)
	var alertMsg types.AlertMessage
	if err := json.Unmarshal(body, &alertMsg); err == nil && len(alertMsg.Alerts) > 0 {
		logrus.WithFields(logrus.Fields{
			"num_alerts": len(alertMsg.Alerts),
			"status":     alertMsg.Status,
			"receiver":   alertMsg.Receiver,
		}).Info("Received AlertManager webhook")

		metrics.AlertsReceived.Add(float64(len(alertMsg.Alerts)))

		for _, alert := range alertMsg.Alerts {
			h.registry.ProcessAlert(alert)
		}

		duration := time.Since(startTime).Seconds()
		metrics.ProcessingDuration.Observe(duration)

		w.WriteHeader(http.StatusOK)
		respondWithJSON(w, http.StatusOK, map[string]interface{}{
			"status":  "success",
			"message": "Alerts received and processed",
			"count":   len(alertMsg.Alerts),
		})
		return
	}

	// Try to unmarshal as array of alerts (simple format)
	var alerts []types.Alert
	if err := json.Unmarshal(body, &alerts); err != nil {
		logrus.WithError(err).Error("Error unmarshalling request body")
		respondWithError(w, http.StatusBadRequest, "Invalid JSON format")
		return
	}

	if len(alerts) == 0 {
		respondWithError(w, http.StatusBadRequest, "No alerts in request")
		return
	}

	logrus.WithField("num_alerts", len(alerts)).Info("Received alerts")
	metrics.AlertsReceived.Add(float64(len(alerts)))

	for _, alert := range alerts {
		h.registry.ProcessAlert(alert)
	}

	duration := time.Since(startTime).Seconds()
	metrics.ProcessingDuration.Observe(duration)

	respondWithJSON(w, http.StatusOK, map[string]interface{}{
		"status":  "success",
		"message": "Alerts received and processed",
		"count":   len(alerts),
	})
}

func respondWithError(w http.ResponseWriter, statusCode int, message string) {
	respondWithJSON(w, statusCode, map[string]string{
		"status": "error",
		"error":  message,
	})
}

func respondWithJSON(w http.ResponseWriter, statusCode int, payload interface{}) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	json.NewEncoder(w).Encode(payload)
}
