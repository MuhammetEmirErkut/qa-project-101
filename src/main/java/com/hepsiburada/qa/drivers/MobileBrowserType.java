package com.hepsiburada.qa.drivers;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.CapabilityType;

import com.hepsiburada.qa.config.MobileConfigReader;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;

public enum MobileBrowserType {

    ANDROID_CHROME {
        @Override
        public Capabilities getCapabilities(MobileConfigReader config) {
            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            if (config.androidDeviceName() != null && !config.androidDeviceName().trim().isEmpty()) {
                options.setDeviceName(config.androidDeviceName());
            }
            if (config.androidPlatformVersion() != null && !config.androidPlatformVersion().trim().isEmpty()) {
                options.setPlatformVersion(config.androidPlatformVersion());
            }
            options.setAutomationName(config.androidAutomationName());
            options.setCapability(CapabilityType.BROWSER_NAME, config.androidBrowserName());
            options.setCapability("appium:chromedriver_autodownload", config.androidChromeDriverAutoDownload());
            options.setCapability("appium:newCommandTimeout", config.commandTimeout());
            options.setCapability("appium:noReset", false);

            // ==================== ANTI-BOT & CHROME CAPABILITIES ====================
            Map<String, Object> chromeOptions = new HashMap<>();
            chromeOptions.put("args", Arrays.asList(
                "--disable-blink-features=AutomationControlled",
                "--disable-notifications",
                "--disable-infobars",
                "--disable-dev-shm-usage",
                "--no-sandbox",
                "--ignore-certificate-errors",
                "user-agent=Mozilla/5.0 (Linux; Android 14; Mobile; Pixel 7 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
            ));
            chromeOptions.put("excludeSwitches", Collections.singletonList("enable-automation"));

            options.setCapability("goog:chromeOptions", chromeOptions);
            options.setCapability("appium:chromeOptions", chromeOptions);

            return options;
        }

        @Override
        public AppiumDriver createDriver(URL appiumServerUrl, MobileConfigReader config) {
            return new AndroidDriver(appiumServerUrl, getCapabilities(config));
        }
    },

    IOS_SAFARI {
        @Override
        public Capabilities getCapabilities(MobileConfigReader config) {
            XCUITestOptions options = new XCUITestOptions();
            options.setPlatformName("iOS");
            options.setDeviceName(config.iosDeviceName());
            if (config.iosPlatformVersion() != null && !config.iosPlatformVersion().trim().isEmpty()) {
                options.setPlatformVersion(config.iosPlatformVersion());
            }
            options.setAutomationName(config.iosAutomationName());
            options.setCapability(CapabilityType.BROWSER_NAME, config.iosBrowserName());
            options.setCapability("appium:newCommandTimeout", config.commandTimeout());
            return options;
        }

        @Override
        public AppiumDriver createDriver(URL appiumServerUrl, MobileConfigReader config) {
            return new IOSDriver(appiumServerUrl, getCapabilities(config));
        }
    };

    public abstract Capabilities getCapabilities(MobileConfigReader config);
    public abstract AppiumDriver createDriver(URL appiumServerUrl, MobileConfigReader config);
}
