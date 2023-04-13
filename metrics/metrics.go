package metrics

import (
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

var (
	AlertsReceived = prometheus.NewCounter(
		prometheus.CounterOpts{
			Name: "prometheus_alerts_handler_alerts_received_total",
			Help: "Total number of alerts received by the handler",
		},
	)
)

func init() {
	prometheus.MustRegister(AlertsReceived)
}

func GetHandler() promhttp.Handler {
	return promhttp.Handler()
}
