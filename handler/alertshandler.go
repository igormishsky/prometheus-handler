package handler

import (
	"encoding/json"
	"io/ioutil"
	"net/http"
)

package handler

import (
"encoding/json"
"io/ioutil"
"net/http"

"github.com/sirupsen/logrus"
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
		http.Error(w, "Error reading request body", http.StatusBadRequest)
		return
	}

	var alerts []Alert
	if err := json.Unmarshal(body, &alerts); err != nil {
		logrus.Error("Error unmarshalling request body:", err)
		http.Error(w, "Error unmarshalling request body", http.StatusBadRequest)
		return
	}

	for _, alert := range alerts {
		logrus.Info("Received alert:", alert)
	}

	w.WriteHeader(http.StatusOK)
	w.Write([]byte("Alerts received"))
}

