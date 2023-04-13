package processor

import (
	"github.com/sirupsen/logrus"
	"github.com/yourusername/prometheus-alerts-handler/handler"
)

func ProcessAlert(alert handler.Alert) {
	logrus.Info("Processing alert:", alert)

	severity, ok := alert.Labels["severity"]
	if !ok {
		logrus.Warn("Alert has no severity label")
		return
	}

	switch severity {
	case "critical":
		processCriticalAlert(alert)
	case "warning":
		processWarningAlert(alert)
	default:
		logrus.Warn("Unknown severity:", severity)
	}
}

func processCriticalAlert(alert handler.Alert) {
	logrus.Error("Critical alert:", alert)
	// Implement critical alert handling logic
}

func processWarningAlert(alert handler.Alert) {
	logrus.Warn("Warning alert:", alert)
	// Implement warning alert handling logic
}
