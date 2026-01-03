package main

import (
	"context"
	"fmt"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/gorilla/mux"
	"github.com/igormishsky/prometheus-alerts-handler/config"
	"github.com/igormishsky/prometheus-alerts-handler/handler"
	"github.com/igormishsky/prometheus-alerts-handler/metrics"
	"github.com/igormishsky/prometheus-alerts-handler/processors"
	"github.com/sirupsen/logrus"
)

func main() {
	// Print banner
	fmt.Println("==========================================")
	fmt.Println("  Prometheus Alerts Handler")
	fmt.Println("  A flexible alert routing system")
	fmt.Println("==========================================")

	// Load configuration
	configPath := os.Getenv("CONFIG_PATH")
	if configPath == "" {
		configPath = "config.yaml"
	}

	cfg, err := config.LoadConfig(configPath)
	if err != nil {
		logrus.WithError(err).Warn("Failed to load config file, using defaults")
		cfg = getDefaultConfig()
	}

	// Configure logging
	logrus.SetFormatter(&logrus.JSONFormatter{})
	logLevel, err := logrus.ParseLevel(cfg.Server.LogLevel)
	if err != nil {
		logLevel = logrus.InfoLevel
	}
	logrus.SetLevel(logLevel)

	logrus.WithFields(logrus.Fields{
		"port":         cfg.Server.Port,
		"metrics_port": cfg.Server.MetricsPort,
		"log_level":    cfg.Server.LogLevel,
	}).Info("Starting Prometheus Alerts Handler")

	// Create processor registry and load processors from config
	registry := processors.NewRegistry()
	if err := registry.LoadFromConfig(cfg); err != nil {
		logrus.WithError(err).Fatal("Failed to load processors from config")
	}

	if registry.GetProcessorCount() == 0 {
		logrus.Warn("No processors configured - alerts will be logged but not sent anywhere")
		// Register basic processor as fallback
		registry.Register(&processors.BasicProcessor{})
	}

	logrus.WithField("count", registry.GetProcessorCount()).Info("Processors loaded")

	// Create alert handler
	alertHandler := handler.NewHandler(registry)

	// Setup main router
	mainRouter := mux.NewRouter()
	mainRouter.Handle("/alerts", alertHandler).Methods("POST")
	mainRouter.HandleFunc("/health", healthHandler).Methods("GET")
	mainRouter.HandleFunc("/", rootHandler).Methods("GET")

	// Setup metrics router
	metricsRouter := mux.NewRouter()
	metricsRouter.Handle("/metrics", metrics.GetHandler())
	metricsRouter.HandleFunc("/health", healthHandler).Methods("GET")

	// Create HTTP servers
	mainServer := &http.Server{
		Addr:         fmt.Sprintf(":%d", cfg.Server.Port),
		Handler:      mainRouter,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	metricsServer := &http.Server{
		Addr:         fmt.Sprintf(":%d", cfg.Server.MetricsPort),
		Handler:      metricsRouter,
		ReadTimeout:  15 * time.Second,
		WriteTimeout: 15 * time.Second,
		IdleTimeout:  60 * time.Second,
	}

	// Start metrics server
	go func() {
		logrus.WithField("port", cfg.Server.MetricsPort).Info("Starting metrics server")
		if err := metricsServer.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logrus.WithError(err).Fatal("Metrics server failed")
		}
	}()

	// Start main server
	go func() {
		logrus.WithField("port", cfg.Server.Port).Info("Starting main server")
		if err := mainServer.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			logrus.WithError(err).Fatal("Main server failed")
		}
	}()

	logrus.Info("Servers started successfully")
	fmt.Printf("\n✓ Main server listening on port %d\n", cfg.Server.Port)
	fmt.Printf("✓ Metrics server listening on port %d\n", cfg.Server.MetricsPort)
	fmt.Printf("✓ Ready to receive alerts at http://localhost:%d/alerts\n\n", cfg.Server.Port)

	// Wait for interrupt signal to gracefully shutdown
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	logrus.Info("Shutting down servers...")

	// Create shutdown context with timeout
	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	// Shutdown servers gracefully
	if err := mainServer.Shutdown(ctx); err != nil {
		logrus.WithError(err).Error("Main server forced to shutdown")
	}

	if err := metricsServer.Shutdown(ctx); err != nil {
		logrus.WithError(err).Error("Metrics server forced to shutdown")
	}

	logrus.Info("Servers stopped")
}

func healthHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	w.Write([]byte(`{"status":"healthy"}`))
}

func rootHandler(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "text/html")
	w.WriteHeader(http.StatusOK)
	html := `
<!DOCTYPE html>
<html>
<head>
    <title>Prometheus Alerts Handler</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }
        .container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        h1 { color: #e6522c; }
        .endpoint { background: #f8f8f8; padding: 15px; margin: 10px 0; border-left: 4px solid #e6522c; }
        code { background: #eee; padding: 2px 6px; border-radius: 3px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔥 Prometheus Alerts Handler</h1>
        <p>A flexible alert routing system for Prometheus Alertmanager</p>

        <h2>Available Endpoints</h2>

        <div class="endpoint">
            <h3>POST /alerts</h3>
            <p>Receive and process alerts from Prometheus Alertmanager</p>
        </div>

        <div class="endpoint">
            <h3>GET /health</h3>
            <p>Health check endpoint</p>
        </div>

        <div class="endpoint">
            <h3>GET /metrics</h3>
            <p>Prometheus metrics (available on metrics port)</p>
        </div>

        <h2>Supported Processors</h2>
        <ul>
            <li><strong>Slack</strong> - Send alerts to Slack channels</li>
            <li><strong>Email</strong> - Send alerts via email (SMTP)</li>
            <li><strong>Webhook</strong> - Send alerts to generic webhooks</li>
            <li><strong>PagerDuty</strong> - Create incidents in PagerDuty</li>
        </ul>

        <h2>Example Configuration</h2>
        <p>Configure alert processors via <code>config.yaml</code></p>
    </div>
</body>
</html>
	`
	w.Write([]byte(html))
}

func getDefaultConfig() *config.Config {
	return &config.Config{
		Server: config.ServerConfig{
			Port:        8080,
			MetricsPort: 2112,
			LogLevel:    "info",
		},
		Processors: []config.ProcessorConfig{
			{
				Type:    "basic",
				Enabled: true,
				Name:    "default-basic",
				Config:  make(map[string]interface{}),
			},
		},
	}
}
