package processors

import (
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/sirupsen/logrus"
)

type AlertProcessor interface {
	Process(alert types.Alert)
}

type BasicProcessor struct{}

func (bp *BasicProcessor) Process(alert types.Alert) {
	logrus.Info("Processing alert:", alert)

	severity, ok := alert.Labels["severity"]
	if !ok {
		logrus.Warn("Alert has no severity label")
		return
	}

	switch severity {
	case "critical":
		bp.processCriticalAlert(alert)
	case "warning":
		bp.processWarningAlert(alert)
	default:
		logrus.Warn("Unknown severity:", severity)
	}
}

func (bp *BasicProcessor) processCriticalAlert(alert types.Alert) {
	logrus.Error("Critical alert:", alert)
	// Implement critical alert handling logic
}

func (bp *BasicProcessor) processWarningAlert(alert types.Alert) {
	logrus.Warn("Warning alert:", alert)
	// Implement warning alert handling logic
}
