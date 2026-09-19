package com.hepsiburada.qa.drivers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

public enum BrowserType {
    
    CHROME {
        @Override
        public ChromeOptions getOptions(boolean isHeadless) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-infobars");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            options.addArguments("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36");
            options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
            options.setExperimentalOption("useAutomationExtension", false);
            if (isHeadless) {
                options.addArguments("--headless=new");
            }
            return options;
        }

        @Override
        public WebDriver createDriver(boolean isHeadless) {
            return new ChromeDriver(getOptions(isHeadless));
        }

        @Override
        public WebDriver createRemoteDriver(String gridUrl, boolean isHeadless) {
            try {
                return new RemoteWebDriver(new URL(gridUrl), getOptions(isHeadless));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid Selenium Grid URL: " + gridUrl, e);
            }
        }
    },

    FIREFOX {
        @Override
        public FirefoxOptions getOptions(boolean isHeadless) {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("start-maximized");
            if (isHeadless) {
                options.addArguments("--headless");
            }
            return options;
        }

        @Override
        public WebDriver createDriver(boolean isHeadless) {
            return new FirefoxDriver(getOptions(isHeadless));
        }

        @Override
        public WebDriver createRemoteDriver(String gridUrl, boolean isHeadless) {
            try {
                return new RemoteWebDriver(new URL(gridUrl), getOptions(isHeadless));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid Selenium Grid URL: " + gridUrl, e);
            }
        }
    },

    EDGE {
        @Override
        public EdgeOptions getOptions(boolean isHeadless) {
            EdgeOptions options = new EdgeOptions();
            options.addArguments("--start-maximized");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-notifications");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--no-sandbox");
            if (isHeadless) {
                options.addArguments("--headless=new");
            }
            return options;
        }

        @Override
        public WebDriver createDriver(boolean isHeadless) {
            return new EdgeDriver(getOptions(isHeadless));
        }

        @Override
        public WebDriver createRemoteDriver(String gridUrl, boolean isHeadless) {
            try {
                return new RemoteWebDriver(new URL(gridUrl), getOptions(isHeadless));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid Selenium Grid URL: " + gridUrl, e);
            }
        }
    };
    
    public abstract MutableCapabilities getOptions(boolean isHeadless);
    public abstract WebDriver createDriver(boolean isHeadless);
    public abstract WebDriver createRemoteDriver(String gridUrl, boolean isHeadless);
}
