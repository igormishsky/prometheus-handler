package processors

import (
	"github.com/igormishsky/prometheus-alerts-handler/handler"
	"github.com/sirupsen/logrus"
)

type AlertProcessor interface {
	Process(alert handler.Alert)
}

type BasicProcessor struct{}

func (bp *BasicProcessor) Process(alert handler.Alert) {
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

func (bp *BasicProcessor) processCriticalAlert(alert handler.Alert) {
	logrus.Error("Critical alert:", alert)
	// Implement critical alert handling logic
}

func (bp *BasicProcessor) processWarningAlert(alert handler.Alert) {
	logrus.Warn("Warning alert:", alert)
	// Implement warning alert handling logic
}
