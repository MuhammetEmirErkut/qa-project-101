package com.hepsiburada.qa.steps;

import com.thoughtworks.gauge.Step;
import org.testng.Assert;

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
        Assert.assertTrue(hasNoMsg || count == 0, "Expected zero results or nor results message! Found: " + count);
    }
}
