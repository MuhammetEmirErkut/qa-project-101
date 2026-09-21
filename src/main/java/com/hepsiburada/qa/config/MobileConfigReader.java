package com.hepsiburada.qa.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({
    "system:properties",
    "system:env",
    "classpath:config/mobile-config.properties",
    "classpath:mobile-config.properties"
})
public interface MobileConfigReader extends Config {

    @Key("appium.server.url")
    @DefaultValue("http://127.0.0.1:4723/")
    String appiumServerUrl();

    @Key("mobile.base.url")
    @DefaultValue("https://www.hepsiburada.com")
    String mobileBaseUrl();

    @Key("mobile.platform")
    @DefaultValue("ANDROID")
    String platform();

    @Key("mobile.android.device.name")
    @DefaultValue("Medium_Phone")
    String androidDeviceName();

    @Key("mobile.android.platform.version")
    @DefaultValue("")
    String androidPlatformVersion();

    @Key("mobile.android.automation.name")
    @DefaultValue("UiAutomator2")
    String androidAutomationName();

    @Key("mobile.android.browser.name")
    @DefaultValue("Chrome")
    String androidBrowserName();

    @Key("mobile.android.chromedriver.autodownload")
    @DefaultValue("true")
    boolean androidChromeDriverAutoDownload();

    @Key("mobile.android.headless")
    @DefaultValue("false")
    boolean androidHeadless();

    @Key("mobile.ios.device.name")
    @DefaultValue("iPhone 15 Pro")
    String iosDeviceName();

    @Key("mobile.ios.platform.version")
    @DefaultValue("17.4")
    String iosPlatformVersion();

    @Key("mobile.ios.automation.name")
    @DefaultValue("XCUITest")
    String iosAutomationName();

    @Key("mobile.ios.browser.name")
    @DefaultValue("Safari")
    String iosBrowserName();

    @Key("mobile.explicit.wait")
    @DefaultValue("15")
    int explicitWait();

    @Key("mobile.implicit.wait")
    @DefaultValue("10")
    int implicitWait();

    @Key("mobile.command.timeout")
    @DefaultValue("300")
    int commandTimeout();
}
