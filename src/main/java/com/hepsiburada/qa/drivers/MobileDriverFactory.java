package com.hepsiburada.qa.drivers;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hepsiburada.qa.config.MobileConfigReader;

import io.appium.java_client.AppiumDriver;

public final class MobileDriverFactory {

    private static final Logger log = LogManager.getLogger(MobileDriverFactory.class);
    private static final ThreadLocal<AppiumDriver> driverPool = new ThreadLocal<>();

    private MobileDriverFactory() {
    }

    public static void initDriver(MobileBrowserType browserType, MobileConfigReader config) {
        if (driverPool.get() == null) {
            try {
                URL serverUrl = new URL(config.appiumServerUrl());
                log.info("Initializing Appium driver for platform [{}] at server [{}]", browserType, serverUrl);
                AppiumDriver driver = browserType.createDriver(serverUrl, config);
                driverPool.set(driver);
            } catch (MalformedURLException e) {
                log.error("Invalid Appium server URL: {}", config.appiumServerUrl(), e);
                throw new RuntimeException("Invalid Appium Server URL: " + config.appiumServerUrl(), e);
            } catch (Exception e) {
                log.error("Failed to initialize Appium driver: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to initialize Appium driver", e);
            }
        }
    }

    public static AppiumDriver getDriver() {
        return driverPool.get();
    }

    public static void quitDriver() {
        if (driverPool.get() != null) {
            try {
                log.info("Quitting Appium mobile driver...");
                driverPool.get().quit();
            } catch (Exception e) {
                log.warn("Error while quitting Appium driver: {}", e.getMessage());
            } finally {
                driverPool.remove();
            }
        }
    }
}
