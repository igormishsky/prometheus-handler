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

// PagerDutyProcessor sends alerts to PagerDuty
type PagerDutyProcessor struct {
	IntegrationKey string
	APIEndpoint    string
	client         *http.Client
}

// PagerDutyEvent represents a PagerDuty Events API v2 event
type PagerDutyEvent struct {
	RoutingKey  string                 `json:"routing_key"`
	EventAction string                 `json:"event_action"`
	DedupKey    string                 `json:"dedup_key,omitempty"`
	Payload     PagerDutyPayload       `json:"payload"`
	Links       []PagerDutyLink        `json:"links,omitempty"`
	Images      []PagerDutyImage       `json:"images,omitempty"`
}

// PagerDutyPayload represents the payload of a PagerDuty event
type PagerDutyPayload struct {
	Summary       string                 `json:"summary"`
	Source        string                 `json:"source"`
	Severity      string                 `json:"severity"`
	Timestamp     string                 `json:"timestamp,omitempty"`
	Component     string                 `json:"component,omitempty"`
	Group         string                 `json:"group,omitempty"`
	Class         string                 `json:"class,omitempty"`
	CustomDetails map[string]interface{} `json:"custom_details,omitempty"`
}

// PagerDutyLink represents a link in a PagerDuty event
type PagerDutyLink struct {
	Href string `json:"href"`
	Text string `json:"text,omitempty"`
}

// PagerDutyImage represents an image in a PagerDuty event
type PagerDutyImage struct {
	Src  string `json:"src"`
	Href string `json:"href,omitempty"`
	Alt  string `json:"alt,omitempty"`
}

// NewPagerDutyProcessor creates a new PagerDuty processor
func NewPagerDutyProcessor(cfg map[string]interface{}) (*PagerDutyProcessor, error) {
	integrationKey, ok := cfg["integration_key"].(string)
	if !ok || integrationKey == "" {
		return nil, fmt.Errorf("pagerduty integration_key is required")
	}

	apiEndpoint := getStringFromConfig(cfg, "api_endpoint", "https://events.pagerduty.com/v2/enqueue")

	processor := &PagerDutyProcessor{
		IntegrationKey: integrationKey,
		APIEndpoint:    apiEndpoint,
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}

	return processor, nil
}

// Process processes an alert and sends it to PagerDuty
func (pdp *PagerDutyProcessor) Process(alert types.Alert) {
	logrus.WithFields(logrus.Fields{
		"processor": "pagerduty",
		"status":    alert.Status,
	}).Info("Processing alert for PagerDuty")

	event := pdp.buildPagerDutyEvent(alert)

	if err := pdp.sendToPagerDuty(event); err != nil {
		logrus.WithError(err).Error("Failed to send alert to PagerDuty")
		metrics.AlertsProcessingErrors.Inc()
		return
	}

	metrics.AlertsProcessed.Inc()
	logrus.Info("Alert sent to PagerDuty successfully")
}

// buildPagerDutyEvent builds a PagerDuty event from an alert
func (pdp *PagerDutyProcessor) buildPagerDutyEvent(alert types.Alert) PagerDutyEvent {
	// Determine event action based on alert status
	eventAction := "trigger"
	if alert.Status == "resolved" {
		eventAction = "resolve"
	}

	// Build summary
	summary := alert.Annotations["summary"]
	if summary == "" {
		summary = alert.Annotations["description"]
	}
	if summary == "" {
		summary = fmt.Sprintf("Alert: %s", alert.Labels["alertname"])
	}

	// Map Prometheus severity to PagerDuty severity
	severity := pdp.mapSeverity(alert.Labels["severity"])

	// Build custom details
	customDetails := make(map[string]interface{})
	for k, v := range alert.Labels {
		customDetails[k] = v
	}
	for k, v := range alert.Annotations {
		customDetails["annotation_"+k] = v
	}

	// Build payload
	payload := PagerDutyPayload{
		Summary:       summary,
		Source:        alert.Labels["instance"],
		Severity:      severity,
		Timestamp:     time.Now().Format(time.RFC3339),
		CustomDetails: customDetails,
	}

	// Build dedup key (fingerprint or generated from labels)
	dedupKey := alert.Fingerprint
	if dedupKey == "" {
		dedupKey = fmt.Sprintf("%s-%s", alert.Labels["alertname"], alert.Labels["instance"])
	}

	event := PagerDutyEvent{
		RoutingKey:  pdp.IntegrationKey,
		EventAction: eventAction,
		DedupKey:    dedupKey,
		Payload:     payload,
	}

	// Add generator URL as a link if available
	if alert.GeneratorURL != "" {
		event.Links = []PagerDutyLink{
			{
				Href: alert.GeneratorURL,
				Text: "View in Prometheus",
			},
		}
	}

	return event
}

// sendToPagerDuty sends an event to PagerDuty
func (pdp *PagerDutyProcessor) sendToPagerDuty(event PagerDutyEvent) error {
	payload, err := json.Marshal(event)
	if err != nil {
		return fmt.Errorf("failed to marshal PagerDuty event: %w", err)
	}

	resp, err := pdp.client.Post(pdp.APIEndpoint, "application/json", bytes.NewBuffer(payload))
	if err != nil {
		return fmt.Errorf("failed to send request to PagerDuty: %w", err)
	}
	defer func() { _ = resp.Body.Close() }()

	if resp.StatusCode != http.StatusAccepted {
		return fmt.Errorf("pagerduty returned non-202 status: %d", resp.StatusCode)
	}

	return nil
}

// mapSeverity maps Prometheus severity levels to PagerDuty severity levels
func (pdp *PagerDutyProcessor) mapSeverity(severity string) string {
	switch severity {
	case "critical":
		return "critical"
	case "warning":
		return "warning"
	case "info":
		return "info"
	default:
		return "error"
	}
}
