package processors

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/igormishsky/prometheus-alerts-handler/metrics"
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/sirupsen/logrus"
)

// WebhookProcessor sends alerts to a generic webhook
type WebhookProcessor struct {
	URL     string
	Method  string
	Headers map[string]string
	client  *http.Client
}

// NewWebhookProcessor creates a new webhook processor
func NewWebhookProcessor(cfg map[string]interface{}) (*WebhookProcessor, error) {
	url, ok := cfg["url"].(string)
	if !ok || url == "" {
		return nil, fmt.Errorf("webhook url is required")
	}

	method := getStringFromConfig(cfg, "method", "POST")

	headers := make(map[string]string)
	if headersCfg, ok := cfg["headers"].(map[string]interface{}); ok {
		for k, v := range headersCfg {
			if str, ok := v.(string); ok {
				headers[k] = str
			}
		}
	}

	processor := &WebhookProcessor{
		URL:     url,
		Method:  method,
		Headers: headers,
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}

	return processor, nil
}

// Process processes an alert and sends it to the webhook
func (wp *WebhookProcessor) Process(alert types.Alert) {
	logrus.WithFields(logrus.Fields{
		"processor": "webhook",
		"url":       wp.URL,
		"status":    alert.Status,
	}).Info("Processing alert for webhook")

	if err := wp.sendToWebhook(alert); err != nil {
		logrus.WithError(err).Error("Failed to send alert to webhook")
		metrics.AlertsProcessingErrors.Inc()
		return
	}

	metrics.AlertsProcessed.Inc()
	logrus.Info("Alert sent to webhook successfully")
}

// sendToWebhook sends an alert to the webhook
func (wp *WebhookProcessor) sendToWebhook(alert types.Alert) error {
	payload, err := json.Marshal(alert)
	if err != nil {
		return fmt.Errorf("failed to marshal alert: %w", err)
	}

	req, err := http.NewRequest(wp.Method, wp.URL, bytes.NewBuffer(payload))
	if err != nil {
		return fmt.Errorf("failed to create request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	for key, value := range wp.Headers {
		req.Header.Set(key, value)
	}

	resp, err := wp.client.Do(req)
	if err != nil {
		return fmt.Errorf("failed to send request to webhook: %w", err)
	}
	defer func() { _ = resp.Body.Close() }()

	if resp.StatusCode < 200 || resp.StatusCode >= 300 {
		return fmt.Errorf("webhook returned non-2xx status: %d", resp.StatusCode)
	}

	return nil
}
