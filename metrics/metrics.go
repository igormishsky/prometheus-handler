package metrics

import (
	"net/http"

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

	AlertsProcessed = prometheus.NewCounter(
		prometheus.CounterOpts{
			Name: "prometheus_alerts_handler_alerts_processed_total",
			Help: "Total number of alerts successfully processed",
		},
	)

	AlertsProcessingErrors = prometheus.NewCounter(
		prometheus.CounterOpts{
			Name: "prometheus_alerts_handler_processing_errors_total",
			Help: "Total number of errors during alert processing",
		},
	)

	ProcessingDuration = prometheus.NewHistogram(
		prometheus.HistogramOpts{
			Name:    "prometheus_alerts_handler_processing_duration_seconds",
			Help:    "Duration of alert processing in seconds",
			Buckets: prometheus.DefBuckets,
		},
	)
)

func init() {
	prometheus.MustRegister(AlertsReceived)
	prometheus.MustRegister(AlertsProcessed)
	prometheus.MustRegister(AlertsProcessingErrors)
	prometheus.MustRegister(ProcessingDuration)
}

func GetHandler() http.Handler {
	return promhttp.Handler()
}
