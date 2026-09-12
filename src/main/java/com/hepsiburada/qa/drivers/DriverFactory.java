package com.hepsiburada.qa.drivers;

import org.openqa.selenium.WebDriver;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> driverPool = new ThreadLocal<>();
    
    private DriverFactory() { }

    public static void initDriver(BrowserType browserType, boolean isRemote, String gridUrl, boolean isHeadless) {
        if (driverPool.get() == null) {
            WebDriver driver;
            if (isRemote) {
                driver = browserType.createRemoteDriver(gridUrl, isHeadless);
            } else {
                driver = browserType.createDriver(isHeadless);
            }
            driverPool.set(driver);
        }
    }

    public static void initDriver(String browserName, boolean isRemote, String gridUrl, boolean isHeadless) {
        BrowserType browserType = BrowserType.valueOf(browserName.trim().toUpperCase());
        initDriver(browserType, isRemote, gridUrl, isHeadless);
    }

    public static void initDriver(BrowserType browserType, boolean isHeadless) {
        initDriver(browserType, false, null, isHeadless);
    }

    public static void initDriver(String browserName, boolean isHeadless) {
        initDriver(browserName, false, null, isHeadless);
    }

    public static WebDriver getDriver() {
        return driverPool.get();
    }

    public static void quitDriver() {
        if (driverPool.get() != null) {
            try {
                driverPool.get().quit();
            } finally {
                driverPool.remove();
            }
        }
    }
}
