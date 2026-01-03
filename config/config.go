package config

import (
	"fmt"
	"os"

	"gopkg.in/yaml.v3"
)

// Config represents the application configuration
type Config struct {
	Server     ServerConfig      `yaml:"server"`
	Processors []ProcessorConfig `yaml:"processors"`
}

// ServerConfig represents HTTP server configuration
type ServerConfig struct {
	Port        int    `yaml:"port" env:"SERVER_PORT"`
	MetricsPort int    `yaml:"metrics_port" env:"METRICS_PORT"`
	LogLevel    string `yaml:"log_level" env:"LOG_LEVEL"`
}

// ProcessorConfig represents a processor configuration
type ProcessorConfig struct {
	Type    string                 `yaml:"type"`
	Enabled bool                   `yaml:"enabled"`
	Name    string                 `yaml:"name"`
	Config  map[string]interface{} `yaml:"config"`
}

// LoadConfig loads configuration from a YAML file
func LoadConfig(path string) (*Config, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("failed to read config file: %w", err)
	}

	var cfg Config
	if err := yaml.Unmarshal(data, &cfg); err != nil {
		return nil, fmt.Errorf("failed to parse config file: %w", err)
	}

	// Apply defaults
	if cfg.Server.Port == 0 {
		cfg.Server.Port = 8080
	}
	if cfg.Server.MetricsPort == 0 {
		cfg.Server.MetricsPort = 2112
	}
	if cfg.Server.LogLevel == "" {
		cfg.Server.LogLevel = "info"
	}

	return &cfg, nil
}

// GetEnv gets an environment variable or returns a default value
func GetEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}
