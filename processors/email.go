package processors

import (
	"bytes"
	"fmt"
	"html/template"
	"net/smtp"

	"github.com/igormishsky/prometheus-alerts-handler/metrics"
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/sirupsen/logrus"
)

// EmailProcessor sends alerts via email
type EmailProcessor struct {
	SMTPHost     string
	SMTPPort     int
	SMTPUser     string
	SMTPPassword string
	From         string
	To           []string
	Subject      string
}

// NewEmailProcessor creates a new email processor
func NewEmailProcessor(cfg map[string]interface{}) (*EmailProcessor, error) {
	smtpHost, ok := cfg["smtp_host"].(string)
	if !ok || smtpHost == "" {
		return nil, fmt.Errorf("smtp_host is required")
	}

	smtpPort := 587
	if port, ok := cfg["smtp_port"].(int); ok {
		smtpPort = port
	}

	smtpUser, ok := cfg["smtp_user"].(string)
	if !ok || smtpUser == "" {
		return nil, fmt.Errorf("smtp_user is required")
	}

	smtpPassword, ok := cfg["smtp_password"].(string)
	if !ok || smtpPassword == "" {
		return nil, fmt.Errorf("smtp_password is required")
	}

	from, ok := cfg["from"].(string)
	if !ok || from == "" {
		return nil, fmt.Errorf("from email is required")
	}

	var to []string
	if toInterface, ok := cfg["to"].([]interface{}); ok {
		for _, t := range toInterface {
			if email, ok := t.(string); ok {
				to = append(to, email)
			}
		}
	}

	if len(to) == 0 {
		return nil, fmt.Errorf("at least one recipient email is required")
	}

	processor := &EmailProcessor{
		SMTPHost:     smtpHost,
		SMTPPort:     smtpPort,
		SMTPUser:     smtpUser,
		SMTPPassword: smtpPassword,
		From:         from,
		To:           to,
		Subject:      getStringFromConfig(cfg, "subject", "Prometheus Alert Notification"),
	}

	return processor, nil
}

// Process processes an alert and sends it via email
func (ep *EmailProcessor) Process(alert types.Alert) {
	logrus.WithFields(logrus.Fields{
		"processor": "email",
		"status":    alert.Status,
	}).Info("Processing alert for email")

	if err := ep.sendEmail(alert); err != nil {
		logrus.WithError(err).Error("Failed to send alert email")
		metrics.AlertsProcessingErrors.Inc()
		return
	}

	metrics.AlertsProcessed.Inc()
	logrus.Info("Alert email sent successfully")
}

// sendEmail sends an alert via email
func (ep *EmailProcessor) sendEmail(alert types.Alert) error {
	subject := ep.buildSubject(alert)
	body := ep.buildEmailBody(alert)

	msg := ep.buildMIMEMessage(subject, body)

	auth := smtp.PlainAuth("", ep.SMTPUser, ep.SMTPPassword, ep.SMTPHost)
	addr := fmt.Sprintf("%s:%d", ep.SMTPHost, ep.SMTPPort)

	if err := smtp.SendMail(addr, auth, ep.From, ep.To, []byte(msg)); err != nil {
		return fmt.Errorf("failed to send email: %w", err)
	}

	return nil
}

// buildSubject builds the email subject
func (ep *EmailProcessor) buildSubject(alert types.Alert) string {
	alertName := alert.Labels["alertname"]
	severity := alert.Labels["severity"]

	if alertName != "" {
		return fmt.Sprintf("[%s] %s - %s", severity, alert.Status, alertName)
	}

	return fmt.Sprintf("[%s] Prometheus Alert - %s", severity, alert.Status)
}

// buildEmailBody builds the email body
func (ep *EmailProcessor) buildEmailBody(alert types.Alert) string {
	tmpl := `
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; }
        .alert { padding: 20px; border-radius: 5px; margin: 10px 0; }
        .alert.firing { background-color: #fee; border-left: 4px solid #d00; }
        .alert.resolved { background-color: #efe; border-left: 4px solid #0d0; }
        .label { display: inline-block; margin: 5px; padding: 5px 10px; background-color: #e0e0e0; border-radius: 3px; }
        h2 { margin-top: 0; }
        table { border-collapse: collapse; width: 100%; }
        th, td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="alert {{.Status}}">
        <h2>Prometheus Alert: {{.Labels.alertname}}</h2>
        <p><strong>Status:</strong> {{.Status}}</p>
        <p><strong>Severity:</strong> {{.Labels.severity}}</p>

        {{if .Annotations.description}}
        <p><strong>Description:</strong> {{.Annotations.description}}</p>
        {{end}}

        {{if .Annotations.summary}}
        <p><strong>Summary:</strong> {{.Annotations.summary}}</p>
        {{end}}

        <h3>Labels</h3>
        <table>
            <tr><th>Label</th><th>Value</th></tr>
            {{range $key, $value := .Labels}}
            <tr><td>{{$key}}</td><td>{{$value}}</td></tr>
            {{end}}
        </table>

        {{if .Annotations}}
        <h3>Annotations</h3>
        <table>
            <tr><th>Annotation</th><th>Value</th></tr>
            {{range $key, $value := .Annotations}}
            <tr><td>{{$key}}</td><td>{{$value}}</td></tr>
            {{end}}
        </table>
        {{end}}
    </div>
</body>
</html>
`

	t := template.Must(template.New("email").Parse(tmpl))
	var buf bytes.Buffer
	if err := t.Execute(&buf, alert); err != nil {
		logrus.WithError(err).Error("Failed to build email template")
		return "Failed to build email body"
	}

	return buf.String()
}

// buildMIMEMessage builds a MIME email message
func (ep *EmailProcessor) buildMIMEMessage(subject, body string) string {
	var msg bytes.Buffer

	msg.WriteString(fmt.Sprintf("From: %s\r\n", ep.From))
	msg.WriteString(fmt.Sprintf("To: %s\r\n", ep.To[0]))
	msg.WriteString(fmt.Sprintf("Subject: %s\r\n", subject))
	msg.WriteString("MIME-Version: 1.0\r\n")
	msg.WriteString("Content-Type: text/html; charset=UTF-8\r\n")
	msg.WriteString("\r\n")
	msg.WriteString(body)

	return msg.String()
}
