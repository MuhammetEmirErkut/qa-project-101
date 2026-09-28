package com.hepsiburada.qa.listeners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.hepsiburada.qa.drivers.DriverFactory;

import io.qameta.allure.Attachment;

public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    private static final String FAILURE_DIR = "target" + File.separator + "failures";
    private static final String SCREENSHOT_DIR = FAILURE_DIR + File.separator + "screenshots";
    private static final String REPORTS_DIR = FAILURE_DIR + File.separator + "reports";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Override
    public void onStart(ITestContext context) {
        createDirectories();
        log.info("========== TEST SUITE STARTED: {} ==========", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("🚀 STARTING TEST: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("✅ TEST PASSED: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("⚠️ TEST SKIPPED: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String testIdentifier = testName + "_" + timestamp;

        WebDriver driver = DriverFactory.getDriver();
        if (driver == null) {
            try {
                Class<?> mobileFactory = Class.forName("com.hepsiburada.qa.drivers.MobileDriverFactory");
                java.lang.reflect.Method getDriverMethod = mobileFactory.getMethod("getDriver");
                Object resultDriver = getDriverMethod.invoke(null);
                if (resultDriver instanceof WebDriver) {
                    driver = (WebDriver) resultDriver;
                }
            } catch (Exception ignored) { }
        }
        String currentUrl = "N/A";
        String pageTitle = "N/A";

        if (driver != null) {
            try {
                currentUrl = driver.getCurrentUrl();
                pageTitle = driver.getTitle();
            } catch (Exception ignored) { }
        }

        // 1. Capture screenshot, attach to Allure and save to local disk
        byte[] screenshotBytes = captureScreenshot(driver);
        String screenshotPath = "Failed to capture";
        if (screenshotBytes != null && screenshotBytes.length > 0) {
            screenshotPath = saveScreenshotToFile(screenshotBytes, testIdentifier);
            attachScreenshotToAllure(testName + " Screenshot", screenshotBytes);
        }

        // 2. Attach page HTML source to Allure
        if (driver != null) {
            try {
                String pageSource = driver.getPageSource();
                if (pageSource != null && !pageSource.isEmpty()) {
                    attachHtmlSourceToAllure(testName + " Page Source", pageSource);
                }
            } catch (Exception ignored) { }
        }

        // 3. Build AI-ready and copy-paste friendly error summary report
        String errorReport = buildCopyableErrorReport(result, currentUrl, pageTitle, screenshotPath, timestamp);
        saveErrorReportToFile(errorReport, testIdentifier);
        attachTextToAllure("AI Error Report (Copyable)", errorReport);

        log.error("================================================================================");
        log.error("❌ TEST FAILED: {}", result.getName());
        log.error("📸 Screenshot: {}", screenshotPath);
        log.error("📝 Error Report File: {}/{}_error.txt", REPORTS_DIR, testIdentifier);
        log.error("================================================================================");
    }

    private byte[] captureScreenshot(WebDriver driver) {
        if (driver instanceof TakesScreenshot) {
            try {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            } catch (Exception e) {
                log.error("Error while capturing screenshot: {}", e.getMessage());
            }
        }
        return new byte[0];
    }

    private String saveScreenshotToFile(byte[] screenshotBytes, String testIdentifier) {
        createDirectories();
        String filePath = SCREENSHOT_DIR + File.separator + testIdentifier + ".png";
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(screenshotBytes);
            return filePath;
        } catch (IOException e) {
            log.error("Failed to write screenshot to file: {}", e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private void saveErrorReportToFile(String content, String testIdentifier) {
        createDirectories();
        String filePath = REPORTS_DIR + File.separator + testIdentifier + "_error.txt";
        try (FileWriter writer = new FileWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            log.error("Failed to save error report to file: {}", e.getMessage());
        }
    }

    private String buildCopyableErrorReport(ITestResult result, String currentUrl, String pageTitle, String screenshotPath, String timestamp) {
        Throwable throwable = result.getThrowable();
        String exceptionType = throwable != null ? throwable.getClass().getName() : "Unknown Error";
        String exceptionMessage = throwable != null ? throwable.getMessage() : "No message";

        String stackTrace = "None";
        if (throwable != null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            stackTrace = filterStackTrace(sw.toString());
        }

        Object[] params = result.getParameters();
        String paramsStr = (params != null && params.length > 0) ? Arrays.toString(params) : "None";

        return new StringBuilder()
            .append("### ❌ TEST FAILURE SUMMARY (AI / DEV COPY READY) ###\n")
            .append("--------------------------------------------------------------------------------\n")
            .append("📌 Test Class    : ").append(result.getTestClass().getName()).append("\n")
            .append("📌 Test Method   : ").append(result.getMethod().getMethodName()).append("\n")
            .append("📌 Description   : ").append(result.getMethod().getDescription()).append("\n")
            .append("📌 Parameters    : ").append(paramsStr).append("\n")
            .append("📌 Timestamp     : ").append(timestamp).append("\n")
            .append("--------------------------------------------------------------------------------\n")
            .append("🌐 Page URL      : ").append(currentUrl).append("\n")
            .append("🌐 Page Title    : ").append(pageTitle).append("\n")
            .append("📸 Screenshot    : ").append(screenshotPath).append("\n")
            .append("--------------------------------------------------------------------------------\n")
            .append("🔴 Exception Type: ").append(exceptionType).append("\n")
            .append("🔴 Error Message : ").append(exceptionMessage).append("\n")
            .append("--------------------------------------------------------------------------------\n")
            .append("📋 Filtered Stack Trace:\n")
            .append(stackTrace).append("\n")
            .append("--------------------------------------------------------------------------------\n")
            .toString();
    }

    private String filterStackTrace(String fullStackTrace) {
        StringBuilder filtered = new StringBuilder();
        String[] lines = fullStackTrace.split("\n");
        int count = 0;
        for (String line : lines) {
            if (line.contains("com.hepsiburada.qa") || line.contains("org.openqa.selenium") || count < 15) {
                filtered.append(line).append("\n");
                count++;
            }
        }
        return filtered.toString().trim();
    }

    @Attachment(value = "{title}", type = "image/png")
    public byte[] attachScreenshotToAllure(String title, byte[] screenshot) {
        return screenshot;
    }

    @Attachment(value = "{title}", type = "text/plain")
    public String attachTextToAllure(String title, String content) {
        return content;
    }

    @Attachment(value = "{title}", type = "text/html")
    public String attachHtmlSourceToAllure(String title, String html) {
        return html;
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(SCREENSHOT_DIR));
            Files.createDirectories(Paths.get(REPORTS_DIR));
        } catch (IOException ignored) { }
    }

    @Override
    public void onFinish(ISuite suite) {
        // Generate Allure Environment Properties
        try {
            Path allureResultsDir = Paths.get("target", "allure-results");
            Files.createDirectories(allureResultsDir);
            Properties props = new Properties();
            props.setProperty("Environment", "QA / Automation");
            props.setProperty("Selenium.Version", "4.21.0");
            props.setProperty("OS", System.getProperty("os.name"));
            props.setProperty("Java.Version", System.getProperty("java.version"));

            File envFile = allureResultsDir.resolve("environment.properties").toFile();
            try (FileOutputStream fos = new FileOutputStream(envFile)) {
                props.store(fos, "Allure Environment Properties");
            }
        } catch (Exception ignored) { }
    }
}
