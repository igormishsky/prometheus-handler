package processors

import (
	"sync"

	"github.com/igormishsky/prometheus-alerts-handler/config"
	"github.com/igormishsky/prometheus-alerts-handler/types"
	"github.com/sirupsen/logrus"
)

// Registry manages multiple alert processors
type Registry struct {
	processors []AlertProcessor
	mu         sync.RWMutex
}

// NewRegistry creates a new processor registry
func NewRegistry() *Registry {
	return &Registry{
		processors: make([]AlertProcessor, 0),
	}
}

// Register adds a processor to the registry
func (r *Registry) Register(processor AlertProcessor) {
	r.mu.Lock()
	defer r.mu.Unlock()
	r.processors = append(r.processors, processor)
}

// ProcessAlert sends an alert to all registered processors
func (r *Registry) ProcessAlert(alert types.Alert) {
	r.mu.RLock()
	defer r.mu.RUnlock()

	if len(r.processors) == 0 {
		logrus.Warn("No processors registered, alert will not be processed")
		return
	}

	// Process alert with all registered processors concurrently
	var wg sync.WaitGroup
	for _, processor := range r.processors {
		wg.Add(1)
		go func(p AlertProcessor) {
			defer wg.Done()
			defer func() {
				if r := recover(); r != nil {
					logrus.WithField("panic", r).Error("Processor panicked")
				}
			}()
			p.Process(alert)
		}(processor)
	}
	wg.Wait()
}

// LoadFromConfig loads processors from configuration
func (r *Registry) LoadFromConfig(cfg *config.Config) error {
	factory := NewFactory()

	for _, processorCfg := range cfg.Processors {
		if !processorCfg.Enabled {
			logrus.WithField("processor", processorCfg.Name).Info("Processor disabled, skipping")
			continue
		}

		processor, err := factory.CreateProcessor(processorCfg)
		if err != nil {
			logrus.WithError(err).WithField("processor", processorCfg.Name).Error("Failed to create processor")
			continue
		}

		r.Register(processor)
		logrus.WithFields(logrus.Fields{
			"name": processorCfg.Name,
			"type": processorCfg.Type,
		}).Info("Registered processor")
	}

	return nil
}

// GetProcessorCount returns the number of registered processors
func (r *Registry) GetProcessorCount() int {
	r.mu.RLock()
	defer r.mu.RUnlock()
	return len(r.processors)
}
