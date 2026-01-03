package processor

import (
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/sirupsen/logrus"
)

func ProcessAlert(alert types.Alert) {
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

func processCriticalAlert(alert types.Alert) {
	logrus.Error("Critical alert:", alert)
	// Implement critical alert handling logic
}

func processWarningAlert(alert types.Alert) {
	logrus.Warn("Warning alert:", alert)
	// Implement warning alert handling logic
}
