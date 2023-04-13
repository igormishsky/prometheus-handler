package handler

import (
	"encoding/json"
	"github.com/igormishsky/prometheus-alerts-handler/processor"
	"github.com/sirupsen/logrus"
	"io/ioutil"
	"net/http"
)

type Alert struct {
	Status      string            `json:"status"`
	Labels      map[string]string `json:"labels"`
	Annotations map[string]string `json:"annotations"`
}

func AlertsHandler(w http.ResponseWriter, r *http.Request) {
	body, err := ioutil.ReadAll(r.Body)
	if err != nil {
		logrus.Error("Error reading request body:", err)
		respondWithError(w, http.StatusBadRequest, "Error reading request body")
		return
	}

	var alerts []Alert
	if err := json.Unmarshal(body, &alerts); err != nil {
		logrus.Error("Error unmarshalling request body:", err)
		respondWithError(w, http.StatusBadRequest, "Error unmarshalling request body")
		return
	}

	for _, alert := range alerts {
		logrus.Info("Received alert:", alert)
		processor.ProcessAlert(alert)
	}

	w.WriteHeader(http.StatusOK)
	w.Write([]byte("Alerts received"))
}

func respondWithError(w http.ResponseWriter, statusCode int, message string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(statusCode)
	response := map[string]string{"error": message}
	json.NewEncoder(w).Encode(response)
}
