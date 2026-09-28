package com.hepsiburada.qa.steps;

import com.hepsiburada.qa.drivers.DriverFactory;
import com.hepsiburada.qa.pages.cart.CartPage;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.login.LoginPage;
import com.hepsiburada.qa.pages.pdp.ProductDetailPage;
import com.hepsiburada.qa.pages.search.SearchResultPage;
import com.thoughtworks.gauge.datastore.DataStore;
import com.thoughtworks.gauge.datastore.DataStoreFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

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

    protected ProductDetailPage productDetailPage() {
        return new ProductDetailPage(getDriver());
    }

    protected ProductDetailPage pdp() {
        return productDetailPage();
    }

    protected CartPage cartPage() {
        return new CartPage(getDriver());
    }

    protected LoginPage loginPage() {
        return new LoginPage(getDriver());
    }

    protected DataStore scenarioStore(){
        return DataStoreFactory.getScenarioDataStore();
    }
}
