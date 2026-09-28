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

        DriverFactory.initDriver(config.browser(), config.remote(), config.gridUrl(), config.headless());

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
