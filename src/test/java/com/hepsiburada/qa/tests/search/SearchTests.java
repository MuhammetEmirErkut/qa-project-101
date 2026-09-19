package com.hepsiburada.qa.tests.search;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.BaseTest;
import com.hepsiburada.qa.dataprovider.SearchDataProvider;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.search.SearchResultPage;
import static com.hepsiburada.qa.drivers.DriverFactory.getDriver;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

public class SearchTests extends BaseTest {

    @Test(description = "User must verify the search functionality")
    @Severity(SeverityLevel.NORMAL)
    @Description("1. Navigate to home page\n2. Search for a product\n3. Verify search results")
    public void testSearchProducts() {
        // 1. Home Page & Search Action
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.search("Laptop");
        
        // 2. Verification of Result Count
        int resultCount = searchResultPage.getResultCount();
        Assert.assertTrue(resultCount > 0, "No search results found (resultCount <= 0)");

        // 3. Verification of Product Title
        String firstTitle = searchResultPage.getFirstTitle();
        Assert.assertFalse(firstTitle.isEmpty(), "First product title in search results should not be empty!");
    }

    @Test(description = "User must verify the search functionality with different search terms", 
          dataProvider = "searchData", 
          dataProviderClass = SearchDataProvider.class)
    public void testSearchProductsWithDataProvider(String searchTerm) {
        // 1. Home Page & Data-Driven Search
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.search(searchTerm);
        
        // 2. Verification of Results for Keyword
        int resultCount = searchResultPage.getResultCount();
        Assert.assertTrue(resultCount > 0, "No search results found for: " + searchTerm);
    }

    @Test(description = "Verify that appropriate message is displayed when searching for non-existing keyword")
    @Severity(SeverityLevel.NORMAL)
    @Description("1. Navigate to home page\n2. Search for a non-existing product\n3. Verify zero results or no results message")
    public void testSearchNonExistingProduct(){
        // 1. Home Page & Search with Invalid Keyword
        String invalidKeyword = "asdfghjklqwertyuiop";
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.search(invalidKeyword);
        
        // 2. Verification of Zero Results or No-Results Message
        int resultCount = searchResultPage.getResultCount();
        boolean hasNoResultMessage = searchResultPage.isNoResultsMessageDisplayed();

        Assert.assertTrue(hasNoResultMessage || resultCount == 0, 
            "Search with invalid keyword should return 0 results or display no results message! Result Count: " + resultCount);
    }
}
