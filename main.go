package main

import (
	"fmt"
	"github.com/sirupsen/logrus"
)

func main() {
	logrus.SetFormatter(&logrus.JSONFormatter{})
	logrus.SetLevel(logrus.InfoLevel)
	fmt.Println("Prometheus Alerts Handler")
}
