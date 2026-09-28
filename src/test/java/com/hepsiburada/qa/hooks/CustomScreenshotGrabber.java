package com.hepsiburada.qa.hooks;

import com.hepsiburada.qa.drivers.DriverFactory;
import com.thoughtworks.gauge.screenshot.ICustomScreenshotGrabber;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class CustomScreenshotGrabber implements ICustomScreenshotGrabber {
    @Override
    public byte[] takeScreenshot() {
        try {
            WebDriver driver = DriverFactory.getDriver();
            if (driver instanceof TakesScreenshot) {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            }
        } catch (Exception ignored) {
        }
        return new byte[0];
    }
}
