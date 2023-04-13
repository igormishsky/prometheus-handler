package main

import (
	"fmt"
	"github.com/igormishsky/prometheus-alerts-handler/handler"
	"github.com/sirupsen/logrus"
)

func main() {
	logrus.SetFormatter(&logrus.JSONFormatter{})
	logrus.SetLevel(logrus.InfoLevel)
	fmt.Println("Prometheus Alerts Handler")
	router.HandleFunc("/alerts", handler.AlertsHandler).Methods("POST")
}
