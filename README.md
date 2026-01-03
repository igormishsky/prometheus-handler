# Prometheus Alerts Handler

A flexible, production-ready alert routing system for Prometheus Alertmanager. Route alerts to multiple destinations including Slack, Email, PagerDuty, and custom webhooks.

## Features

- **Multiple Connectors**: Slack, Email, PagerDuty, and generic webhooks
- **Concurrent Processing**: Alerts sent to all configured processors simultaneously
- **Flexible Configuration**: YAML-based configuration with environment variable support
- **Production Ready**: Graceful shutdown, health checks, and comprehensive metrics
- **Extensible**: Easy to add custom processors
- **Native Prometheus Integration**: Direct webhook receiver for Alertmanager

## Quick Start

### Installation

```bash
# Clone the repository
git clone https://github.com/igormishsky/prometheus-handler.git
cd prometheus-handler

# Build the application
go build -o prometheus-alerts-handler

# Run with default configuration
./prometheus-alerts-handler
```

### Docker

```bash
# Build Docker image
docker build -t prometheus-alerts-handler .

# Run with configuration
docker run -v $(pwd)/config.yaml:/app/config.yaml -p 8080:8080 -p 2112:2112 prometheus-alerts-handler
```

## Configuration

Create a `config.yaml` file to configure the handler:

```yaml
server:
  port: 8080
  metrics_port: 2112
  log_level: info

processors:
  - type: slack
    enabled: true
    name: slack-alerts
    config:
      webhook_url: "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
      channel: "#alerts"
      username: "Prometheus"
      icon_emoji: ":fire:"

  - type: email
    enabled: true
    name: email-team
    config:
      smtp_host: "smtp.gmail.com"
      smtp_port: 587
      smtp_user: "alerts@example.com"
      smtp_password: "your-app-password"
      from: "prometheus@example.com"
      to:
        - "oncall@example.com"
```

See the [examples](./examples) directory for more configuration examples.

## Supported Processors

### Slack

Send alerts to Slack channels with rich formatting and color coding.

```yaml
- type: slack
  enabled: true
  name: slack-critical
  config:
    webhook_url: "https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
    channel: "#alerts"          # Optional
    username: "Prometheus"      # Optional
    icon_emoji: ":fire:"        # Optional
```

### Email

Send HTML-formatted alert emails via SMTP.

```yaml
- type: email
  enabled: true
  name: email-alerts
  config:
    smtp_host: "smtp.gmail.com"
    smtp_port: 587
    smtp_user: "your-email@gmail.com"
    smtp_password: "your-app-password"
    from: "alerts@example.com"
    to:
      - "oncall@example.com"
      - "team@example.com"
    subject: "Prometheus Alert"  # Optional
```

### PagerDuty

Create incidents in PagerDuty with proper severity mapping.

```yaml
- type: pagerduty
  enabled: true
  name: pagerduty-oncall
  config:
    integration_key: "YOUR_INTEGRATION_KEY"
    api_endpoint: "https://events.pagerduty.com/v2/enqueue"  # Optional
```

### Webhook

Send alerts to generic HTTP webhooks.

```yaml
- type: webhook
  enabled: true
  name: custom-webhook
  config:
    url: "https://your-webhook.com/alerts"
    method: "POST"              # Optional, default: POST
    headers:                    # Optional
      Authorization: "Bearer TOKEN"
      X-Custom-Header: "value"
```

### Basic

Simple processor that logs alerts (useful for debugging).

```yaml
- type: basic
  enabled: true
  name: logger
  config: {}
```

## Prometheus Alertmanager Integration

Configure Alertmanager to send webhooks to this handler:

```yaml
# alertmanager.yml
route:
  receiver: 'prometheus-alerts-handler'

receivers:
  - name: 'prometheus-alerts-handler'
    webhook_configs:
      - url: 'http://localhost:8080/alerts'
        send_resolved: true
```

## API Endpoints

### POST /alerts

Receive alerts from Prometheus Alertmanager.

**Request body:**
```json
{
  "version": "4",
  "groupKey": "{}:{alertname=\"ExampleAlert\"}",
  "status": "firing",
  "receiver": "webhook",
  "alerts": [
    {
      "status": "firing",
      "labels": {
        "alertname": "ExampleAlert",
        "severity": "critical",
        "instance": "server1"
      },
      "annotations": {
        "summary": "Example alert summary",
        "description": "Detailed description"
      }
    }
  ]
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Alerts received and processed",
  "count": 1
}
```

### GET /health

Health check endpoint.

```json
{"status": "healthy"}
```

### GET /metrics

Prometheus metrics endpoint (available on metrics port).

## Metrics

The handler exposes the following Prometheus metrics on the metrics port (default: 2112):

- `prometheus_alerts_handler_alerts_received_total` - Total number of alerts received
- `prometheus_alerts_handler_alerts_processed_total` - Total number of alerts successfully processed
- `prometheus_alerts_handler_processing_errors_total` - Total number of processing errors
- `prometheus_alerts_handler_processing_duration_seconds` - Alert processing duration histogram

## Environment Variables

- `CONFIG_PATH` - Path to configuration file (default: `config.yaml`)
- `SERVER_PORT` - Override server port
- `METRICS_PORT` - Override metrics port
- `LOG_LEVEL` - Override log level (debug, info, warn, error)

## Development

### Running Tests

```bash
go test ./... -v
```

### Building

```bash
go build -o prometheus-alerts-handler
```

### Adding Custom Processors

1. Create a new file in `processors/` directory
2. Implement the `AlertProcessor` interface:

```go
type AlertProcessor interface {
    Process(alert types.Alert)
}
```

3. Add factory method in `processors/factory.go`
4. Update configuration schema

## Architecture

```
┌─────────────────┐
│  Alertmanager   │
└────────┬────────┘
         │ HTTP POST
         ▼
┌─────────────────┐
│  /alerts        │
│  Handler        │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Registry       │
│  (Concurrent)   │
└────────┬────────┘
         │
    ┌────┴────┬─────┬──────┐
    ▼         ▼     ▼      ▼
  Slack    Email  PagerDuty Webhook
```

## License

MIT License - see [LICENSE](LICENSE) file

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and feature requests, please use the [GitHub issue tracker](https://github.com/igormishsky/prometheus-handler/issues).
