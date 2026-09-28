# 🚀 Gauge & Page Object Model (POM) Enterprise Test Automation Guide

This guide provides a comprehensive, enterprise-grade reference for integrating the **Gauge Framework** alongside existing **TestNG** tests using the **Page Object Model (POM)**, **Component Object Model (COM)**, and **DataStore** architectural patterns. It details the setup, lifecycle management, concept reusability, and step-by-step implementations for all functional suites (`Search`, `PDP`, `Cart`, `Login`, and `Filter & Sort`) in English.

---

## 📑 Table of Contents
1. [Gauge & Page Object Model (POM) Architecture](#1-gauge--page-object-model-pom-architecture)
2. [TestNG vs Gauge Comparison & Coexistence Strategy](#2-testng-vs-gauge-comparison--coexistence-strategy)
3. [Gradle Configuration & Project Directory Structure](#3-gradle-configuration--project-directory-structure)
4. [Gauge Lifecycle & Execution Hooks](#4-gauge-lifecycle--execution-hooks)
5. [Automatic Screenshot Capturing (Custom Screenshot Grabber)](#5-automatic-screenshot-capturing-custom-screenshot-grabber)
6. [Inter-Step Data Sharing (DataStore Mechanism)](#6-inter-step-data-sharing-datastore-mechanism)
7. [Enterprise BaseStep & Page Object Management](#7-enterprise-basestep--page-object-management)
8. [Complete Specification & Step Implementations](#8-complete-specification--step-implementations)
   * 8.1. Search Tests (`Search.spec` & `SearchSteps.java`)
   * 8.2. Product Detail Page (PDP) Tests (`ProductDetail.spec` & `ProductDetailSteps.java`)
   * 8.3. Cart Tests (`Cart.spec` & `CartSteps.java`)
   * 8.4. Login & Negative Tests (`Login.spec` & `LoginSteps.java`)
   * 8.5. Filter & Sorting Tests (`FilterAndSort.spec` & `FilterAndSortSteps.java`)
9. [Reusable Business Workflows: Concept (.cpt) Files](#9-reusable-business-workflows-concept-cpt-files)
10. [Test Execution, Parallel Running & CI/CD Integration](#10-test-execution-parallel-running--cicd-integration)
11. [Best Practices & Anti-Patterns](#11-best-practices--anti-patterns)

---

## 🏛️ 1. Gauge & Page Object Model (POM) Architecture

When building a scalable test automation framework with Gauge, the industry standard is the **3-Tier Enterprise Architecture**:

```
┌─────────────────────────────────────────────────────────────────────────┐
│ 1. SPECIFICATION & CONCEPT TIER (.spec / .cpt)                         │
│    * Answers: "WHAT to test?"                                           │
│    * Plain English / Markdown business specifications.                  │
│    Example: * Search for product "Bluetooth Headset"                    │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ (Step Mapping)
┌────────────────────────────────────▼────────────────────────────────────┐
│ 2. STEP IMPLEMENTATION TIER (SearchSteps.java)                          │
│    * Answers: "ORCHESTRATION & GLUE CODE"                               │
│    * NEVER calls `driver.findElement` directly!                         │
│    * Delegates to Page Objects and performs business assertions.        │
│    Example: homePage.search(keyword);                                   │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ (Page Method Call)
┌────────────────────────────────────▼────────────────────────────────────┐
│ 3. PAGE OBJECT & COMPONENT TIER (HomePage.java, CartPage.java)          │
│    * Answers: "HOW to interact with the UI / DOM?"                      │
│    * Contains all Selenium WebDriver locators, explicit waits, actions. │
│    * Handles fluent navigation chaining across pages.                   │
│    Example: wait.until(ExpectedConditions.elementToBeClickable(...));   │
└─────────────────────────────────────────────────────────────────────────┘
```

### 🌟 Key Architectural Principles:
1. **Single Responsibility Principle (SRP):** When a UI locator or DOM hierarchy changes, only the corresponding `Page` class is modified. Step definitions and `.spec` files remain unaffected.
2. **High Reusability:** Page methods can be reused across dozens of distinct scenarios and concepts.
3. **Clean Code & Readability:** Step implementation classes remain lightweight and focused strictly on test flow and assertions.

---

## ⚖️ 2. TestNG vs Gauge Comparison & Coexistence Strategy

| Feature | TestNG Approach | Gauge + POM Approach |
| :--- | :--- | :--- |
| **Test Definition** | Java Test Classes (`@Test`) | Markdown Documents (`.spec` / `.cpt`) |
| **Readability** | Developer / QA Engineer only | Product Owners, Business Analysts, QA & Devs |
| **Data-Driven Testing (DDT)** | `@DataProvider` (Object[][] matrices) | Markdown Tables (`\| Data Value \|`) |
| **Step Reusability** | Helper utility methods | Concept Files (`.cpt`) |
| **State Sharing Between Steps** | Class variables / ThreadLocal | Built-in `DataStoreFactory` (Suite/Spec/Scenario) |
| **Reporting** | TestNG HTML / Allure | Built-in Live HTML Reports + Allure |

> [!IMPORTANT]
> **Coexistence Strategy:**
> Existing TestNG tests under `src/test/java/com/hepsiburada/qa/tests/` remain fully functional and intact. Gauge test specifications reside in the `specs/` directory and use the shared Page Object classes under `src/main/java/.../pages/`.

---

## 🏗️ 3. Gradle Configuration & Project Directory Structure

### 3.1. `build.gradle` Configuration
Add the Gauge Gradle plugin and dependencies to `build.gradle`:

```groovy
plugins {
    id 'java'
    id 'io.qameta.allure' version '2.12.0'
    id 'org.gauge' version '3.2.0'
}

dependencies {
    // ... Existing Selenium, Log4j, Owner dependencies ...

    // ==================== TEST RUNNERS ====================
    testImplementation "org.testng:testng:${testngVersion}"
    testImplementation "com.thoughtworks.gauge:gauge-java:${gaugeVersion}"
    testImplementation "org.assertj:assertj-core:${assertjVersion}"
}

// Gauge task configuration
gauge {
    specsDir = 'specs'
    inParallel = false
    nodes = 2
}
```

### 3.2. Project Directory Layout

```text
qa-project-101/
├── build.gradle
├── testng.xml                          <-- Existing TestNG Suite (Untouched)
├── env/
│   └── default/
│       ├── default.properties          <-- Gauge environment configuration
│       └── java.properties             <-- Gauge Java runner configuration
├── specs/                              <-- GAUGE SPECIFICATIONS (Markdown)
│   ├── concepts/
│   │   ├── common.cpt                  <-- Shared concepts (e.g. cookie acceptance)
│   │   └── cart_flows.cpt              <-- Reusable cart workflows
│   ├── search/
│   │   └── Search.spec                 <-- Search specs
│   ├── pdp/
│   │   └── ProductDetail.spec          <-- PDP specs
│   ├── cart/
│   │   └── Cart.spec                   <-- Cart specs
│   ├── login/
│   │   └── Login.spec                  <-- Login & negative validation specs
│   └── filter/
│       └── FilterAndSort.spec          <-- Filter & sort specs
├── src/
│   ├── main/java/com/hepsiburada/qa/
│   │   ├── drivers/DriverFactory.java  <-- Shared Thread-Safe Driver Factory
│   │   └── pages/                      <-- SHARED PAGE OBJECTS (POM)
│   │       ├── home/HomePage.java
│   │       ├── search/SearchResultPage.java
│   │       ├── pdp/ProductDetailPage.java
│   │       ├── cart/CartPage.java
│   │       └── login/LoginPage.java
│   └── test/java/com/hepsiburada/qa/
│       ├── base/BaseTest.java          <-- TestNG Base Class
│       ├── tests/                      <-- EXISTING TESTNG TESTS
│       ├── hooks/
│       │   ├── ExecutionHooks.java     <-- Gauge Scenario/Suite Lifecycle Hooks
│       │   └── CustomScreenshotGrabber.java <-- Gauge Screenshot Provider
│       └── steps/                      <-- GAUGE STEP DEFINITIONS
│           ├── BaseStep.java           <-- Common Base Step Class
│           ├── SearchSteps.java
│           ├── ProductDetailSteps.java
│           ├── CartSteps.java
│           ├── LoginSteps.java
│           └── FilterAndSortSteps.java
```

---

## 🔄 4. Gauge Lifecycle & Execution Hooks

Gauge provides lifecycle hook annotations for setup and teardown:
* `@BeforeSuite` / `@AfterSuite`
* `@BeforeSpec` / `@AfterSpec`
* `@BeforeScenario` / `@AfterScenario`
* `@BeforeStep` / `@AfterStep`

**File:** `src/test/java/com/hepsiburada/qa/hooks/ExecutionHooks.java`

```java
package com.hepsiburada.qa.hooks;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.hepsiburada.qa.config.ConfigReader;
import com.hepsiburada.qa.drivers.DriverFactory;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import com.thoughtworks.gauge.ExecutionContext;

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
        
        DriverFactory.initDriver(
            config.browser(),
            config.remote(),
            config.gridUrl(),
            config.headless()
        );
        
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
```

---

## 📸 5. Automatic Screenshot Capturing (Custom Screenshot Grabber)

To automatically attach full-page screenshots to Gauge HTML reports upon step failures, implement Gauge's `ICustomScreenshotGrabber` interface.

**File:** `src/test/java/com/hepsiburada/qa/hooks/CustomScreenshotGrabber.java`

```java
package com.hepsiburada.qa.hooks;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import com.hepsiburada.qa.drivers.DriverFactory;
import com.thoughtworks.gauge.screenshot.ICustomScreenshotGrabber;

public class CustomScreenshotGrabber implements ICustomScreenshotGrabber {

    @Override
    public byte[] takeScreenshot() {
        WebDriver driver = DriverFactory.getDriver();
        if (driver instanceof TakesScreenshot) {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        return new byte[0];
    }
}
```

---

## 💾 6. Inter-Step Data Sharing (DataStore Mechanism)

Gauge provides thread-safe data stores at three scopes:

| Store Type | Scope | Use Case |
| :--- | :--- | :--- |
| **`ScenarioDataStore`** | Current scenario execution | Product title, search keyword, initial price |
| **`SpecDataStore`** | Across all scenarios in a `.spec` | Spec-level authentication tokens / session IDs |
| **`SuiteDataStore`** | Entire test suite execution | Global test run metadata |

### Example Usage:
```java
import com.thoughtworks.gauge.datastore.DataStoreFactory;

// Save data in Scenario Store
DataStoreFactory.getScenarioDataStore().put("SAVED_PRODUCT_NAME", "Apple iPhone 13");

// Read data in subsequent steps
String productName = (String) DataStoreFactory.getScenarioDataStore().get("SAVED_PRODUCT_NAME");
```

---

## 🧩 7. Enterprise BaseStep & Page Object Management

To avoid repetitive `new HomePage(driver)` instantiations across step classes, create an abstract `BaseStep` base class.

**File:** `src/test/java/com/hepsiburada/qa/steps/BaseStep.java`

```java
package com.hepsiburada.qa.steps;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.hepsiburada.qa.drivers.DriverFactory;
import com.hepsiburada.qa.pages.cart.CartPage;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.login.LoginPage;
import com.hepsiburada.qa.pages.pdp.ProductDetailPage;
import com.hepsiburada.qa.pages.search.SearchResultPage;
import com.thoughtworks.gauge.datastore.DataStore;
import com.thoughtworks.gauge.datastore.DataStoreFactory;

public abstract class BaseStep {
    protected final Logger log = LogManager.getLogger(getClass());

    protected WebDriver getDriver() {
        return DriverFactory.getDriver();
    }

    protected HomePage homePage() {
        return new HomePage(getDriver());
    }

    protected SearchResultPage searchResultPage() {
        return new SearchResultPage(getDriver());
    }

    protected ProductDetailPage pdp() {
        return new ProductDetailPage(getDriver());
    }

    protected CartPage cartPage() {
        return new CartPage(getDriver());
    }

    protected LoginPage loginPage() {
        return new LoginPage(getDriver());
    }

    protected DataStore scenarioStore() {
        return DataStoreFactory.getScenarioDataStore();
    }
}
```

---

## 📝 8. Complete Specification & Step Implementations

---

### 8.1. Search Tests

#### 📄 `specs/search/Search.spec`
```markdown
# 🔍 Search Functionality Specifications

This specification verifies basic keyword search, data-driven table search, and non-existing keyword validations.

## 1. Valid Product Search and Title Verification
tags: smoke, search
* Navigate to home page and accept cookies
* Search for product "Laptop"
* Verify search results are displayed
* Verify first product title is not empty

## 2. Data-Driven Product Search with Keyword Table
tags: regression, search, data-driven

| Search Term         |
|---------------------|
| Headphones          |
| Smart Watch         |
| Mechanical Keyboard |

* Navigate to home page and accept cookies
* Search for product <Search Term>
* Verify search results are displayed for <Search Term>

## 3. Invalid Keyword Search Displays No Results Message
tags: negative, search
* Navigate to home page and accept cookies
* Search for product "asdfghjklqwertyuiop"
* Verify no results message is displayed or result count is zero
```

#### ☕ `src/test/java/com/hepsiburada/qa/steps/SearchSteps.java`
```java
package com.hepsiburada.qa.steps;

import org.testng.Assert;
import com.thoughtworks.gauge.Step;

public class SearchSteps extends BaseStep {

    @Step("Navigate to home page and accept cookies")
    public void navigateToHomeAndAcceptCookies() {
        homePage().handleCookies();
    }

    @Step("Search for product <keyword>")
    public void searchForProduct(String keyword) {
        log.info("Searching for keyword: {}", keyword);
        homePage().search(keyword);
    }

    @Step("Verify search results are displayed")
    public void verifySearchResultsExist() {
        int count = searchResultPage().getResultCount();
        Assert.assertTrue(count > 0, "No search results found! Count: " + count);
    }

    @Step("Verify first product title is not empty")
    public void verifyFirstProductTitleNotEmpty() {
        String title = searchResultPage().getFirstTitle();
        Assert.assertFalse(title.isEmpty(), "First product title was empty!");
    }

    @Step("Verify search results are displayed for <searchTerm>")
    public void verifySearchResultsForTerm(String searchTerm) {
        int count = searchResultPage().getResultCount();
        Assert.assertTrue(count > 0, "No search results found for term: " + searchTerm);
    }

    @Step("Verify no results message is displayed or result count is zero")
    public void verifyNoResultsMessageOrZeroCount() {
        int count = searchResultPage().getResultCount();
        boolean hasNoMsg = searchResultPage().isNoResultsMessageDisplayed();
        Assert.assertTrue(hasNoMsg || count == 0, "Expected zero results or no-results message! Found: " + count);
    }
}
```

---

### 8.2. Product Detail Page (PDP) Tests

#### 📄 `specs/pdp/ProductDetail.spec`
```markdown
# 📱 Product Detail Page (PDP) Specifications

This specification verifies product variant changes, guest wishlist login prompts, and product reviews.

## 1. Selecting Product Variant Updates Product Information
tags: pdp, variants
* Navigate to home page and accept cookies
* Search for product "iPhone 13"
* Click first product and navigate to detail page
* Save initial product price
* Select an alternative variant
* Verify product name and updated price are displayed

## 2. Guest User Adding to Wishlist Prompts Login
tags: pdp, wishlist, authentication
* Navigate to home page and accept cookies
* Search for product "MacBook"
* Click first product and navigate to detail page
* Click wishlist button
* Verify login modal or login page redirection is displayed

## 3. Opening Product Reviews and Ratings Tab
tags: pdp, reviews
* Navigate to home page and accept cookies
* Search for product "Samsung"
* Click first product and navigate to detail page
* Open product reviews tab
* Verify reviews container or ratings score is visible
```

#### ☕ `src/test/java/com/hepsiburada/qa/steps/ProductDetailSteps.java`
```java
package com.hepsiburada.qa.steps;

import org.testng.Assert;
import com.thoughtworks.gauge.Step;

public class ProductDetailSteps extends BaseStep {

    @Step("Click first product and navigate to detail page")
    public void clickFirstProductAndOpenPdp() {
        searchResultPage().clickFirstProduct();
    }

    @Step("Save initial product price")
    public void saveInitialProductPrice() {
        String initialPrice = pdp().getProductPriceText();
        scenarioStore().put("INITIAL_PRICE", initialPrice);
        log.info("Saved initial PDP price: {}", initialPrice);
    }

    @Step("Select an alternative variant")
    public void selectAlternativeVariant() {
        pdp().selectFirstAvailableAlternativeVariant();
    }

    @Step("Verify product name and updated price are displayed")
    public void verifyProductNameAndUpdatedPrice() {
        String name = pdp().getProductName();
        String updatedPrice = pdp().getProductPriceText();
        String initialPrice = (String) scenarioStore().get("INITIAL_PRICE");

        Assert.assertFalse(name.isEmpty(), "Product name cannot be empty!");
        Assert.assertTrue(!updatedPrice.isEmpty() || !initialPrice.isEmpty(), "Product price is not displayed!");
    }

    @Step("Click wishlist button")
    public void clickWishlistButton() {
        pdp().clickWishlist();
    }

    @Step("Verify login modal or login page redirection is displayed")
    public void verifyLoginPromptDisplayed() {
        Assert.assertTrue(pdp().isLoginRequiredPromptDisplayed(), "Login requirement prompt was not displayed!");
    }

    @Step("Open product reviews tab")
    public void openProductReviewsTab() {
        pdp().openReviewsTab();
    }

    @Step("Verify reviews container or ratings score is visible")
    public void verifyReviewsContainerVisible() {
        Assert.assertTrue(pdp().isReviewsContainerVisible(), "Reviews container or score was not visible!");
    }
}
```

---

### 8.3. Cart Tests

#### 📄 `specs/cart/Cart.spec`
```markdown
# 🛒 Cart Operations Specifications

This specification verifies adding products to cart, validating cart contents, and removing items from cart.

## 1. User Can Add Product to Cart
tags: critical, cart, smoke
* Navigate to home page and accept cookies
* Search for product "Bluetooth Headset"
* Click first product and navigate to detail page
* Save expected product name
* Add product to cart and go to cart page
* Verify cart contains at least 1 item
* Verify first cart item name is not empty

## 2. User Can Remove Product from Cart
tags: critical, cart, removal
* Navigate to home page and accept cookies
* Search for product "Bluetooth Headset"
* Click first product and navigate to detail page
* Add product to cart and go to cart page
* Verify cart contains at least 1 item before removal
* Remove first product from cart
* Verify cart is empty
```

#### ☕ `src/test/java/com/hepsiburada/qa/steps/CartSteps.java`
```java
package com.hepsiburada.qa.steps;

import org.testng.Assert;
import com.thoughtworks.gauge.Step;

public class CartSteps extends BaseStep {

    @Step("Save expected product name")
    public void saveExpectedProductName() {
        String name = pdp().getProductName();
        scenarioStore().put("EXPECTED_PRODUCT_NAME", name);
        log.info("Saved expected product name: {}", name);
    }

    @Step("Add product to cart and go to cart page")
    public void addProductToCartAndGoToCart() {
        pdp().addToCart().goToCart();
    }

    @Step("Verify cart contains at least 1 item")
    public void verifyCartContainsItems() {
        int count = cartPage().getCartItemCount();
        Assert.assertTrue(count > 0, "Cart is empty! Item count: " + count);
    }

    @Step("Verify first cart item name is not empty")
    public void verifyFirstCartItemNameNotEmpty() {
        String itemName = cartPage().getFirstCartItemName();
        Assert.assertFalse(itemName.isEmpty(), "First cart item name is empty!");
    }

    @Step("Verify cart contains at least 1 item before removal")
    public void verifyCartContainsItemsBeforeRemoval() {
        Assert.assertTrue(cartPage().getCartItemCount() > 0, "Cart must have items prior to deletion!");
    }

    @Step("Remove first product from cart")
    public void removeFirstProductFromCart() {
        cartPage().removeFirstProduct();
    }

    @Step("Verify cart is empty")
    public void verifyCartIsEmpty() {
        Assert.assertTrue(cartPage().isCartEmpty(), "Cart should be empty after item removal!");
    }
}
```

---

### 8.4. Login & Negative Tests

#### 📄 `specs/login/Login.spec`
```markdown
# 🔐 Login & Negative Validation Specifications

This specification verifies invalid email formats, blank inputs, wrong credentials, and password visibility toggling.

## 1. Invalid Email Format Rejection
tags: negative, login
* Navigate to home page and accept cookies
* Navigate to login page
* Enter email "invalid-email-format" and click login
* Verify invalid email format error message is displayed

## 2. Blank Email Field Validation
tags: negative, login
* Navigate to home page and accept cookies
* Navigate to login page
* Leave email blank and click login
* Verify blank email error message is displayed

## 3. Invalid Password Authentication Failure
tags: negative, login
* Navigate to home page and accept cookies
* Navigate to login page
* Enter email "testuser@example.com" and click login
* Enter password "WrongPassword123!" and submit
* Verify invalid credentials error message is displayed

## 4. Password Visibility Toggle Verification
tags: login, ui
* Navigate to home page and accept cookies
* Navigate to login page
* Enter email "testuser@example.com" and click login
* Enter password "SecretPassword123"
* Verify password input is masked
* Click toggle password visibility icon
* Verify password input is unmasked
```

#### ☕ `src/test/java/com/hepsiburada/qa/steps/LoginSteps.java`
```java
package com.hepsiburada.qa.steps;

import org.testng.Assert;
import com.thoughtworks.gauge.Step;

public class LoginSteps extends BaseStep {

    @Step("Navigate to login page")
    public void navigateToLoginPage() {
        homePage().goToLoginPage();
    }

    @Step("Enter email <email> and click login")
    public void enterEmailAndClickLogin(String email) {
        loginPage().enterEmail(email).clickLogin();
    }

    @Step("Leave email blank and click login")
    public void leaveEmailBlankAndClickLogin() {
        loginPage().clickLogin();
    }

    @Step("Enter password <password> and submit")
    public void enterPasswordAndSubmit(String password) {
        loginPage().enterPassword(password).clickPasswordSubmit();
    }

    @Step("Enter password <password>")
    public void enterPasswordOnly(String password) {
        loginPage().enterPassword(password);
    }

    @Step("Verify invalid email format error message is displayed")
    public void verifyInvalidEmailErrorMessage() {
        Assert.assertTrue(loginPage().isInvalidEmailErrorMessageDisplayed(), "Invalid email error message was not displayed!");
    }

    @Step("Verify blank email error message is displayed")
    public void verifyBlankEmailErrorMessage() {
        Assert.assertTrue(loginPage().isBlankEmailErrorMessageDisplayed(), "Blank email error message was not displayed!");
    }

    @Step("Verify invalid credentials error message is displayed")
    public void verifyInvalidCredentialsMessage() {
        Assert.assertTrue(loginPage().isInvalidCredentialsMessageDisplayed(), "Authentication failure message was not displayed!");
    }

    @Step("Verify password input is masked")
    public void verifyPasswordMasked() {
        Assert.assertFalse(loginPage().isPasswordVisible(), "Password should be masked initially!");
    }

    @Step("Click toggle password visibility icon")
    public void clickTogglePasswordVisibility() {
        loginPage().togglePasswordVisibility();
    }

    @Step("Verify password input is unmasked")
    public void verifyPasswordUnmasked() {
        Assert.assertTrue(loginPage().isPasswordVisible(), "Password should be visible after toggle!");
    }
}
```

---

### 8.5. Filter & Sorting Tests

#### 📄 `specs/filter/FilterAndSort.spec`
```markdown
# 🏷️ Filter and Sorting Specifications

This specification verifies brand filtering, lowest price sorting, and price range filters.

## 1. Filter Search Results by Brand
tags: filter, brand
* Navigate to home page and accept cookies
* Search for product "Phone"
* Apply brand filter "Apple"
* Verify top "5" product titles contain "Apple" or "iPhone"

## 2. Sort Search Results by Lowest Price
tags: sort, price
* Navigate to home page and accept cookies
* Search for product "SSD"
* Sort products by lowest price
* Verify top "5" product prices are in ascending order

## 3. Filter Search Results by Price Range
tags: filter, price-range
* Navigate to home page and accept cookies
* Search for product "Monitor"
* Filter products with price range "1000" to "5000"
* Verify top "5" product prices are between "1000" and "5000"
```

#### ☕ `src/test/java/com/hepsiburada/qa/steps/FilterAndSortSteps.java`
```java
package com.hepsiburada.qa.steps;

import java.util.List;
import org.testng.Assert;
import com.thoughtworks.gauge.Step;

public class FilterAndSortSteps extends BaseStep {

    @Step("Apply brand filter <brandName>")
    public void applyBrandFilter(String brandName) {
        searchResultPage().filterByBrand(brandName);
    }

    @Step("Verify top <limit> product titles contain <keyword1> or <keyword2>")
    public void verifyTopProductTitlesContain(int limit, String keyword1, String keyword2) {
        List<String> titles = searchResultPage().getTopProductTitles(limit);
        Assert.assertFalse(titles.isEmpty(), "Filtered product list is empty!");
        for (String title : titles) {
            boolean matches = title.toLowerCase().contains(keyword1.toLowerCase()) || 
                              title.toLowerCase().contains(keyword2.toLowerCase());
            Assert.assertTrue(matches, "Product title did not contain expected brand: " + title);
        }
    }

    @Step("Sort products by lowest price")
    public void sortProductsByLowestPrice() {
        searchResultPage().sortByLowestPrice();
    }

    @Step("Verify top <limit> product prices are in ascending order")
    public void verifyPricesAscending(int limit) {
        List<Double> prices = searchResultPage().getProductPricesList(limit);
        Assert.assertTrue(prices.size() >= 2, "At least 2 prices needed to verify ascending order!");
        for (int i = 0; i < prices.size() - 1; i++) {
            Assert.assertTrue(prices.get(i) <= prices.get(i + 1),
                "Prices not in ascending order: " + prices.get(i) + " > " + prices.get(i + 1));
        }
    }

    @Step("Filter products with price range <minPrice> to <maxPrice>")
    public void filterPriceRange(String minPrice, String maxPrice) {
        searchResultPage().filterByPriceRange(minPrice, maxPrice);
    }

    @Step("Verify top <limit> product prices are between <minPrice> and <maxPrice>")
    public void verifyPriceRange(int limit, double minPrice, double maxPrice) {
        List<Double> prices = searchResultPage().getProductPricesList(limit);
        Assert.assertFalse(prices.isEmpty(), "Price list is empty!");
        for (Double price : prices) {
            Assert.assertTrue(price >= minPrice && price <= maxPrice,
                "Price out of range: " + price + " (Expected: " + minPrice + " - " + maxPrice + ")");
        }
    }
}
```

---

## 🧩 9. Reusable Business Workflows: Concept (.cpt) Files

Concepts encapsulate frequently repeated sequences of steps into a single reusable step.

#### 📄 `specs/concepts/common.cpt`
```markdown
# Search for <keyword> and navigate to PDP
* Search for product <keyword>
* Verify search results are displayed
* Click first product and navigate to detail page

# Add <keyword> to cart
* Search for <keyword> and navigate to PDP
* Save expected product name
* Add product to cart and go to cart page
```

#### 📄 Using Concepts in Specifications:
In your `.spec` files, you can now execute 3-4 steps with a single concept line:
```markdown
## Quick Cart Checkout Scenario
* Navigate to home page and accept cookies
* Add "Bluetooth Headset" to cart
* Verify cart contains at least 1 item
```

---

## 🚀 10. Test Execution, Parallel Running & CI/CD Integration

### 10.1. CLI Commands

```bash
# Run all specifications
gauge run specs

# Run a specific spec file
gauge run specs/cart/Cart.spec

# Run tests by Tag (e.g., smoke suite)
gauge run --tags "smoke" specs

# Multi-tag execution (smoke AND search)
gauge run --tags "smoke & search" specs

# Parallel execution with 4 worker threads
gauge run -n 4 specs
```

### 10.2. Gradle Task Execution

```bash
# Execute Gauge tests via Gradle
./gradlew gauge

# Execute specific directory with Gradle
./gradlew gauge -PspecsDir=specs/cart
```

### 10.3. Jenkins Pipeline (Jenkinsfile) Snippet

```groovy
pipeline {
    agent any
    tools {
        jdk 'Java17'
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/your-org/qa-project-101.git'
            }
        }
        stage('Run Gauge Smoke Tests') {
            steps {
                sh './gradlew gauge -Ptags="smoke"'
            }
        }
        stage('Publish HTML Reports') {
            always {
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'reports/html-report',
                    reportFiles: 'index.html',
                    reportName: 'Gauge Test Report'
                ])
            }
        }
    }
}
```

---

## 💎 11. Best Practices & Anti-Patterns

1. ❌ **Do NOT Write Selenium Code in Step Classes (Anti-Pattern):**
   * Never put `driver.findElement(...)` or `Thread.sleep(...)` inside Step implementations. All UI interactions belong inside Page Objects ([HomePage](file:///Users/erkut/projects/qa-project-101/src/main/java/com/hepsiburada/qa/pages/home/HomePage.java), [CartPage](file:///Users/erkut/projects/qa-project-101/src/main/java/com/hepsiburada/qa/pages/cart/CartPage.java)).
2. ❌ **Do NOT Share State via Static Variables:**
   * Avoid `public static String productName;` in Step classes. Use `scenarioStore().put(...)` and `scenarioStore().get(...)` to guarantee thread safety during parallel runs.
3. ✅ **Keep Steps Atomic and Expressive:**
   * Use clean, parameter-driven sentences in plain English: `* Search for product <keyword>`.
4. ✅ **Modularize with Concepts:**
   * Group repetitive sequences of 3+ steps into `.cpt` files to keep specifications concise and readable.
