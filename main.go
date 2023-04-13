package main

import (
	"fmt"
	"github.com/igormishsky/prometheus-alerts-handler/handler"
)

func main() {
	fmt.Println("Prometheus Alerts Handler")
	router.HandleFunc("/alerts", handler.AlertsHandler).Methods("POST")
}
