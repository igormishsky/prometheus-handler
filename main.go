package main

import (
	"fmt"
	"github.com/gorilla/mux"
	"github.com/igormishsky/prometheus-alerts-handler/metrics"
	"github.com/sirupsen/logrus"
	"net/http"
)

func main() {
	logrus.SetFormatter(&logrus.JSONFormatter{})
	logrus.SetLevel(logrus.InfoLevel)
	fmt.Println("Prometheus Alerts Handler")
	metricsRouter := mux.NewRouter()
	metricsRouter.Handle("/metrics", metrics.GetHandler())
	go http.ListenAndServe(":2112", metricsRouter)
}
