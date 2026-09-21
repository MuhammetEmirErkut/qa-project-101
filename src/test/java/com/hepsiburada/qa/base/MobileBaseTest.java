package com.hepsiburada.qa.base;

import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.hepsiburada.qa.config.MobileConfigReader;
import com.hepsiburada.qa.drivers.MobileBrowserType;
import com.hepsiburada.qa.drivers.MobileDriverFactory;

import io.appium.java_client.AppiumDriver;

public abstract class MobileBaseTest {

    protected MobileConfigReader config;

    @BeforeMethod
    public void setUp() {
        config = ConfigFactory.create(MobileConfigReader.class, System.getProperties(), System.getenv());
        String platform = config.platform().trim().toUpperCase();
        MobileBrowserType browserType;
        
        if ("IOS".equals(platform)) {
            browserType = MobileBrowserType.IOS_SAFARI;
        } else {
            browserType = MobileBrowserType.ANDROID_CHROME;
        }

        MobileDriverFactory.initDriver(browserType, config);
        getDriver().get(config.mobileBaseUrl());
    }

    @AfterMethod
    public void tearDown() {
        MobileDriverFactory.quitDriver();
    }

    public AppiumDriver getDriver() {
        return MobileDriverFactory.getDriver();
    }
}
