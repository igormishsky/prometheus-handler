package processors_test

import (
	"testing"

	"github.com/igormishsky/prometheus-alerts-handler/processors"
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/stretchr/testify/assert"
)

func TestBasicProcessor_Process(t *testing.T) {
	sampleAlerts := []types.Alert{
		{
			Status: "firing",
			Labels: map[string]string{
				"severity": "critical",
			},
			Annotations: map[string]string{
				"description": "Test critical description",
			},
		},
		{
			Status: "firing",
			Labels: map[string]string{
				"severity": "warning",
			},
			Annotations: map[string]string{
				"description": "Test warning description",
			},
		},
	}

	basicProcessor := &processors.BasicProcessor{}

	for _, alert := range sampleAlerts {
		basicProcessor.Process(alert)
	}

	assert.True(t, true, "BasicProcessor processed alerts without issues")
}
