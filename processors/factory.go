package processors

import (
	"fmt"

	"github.com/igormishsky/prometheus-alerts-handler/config"
)

// Factory creates processors based on configuration
type Factory struct {
	processors map[string]AlertProcessor
}

// NewFactory creates a new processor factory
func NewFactory() *Factory {
	return &Factory{
		processors: make(map[string]AlertProcessor),
	}
}

// Register registers a processor with a given name
func (f *Factory) Register(name string, processor AlertProcessor) {
	f.processors[name] = processor
}

// CreateProcessor creates a processor based on configuration
func (f *Factory) CreateProcessor(cfg config.ProcessorConfig) (AlertProcessor, error) {
	switch cfg.Type {
	case "slack":
		return NewSlackProcessor(cfg.Config)
	case "email":
		return NewEmailProcessor(cfg.Config)
	case "webhook":
		return NewWebhookProcessor(cfg.Config)
	case "pagerduty":
		return NewPagerDutyProcessor(cfg.Config)
	case "basic":
		return &BasicProcessor{}, nil
	default:
		return nil, fmt.Errorf("unknown processor type: %s", cfg.Type)
	}
}

// GetProcessor returns a registered processor by name
func (f *Factory) GetProcessor(name string) (AlertProcessor, bool) {
	processor, ok := f.processors[name]
	return processor, ok
}

// GetAllProcessors returns all registered processors
func (f *Factory) GetAllProcessors() []AlertProcessor {
	processors := make([]AlertProcessor, 0, len(f.processors))
	for _, p := range f.processors {
		processors = append(processors, p)
	}
	return processors
}
