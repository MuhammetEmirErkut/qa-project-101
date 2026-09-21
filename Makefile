# =============================================================================
# Makefile - Enterprise QA & SDET Automation Platform (qa-project-101)
# =============================================================================

.PHONY: help up down test-api test-ui test-regression perf-load report clean lint

help: ## Show available make targets
	@echo "Available commands in QA Automation Platform:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

up: ## Start all infrastructure containers (Grid, Jenkins, Mock API, Grafana, InfluxDB, Prometheus, Appium)
	@echo "🚀 Starting QA Test Infrastructure..."
	@cd ci && docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d --remove-orphans
	@echo "✅ All services started successfully!"
	@echo "👉 Selenium Grid: http://localhost:4444"
	@echo "👉 Jenkins:       http://localhost:8080"
	@echo "👉 Grafana:       http://localhost:3000 (admin/admin)"
	@echo "👉 Mock API:      http://localhost:3001"
	@echo "👉 Appium Server: http://localhost:4723"

down: ## Stop and remove all infrastructure containers
	@echo "🛑 Stopping QA Test Infrastructure..."
	@cd ci && docker compose -f docker-compose.yml -f docker-compose.monitoring.yml down --remove-orphans
	@echo "✅ Infrastructure stopped cleanly."

test-api: ## Run RestAssured API automation tests against Mock API
	@echo "🧪 Running API Automated Tests..."
	@node performance-test/mock-api/server.js & MOCK_PID=$$!; \
	sleep 2; \
	mvn test -Dsurefire.suiteXmlFiles=src/test/resources/suites/api-tests.xml -DapiBaseUrl=http://localhost:3001; \
	kill $$MOCK_PID || true

test-ui: ## Run UI Smoke Tests (Headless mode)
	@echo "🌐 Running UI Smoke Tests (Chrome Headless)..."
	@mvn test -Dsurefire.suiteXmlFiles=src/test/resources/suites/smoke-tests.xml -Dbrowser=chrome -Dheadless=true -Dremote=false

test-regression: ## Run Full Regression Test Suite
	@echo "🔄 Running Full Regression Suite..."
	@mvn test -DsuiteXmlFile=src/test/resources/suites/regression-tests.xml -Dbrowser=chrome -Dheadless=true -Dremote=false

test-mobile-smoke: ## Run Mobile Web Smoke Tests on Appium (Android Chrome)
	@echo "📱 Running Mobile Smoke Tests on Appium..."
	@mvn test -DsuiteXmlFile=src/test/resources/suites/mobile-smoke-tests.xml

test-mobile-regression: ## Run Mobile Web Full Regression Suite on Appium
	@echo "📱 Running Mobile Full Regression Suite on Appium..."
	@mvn test -DsuiteXmlFile=src/test/resources/suites/mobile-regression-tests.xml

perf-load: ## Run JMeter Load Test profile against Mock API
	@echo "⚡ Running JMeter Load Test Profile..."
	@jmeter -n \
		-t performance-test/jmeter/ecommerce_api_test_plan.jmx \
		-q performance-test/jmeter/profiles/load.properties \
		-Jhost=localhost -Jport=3001 \
		-JinfluxdbUrl=http://localhost:8086/write?db=jmeter \
		-l performance-test/jmeter/results.jtl \
		-e -o performance-test/jmeter/html-report

report: ## Generate and open live Allure Test Report
	@echo "📊 Launching Allure Report Server..."
	@mvn allure:serve

clean: ## Clean build artifacts and temporary files
	@echo "🧹 Cleaning workspace and report artifacts..."
	@mvn clean
	@rm -rf logs/ target/ performance-test/jmeter/results.jtl performance-test/jmeter/html-report
