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

// SlackProcessor sends alerts to Slack
type SlackProcessor struct {
	WebhookURL string
	Channel    string
	Username   string
	IconEmoji  string
	client     *http.Client
}

// SlackMessage represents a Slack message payload
type SlackMessage struct {
	Text        string            `json:"text,omitempty"`
	Channel     string            `json:"channel,omitempty"`
	Username    string            `json:"username,omitempty"`
	IconEmoji   string            `json:"icon_emoji,omitempty"`
	Attachments []SlackAttachment `json:"attachments,omitempty"`
}

// SlackAttachment represents a Slack message attachment
type SlackAttachment struct {
	Color      string                 `json:"color,omitempty"`
	Title      string                 `json:"title,omitempty"`
	Text       string                 `json:"text,omitempty"`
	Fields     []SlackAttachmentField `json:"fields,omitempty"`
	Footer     string                 `json:"footer,omitempty"`
	FooterIcon string                 `json:"footer_icon,omitempty"`
	Timestamp  int64                  `json:"ts,omitempty"`
}

// SlackAttachmentField represents a field in a Slack attachment
type SlackAttachmentField struct {
	Title string `json:"title"`
	Value string `json:"value"`
	Short bool   `json:"short"`
}

// NewSlackProcessor creates a new Slack processor
func NewSlackProcessor(cfg map[string]interface{}) (*SlackProcessor, error) {
	webhookURL, ok := cfg["webhook_url"].(string)
	if !ok || webhookURL == "" {
		return nil, fmt.Errorf("slack webhook_url is required")
	}

	processor := &SlackProcessor{
		WebhookURL: webhookURL,
		Channel:    getStringFromConfig(cfg, "channel", ""),
		Username:   getStringFromConfig(cfg, "username", "Prometheus Alerts"),
		IconEmoji:  getStringFromConfig(cfg, "icon_emoji", ":fire:"),
		client: &http.Client{
			Timeout: 10 * time.Second,
		},
	}

	return processor, nil
}

// Process processes an alert and sends it to Slack
func (sp *SlackProcessor) Process(alert types.Alert) {
	logrus.WithFields(logrus.Fields{
		"processor": "slack",
		"status":    alert.Status,
	}).Info("Processing alert for Slack")

	message := sp.buildSlackMessage(alert)

	if err := sp.sendToSlack(message); err != nil {
		logrus.WithError(err).Error("Failed to send alert to Slack")
		metrics.AlertsProcessingErrors.Inc()
		return
	}

	metrics.AlertsProcessed.Inc()
	logrus.Info("Alert sent to Slack successfully")
}

// buildSlackMessage builds a Slack message from an alert
func (sp *SlackProcessor) buildSlackMessage(alert types.Alert) SlackMessage {
	color := sp.getColorForStatus(alert.Status, alert.Labels["severity"])

	title := fmt.Sprintf("Alert: %s", alert.Labels["alertname"])
	if title == "Alert: " {
		title = "Prometheus Alert"
	}

	fields := []SlackAttachmentField{}

	// Add status
	fields = append(fields, SlackAttachmentField{
		Title: "Status",
		Value: alert.Status,
		Short: true,
	})

	// Add severity if present
	if severity, ok := alert.Labels["severity"]; ok {
		fields = append(fields, SlackAttachmentField{
			Title: "Severity",
			Value: severity,
			Short: true,
		})
	}

	// Add instance if present
	if instance, ok := alert.Labels["instance"]; ok {
		fields = append(fields, SlackAttachmentField{
			Title: "Instance",
			Value: instance,
			Short: true,
		})
	}

	// Add description from annotations
	description := alert.Annotations["description"]
	if description == "" {
		description = alert.Annotations["summary"]
	}

	attachment := SlackAttachment{
		Color:      color,
		Title:      title,
		Text:       description,
		Fields:     fields,
		Footer:     "Prometheus Alerts Handler",
		FooterIcon: "https://prometheus.io/assets/favicons/android-chrome-192x192.png",
		Timestamp:  time.Now().Unix(),
	}

	return SlackMessage{
		Channel:     sp.Channel,
		Username:    sp.Username,
		IconEmoji:   sp.IconEmoji,
		Attachments: []SlackAttachment{attachment},
	}
}

// sendToSlack sends a message to Slack
func (sp *SlackProcessor) sendToSlack(message SlackMessage) error {
	payload, err := json.Marshal(message)
	if err != nil {
		return fmt.Errorf("failed to marshal Slack message: %w", err)
	}

	resp, err := sp.client.Post(sp.WebhookURL, "application/json", bytes.NewBuffer(payload))
	if err != nil {
		return fmt.Errorf("failed to send request to Slack: %w", err)
	}
	defer func() { _ = resp.Body.Close() }()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("slack returned non-OK status: %d", resp.StatusCode)
	}

	return nil
}

// getColorForStatus returns a color based on alert status and severity
func (sp *SlackProcessor) getColorForStatus(status, severity string) string {
	if status == "resolved" {
		return "good" // green
	}

	switch severity {
	case "critical":
		return "danger" // red
	case "warning":
		return "warning" // yellow
	default:
		return "#439FE0" // blue
	}
}

// Helper function to get string from config map
func getStringFromConfig(cfg map[string]interface{}, key, defaultValue string) string {
	if val, ok := cfg[key].(string); ok {
		return val
	}
	return defaultValue
}
