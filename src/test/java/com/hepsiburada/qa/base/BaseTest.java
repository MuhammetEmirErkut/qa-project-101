package com.hepsiburada.qa.base;

import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.hepsiburada.qa.config.ConfigReader;
import com.hepsiburada.qa.drivers.DriverFactory;
import static com.hepsiburada.qa.drivers.DriverFactory.getDriver;

public abstract class BaseTest {
    protected ConfigReader config;

    @BeforeMethod
    public void setUp() {
        config = ConfigFactory.create(ConfigReader.class);
        DriverFactory.initDriver(
            config.browser(),
            config.remote(),
            config.gridUrl(),
            config.headless()
        );
        getDriver().get(config.baseUrl());
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }

    public org.openqa.selenium.WebDriver getDriver() {
        return DriverFactory.getDriver();
    }
}
