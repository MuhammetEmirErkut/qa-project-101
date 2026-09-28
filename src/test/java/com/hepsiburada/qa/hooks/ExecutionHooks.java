package com.hepsiburada.qa.hooks;

import com.hepsiburada.qa.config.ConfigReader;
import com.hepsiburada.qa.drivers.DriverFactory;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.thoughtworks.gauge.ExecutionContext;
import org.openqa.selenium.WebDriver;

import java.sql.Driver;

public class ExecutionHooks {
    private static final Logger log = LogManager.getLogger(ExecutionHooks.class);
    private ConfigReader config;

    @BeforeScenario
    public void setUp(ExecutionContext context) {
        log.info("================================================================================");
        log.info("🚀 [Gauge Scenario Starting]: {}", context.getCurrentScenario().getName());
        log.info("📄 [Spec]: {}", context.getCurrentSpecification().getName());
        log.info("================================================================================");

        config = ConfigFactory.create(ConfigReader.class);

        String browser = System.getProperty("browser", System.getenv().getOrDefault("browser", System.getenv().getOrDefault("BROWSER", config.browser())));
        
        String remoteStr = System.getProperty("remote", System.getenv().getOrDefault("remote", System.getenv().getOrDefault("REMOTE", String.valueOf(config.remote()))));
        boolean isRemote = Boolean.parseBoolean(remoteStr);

        String gridUrl = System.getProperty("gridUrl", System.getenv().getOrDefault("gridUrl", System.getenv().getOrDefault("GRID_URL", System.getenv().getOrDefault("SELENIUM_REMOTE_URL", config.gridUrl()))));
        
        String headlessStr = System.getProperty("headless", System.getenv().getOrDefault("headless", System.getenv().getOrDefault("HEADLESS", String.valueOf(config.headless()))));
        boolean isHeadless = Boolean.parseBoolean(headlessStr);

        // Auto-detect Docker / Jenkins CI container environment
        if (System.getenv("JENKINS_HOME") != null || System.getenv("JENKINS_URL") != null) {
            isRemote = true;
            isHeadless = true;
            if (gridUrl == null || gridUrl.contains("localhost")) {
                gridUrl = "http://selenium-hub:4444";
            }
            log.info("Detected Jenkins CI environment. Automatically routing to Selenium Grid: {}", gridUrl);
        }

        log.info("Initializing WebDriver -> Browser: {}, Remote: {}, GridUrl: {}, Headless: {}", browser, isRemote, gridUrl, isHeadless);
        DriverFactory.initDriver(browser, isRemote, gridUrl, isHeadless);

        WebDriver driver = DriverFactory.getDriver();
        driver.get(config.baseUrl());
        log.info("Navigated to base URL: {}", config.baseUrl());
    }

    @AfterScenario
    public void tearDown(ExecutionContext context) {
        log.info("🏁 [Gauge Scenario Finished]: {}", context.getCurrentScenario().getName());
        DriverFactory.quitDriver();
    }
}
