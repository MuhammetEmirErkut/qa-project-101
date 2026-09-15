# QA Test Automation Project

This repository contains an enterprise-level UI Test Automation framework developed using Java, Selenium WebDriver, TestNG, Selenium Grid (Docker), and Allure Report.

## 🚀 Key Features

* **Isolated Execution with Selenium Grid (Docker):** Runs tests inside Docker containers without opening browser windows on your local desktop.
* **Allure Reporting & Failure Diagnostics:** Automatically captures screenshots, attaches DOM page sources, and generates copy-paste friendly AI error reports for failures.
* **Page Object Model (POM):** Clean, maintainable component-based page architecture.

---

## 🛠️ Tech Stack
* **Language:** Java 17+
* **Automation Engine:** Selenium WebDriver 4.21.0 & RemoteWebDriver
* **Test Runner:** TestNG 7.10.2
* **Infrastructure:** Docker & Docker Compose (Selenium Hub, Chrome Node, Firefox Node)
* **Reporting:** Allure Report & Custom AI-Ready Failure Listener
* **Configuration:** Aeonbits Owner (Type-safe properties)
* **Logging:** Log4j2 & SLF4J

---

## 🚦 Quick Start Guide

### 1. Start Infrastructure (Jenkins, Selenium Grid & Mock API)
```bash
docker compose -f ci/docker-compose.yml up -d
```
* **Jenkins UI:** [http://localhost:8080](http://localhost:8080)
* **Grid Console:** [http://localhost:4444](http://localhost:4444)
* **Live VNC Viewer (Optional):** [http://localhost:7900](http://localhost:7900) *(Password: `secret`)*
* **Mock API:** [http://localhost:3001](http://localhost:3001)

### 2. Run Tests
```bash
# Run all tests on Selenium Grid (remote=true by default)
mvn clean test

# Run tests locally (optional)
mvn clean test -Dremote=false
```

### 3. Generate & View Allure Report
```bash
# Generate and open Allure report in browser
mvn allure:serve

# Or generate static HTML report
mvn allure:report
```

### 4. Stop Infrastructure
```bash
docker compose -f ci/docker-compose.yml down
```

---


## 📋 Failure Artifacts
When a test fails, the framework automatically produces:
1. **Screenshots:** Saved under `target/failures/screenshots/`
2. **AI-Ready Error Reports:** Saved under `target/failures/reports/<test_name>_error.txt`
3. **Allure Attachments:** Screenshots, HTML DOM snapshots, and error summaries are directly visible in Allure.

---

## 📚 Guides & Documentation
* 🚀 [Automation Architecture Guide](docs/AUTOMATION_GUIDE.md)
* ⚡ [JMeter Performance, Load & Spike Testing Guide](docs/JMETER_PERFORMANCE_TESTING_GUIDE.md)
* 🤖 [Jenkins CI/CD Architecture Guide](docs/JENKINS_CI_CD_GUIDE.md)
* 🛠️ [Jenkins Item (Job) Creation & Setup Guide](docs/JENKINS_ITEM_CREATION_GUIDE.md)
* 🛒 [Cart & PDP Tests Guide](docs/CART_AND_PDP_GUIDE.md)


