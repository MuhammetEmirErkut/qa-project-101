package com.hepsiburada.qa.tests.mobile.search;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.MobileBaseTest;
import com.hepsiburada.qa.dataprovider.SearchDataProvider;
import com.hepsiburada.qa.pages.mobile.home.MobileHomePage;
import com.hepsiburada.qa.pages.mobile.search.MobileSearchResultPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Mobile Web Automation")
@Feature("Mobile Search Flow")
public class MobileSearchTests extends MobileBaseTest {

    @Test(description = "User must verify the mobile search functionality")
    @Severity(SeverityLevel.NORMAL)
    @Story("Basic Mobile Search")
    @Description("1. Navigate to mobile home page\n2. Search for a product\n3. Verify mobile search results count and first product title")
    public void testSearchProducts() {
        // 1. Mobile Home Page & Search Action
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();
        MobileSearchResultPage searchResultPage = homePage.search("Laptop");

        // 2. Verification of Result Count
        int resultCount = searchResultPage.getResultCount();
        Assert.assertTrue(resultCount > 0, "No search results found on mobile (resultCount <= 0)");

        // 3. Verification of Product Title
        String firstTitle = searchResultPage.getFirstTitle();
        Assert.assertFalse(firstTitle.isEmpty(), "First product title in mobile search results should not be empty!");
    }

    @Test(
        description = "User must verify mobile search functionality with different search terms",
        dataProvider = "searchData",
        dataProviderClass = SearchDataProvider.class
    )
    @Severity(SeverityLevel.NORMAL)
    @Story("Data-Driven Mobile Search")
    @Description("Verify multiple search queries on mobile browser using JSON data provider")
    public void testSearchProductsWithDataProvider(String searchTerm) {
        // 1. Mobile Home Page & Data-Driven Search
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();
        MobileSearchResultPage searchResultPage = homePage.search(searchTerm);

        // 2. Verification of Results for Keyword
        int resultCount = searchResultPage.getResultCount();
        Assert.assertTrue(resultCount > 0, "No mobile search results found for: " + searchTerm);
    }

    @Test(description = "Verify that appropriate message is displayed when searching for non-existing keyword on mobile")
    @Severity(SeverityLevel.NORMAL)
    @Story("Negative Mobile Search")
    @Description("1. Navigate to mobile home page\n2. Search for a non-existing product\n3. Verify zero results or no results message")
    public void testSearchNonExistingProduct() {
        // 1. Mobile Home Page & Search with Invalid Keyword
        String invalidKeyword = "asdfghjklqwertyuiop";
        MobileHomePage homePage = new MobileHomePage(getDriver());
        homePage.handleCookies();
        MobileSearchResultPage searchResultPage = homePage.search(invalidKeyword);

        // 2. Verification of Zero Results or No-Results Message
        int resultCount = searchResultPage.getResultCount();
        boolean hasNoResultMessage = searchResultPage.isNoResultsMessageDisplayed();

        Assert.assertTrue(hasNoResultMessage || resultCount == 0,
            "Mobile search with invalid keyword should return 0 results or display no results message! Result Count: " + resultCount);
    }
}
